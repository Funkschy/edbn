(ns edbn.sql
  (:import
   [java.sql Connection PreparedStatement]))

(defprotocol StatementParam
  (set-param [param index stmt]))

;; TODO: implement rest
(extend-protocol StatementParam
  java.lang.String
  (set-param [param index ^PreparedStatement stmt]
    (.setString stmt index param)
    (inc index))

  java.lang.Long
  (set-param [param index ^PreparedStatement stmt]
    (.setLong stmt index param)
    (inc index))

  clojure.lang.Keyword
  (set-param [param index ^PreparedStatement stmt]
    (.setString stmt index (name param))
    (inc index))

  java.util.Collection
  (set-param [param index ^PreparedStatement stmt]
    (doseq [[i p] (map-indexed vector param)]
      (set-param p (+ index i) stmt))
    (+ index (count param))))

(defn prepare-statement [^Connection conn sql & args]
  (let [s (.prepareStatement conn sql)]
    (reduce (fn [i a]
              (set-param a i s))
            1
            args)
    s))
