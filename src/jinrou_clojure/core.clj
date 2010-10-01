(ns jinrou-clojure.core
  (:use jinrou-clojure.controller))

#_(defroutes example
  (GET "/" [] "<h1>Hello World Wide Web!</h1>")
  (route/not-found "Page not found"))

#_(future (run-jetty (var example) {:port 8080}))

(in-ns 'user)
(use 'jinrou-clojure.core)

