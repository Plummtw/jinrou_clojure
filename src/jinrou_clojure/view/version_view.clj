
(ns jinrou-clojure.view.version-view
  (:refer-clojure)
  (:use hiccup.core
    jinrou-clojure.view.baseview))

(defn version-div []
  [:div#version
   [:h4 "99年7月17日"]
   [:ul
    [:li "Scala 2.8釋出，而Lift 2.1-SNAPSHOT支援Scala 2.8，故升級至Lift 2.1-SNAPSHOT。"]]
   [:h4 "99年7月12日"]
   [:ul
    [:li "因Lift 2.0釋出，開始進行測試Lift 2.0。"]]])

(defn version-view []
  (sub-view (version-div)))