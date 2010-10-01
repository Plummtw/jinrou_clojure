(defproject jinrou-clojure "1.0.0-SNAPSHOT"
 :project.build.sourceEncoding "UTF-8"
 :description "Jinrou-clojure written by Plummtw"
 :dependencies [[org.clojure/clojure "1.2.0"]
                [org.clojure/clojure-contrib "1.2.0"]
                [compojure "0.5.1"]
                [ring/ring-servlet "0.3.0"]
                [ring/ring-devel "0.3.0"]
                [ring/ring-jetty-adapter "0.3.0"]
                [sandbar/sandbar-session "0.2.4"]
                [congomongo "0.1.3-SNAPSHOT"]
                [uk.org.alienscience/cache-dot-clj "0.0.2"]]
  ;; Leiningen-war should be a dev dependency
  :dev-dependencies [[swank-clojure "1.2.1"]
                     [uk.org.alienscience/leiningen-war "0.0.8"]]
  ;; A servlet class must compiled for use in a java web server
  :aot [deploy.servlet])


