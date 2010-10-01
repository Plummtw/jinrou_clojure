
(ns jinrou-clojure.deploy.servlet
  (:use ring.util.servlet)
  (:require jinrou-clojure.controller)
  (:gen-class :extends javax.servlet.http.HttpServlet))

(defservice jinrou-clojure.controller/*routes*)


