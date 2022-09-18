(ns edbn.query
  (:require
   [clojure.string :as str]))

(declare parse-query)

(defn- parse-function [[f & args]]
  (case f
    :in {:kind :set :set (first args)}))

(defn- parse-condition [k v]
  (cond
    (map? v)    {:kind :sub-query :key k :sub (parse-query v)}
    (vector? v) {:kind :function :key k :f (parse-function v)}
    :else       {:kind :eq :key k :value v}))

(defn- parse-query [query]
  (map (partial apply parse-condition) query))

(defn- collect-path [path operator]
  (str "json_extract(json, '$."
       (str/join "." path)
       "') "
       operator
       " "))

(defmulti collect-query
  (fn [q _]
    (if (or (seq? q) (vector? q))
      :and
      (get q :kind))))

(defmethod collect-query :eq [{:keys [key value]} path]
  [(collect-path (conj path (name key)) "=")
   value])

(defmulti collect-function :kind)

(defmethod collect-function :set [{values :set}]
  (set values))

(defmethod collect-query :function [{:keys [f key]} path]
  [(collect-path (conj path (name key)) "in")
   (collect-function f)])

(defmethod collect-query :sub-query [{:keys [sub key]} path]
  (collect-query sub (conj path (name key))))

(defmethod collect-query :and [conditions path]
  (map #(collect-query % path) conditions))

(defn- to-? [p]
  (if (coll? p)
    (str "(" (str/join ", " (repeat (count p) "?")) ")")
    "?"))

(defn query-to-sql [query]
  (let [parsed  (parse-query query)
        parts   (partition 2 (flatten (collect-query parsed [])))
        q-parts (map (fn [[q p]] (str q (to-? p))) parts)
        v-parts (map second parts)]
    (when-not (empty? q-parts)
      [(str "where " (str/join " and " q-parts)) v-parts])))

