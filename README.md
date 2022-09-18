# EDBN

A small library to turn SQLite into a document database inspired by MongoDB.

## How does it work?

SQLite actually has [Json support](https://www.sqlite.org/json1.html), so we can use that to store and query unstructured data inside of it.
 
EDBN will simply convert the clojure map you throw into the library into json and then store that json inside of SQLite. When you query the data, it will convert your query into sql and parse the returned json back into a clojure map under the hood.

For example:

``` clojure
;; The following query...
(query conn "users" {:name "jeff" :info {:age [:in #{23 24 25}] :birth-month :june}})

;; is turned into the following sql prepared statement
;; SELECT json FROM users where json_extract(json, '$.name') = ? and json_extract(json, '$.info.age') in (?, ?, ?) and json_extract(json, '$.info.birth-month') = ?
```

## Usage

``` clojure
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
```
