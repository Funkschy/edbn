(ns edbn.core
  (:require
   [clojure.data.json :as json]
   [edbn.query :refer [query-to-sql]]
   [edbn.sql :refer [prepare-statement]])
  (:import
   [java.sql Connection DriverManager PreparedStatement]))

;; (set! *warn-on-reflection* true)

(defn create-connection!
  ([db-name]
   (DriverManager/getConnection (str "jdbc:sqlite:" db-name)))
  ([db-name user password]
   (DriverManager/getConnection (str "jdbc:sqlite:" db-name) user password)))

(defn close-connection! [^Connection conn]
  (.close conn))

(defn create-collection! [^Connection conn coll-name]
  (->> (str "CREATE TABLE " coll-name  "(json TEXT)")
       ^PreparedStatement (prepare-statement conn)
       .executeUpdate))

(defn destroy-collection! [^Connection conn coll-name]
  (->> (str "DROP TABLE " coll-name)
       ^PreparedStatement (prepare-statement conn)
       .executeUpdate))

(defn insert! [conn coll document]
  (let [json-data (json/write-str document)
        sql (str "INSERT INTO " coll " (json) VALUES (?)")
        stmt (prepare-statement conn sql json-data)]
    (.executeUpdate ^PreparedStatement stmt)))

(defn query
  ([conn coll] (query conn coll {}))
  ([conn coll query]
   (let [[where params] (query-to-sql query)
         sql (str "SELECT json FROM " coll " " where)
         ;; _ (prn :query sql params)
         ^PreparedStatement stmt (apply prepare-statement conn sql params)]
     (map (comp #(json/read-str % :key-fn keyword) :json)
          (resultset-seq (.executeQuery stmt))))))

(defn delete! [conn coll query]
  (let [[where params] (query-to-sql query)
        sql (str "DELETE FROM " coll " " where)
        ;; _ (prn sql params)
        ^PreparedStatement stmt (apply prepare-statement conn sql params)]
    (.executeUpdate stmt)))

(def conn (create-connection! "test.db"))

(create-collection! conn "users")

(insert! conn "users" {:name "Jeff" :info {:age 24 :birth-month :june}})
(insert! conn "users" {:name "Bob" :info {:age 20 :birth-month :june}})

(query conn "users" {:name "Jeff" :info {:age [:in (range 20 25)] :birth-month :june}})
(query conn "users")

(delete! conn "users" {:name "Bob"})
(query conn "users")

(destroy-collection! conn "users")
(close-connection! conn)
