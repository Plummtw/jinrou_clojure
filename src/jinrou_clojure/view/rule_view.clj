
(ns jinrou-clojure.view.rule-view
  (:refer-clojure)
  (:use hiccup.core
    jinrou-clojure.view.baseview))

(defn rule-div []
  [:div#rule 
   [:p [:span "施工中"]]])

(defn rule-view []
  (sub-view (rule-div)))


