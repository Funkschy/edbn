(defproject edbn "0.1.0-SNAPSHOT"
  :description "Use SQLite as a document database"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.xerial/sqlite-jdbc "3.36.0"]]
  :repl-options {:init-ns edbn.core})
