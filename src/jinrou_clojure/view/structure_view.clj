
(ns jinrou-clojure.view.structure-view
  (:refer-clojure)
  (:use hiccup.core
    jinrou-clojure.view.baseview))

(defn structure-div []
  [:div#structure
   [:p [:span "施工中"]]])

(defn structure-view []
  (sub-view (structure-div)))


