
(ns jinrou-clojure.view.usericon-view
  (:refer-clojure)
  (:use hiccup.core
    jinrou-clojure.util
    jinrou-clojure.view.baseview
    jinrou-clojure.model.usericon
    jinrou-clojure.properties))

(def *usericon-per-line*  5)
(def *usericon-per-page* 25)

;(def *usericon-id* nil)

(defn usericon-color-with-name [usericon]
  [:span [:font {:color (:color usericon)} "◆" ] (:color usericon)])

(defn usericon-img [usericon]
  [:img {:src (:filename usericon) :width (:width usericon) :height (:height usericon)
         :style (str "border-color:" (:color usericon) ";")}])

(defn- usericon-td [usericon]
  [[:td (usericon-img usericon)]
   [:td (:id usericon) [:br]
    (:name usericon) [:br]
    (:color usericon) [:br]]])

(defn- usericon-tr [usericons]
  [:tr (mapcat usericon-td usericons)])

(defn- usericon-table [usericons]
  [:table#usericon.usericon
   (map usericon-tr  (partition *usericon-per-line* *usericon-per-line* nil usericons))])


(defn usericon-div [offset]
  [:div#usericon
   [:table {:align "center"}
    [:tr
     [:td
      [:div#link
       [:a {:href "usericon_upload"} "←頭像上傳"]]]]
    [:tr
     [:td
      [:fieldset
       [:legend "頭像一覽"]
       (usericon-table (usericons (* *usericon-per-page* offset) *usericon-per-page*))
       (paginate-div "usericon" offset (usericons-count) *usericon-per-page*) ]]]]])

(defn usericon-view [params]
  (let [{:strs [offset]} params
        offset (if offset (parse-int offset) 0)]
    (sub-view (usericon-div offset))))

(defn- usericon-select-td [usericon]
  [[:td (usericon-img usericon)]
   [:td (:id usericon) [:br]
    (:name usericon) [:br]
    (:color usericon) [:br]
    [:input {:type "radio" :name "usericon_id" ;:checked (= (:_id usericon) *usericon-id*)
             :value (:_id usericon) }]]])

(defn- usericon-select-tr [usericons]
  [:tr (mapcat usericon-select-td usericons)])

(defn- usericon-select-table [usericons]
  [:table#usericon.usericon
   (map usericon-select-tr
     (partition *usericon-per-line* *usericon-per-line* nil usericons))])

(defn usericon-ajax-div [params]
  (let [{:strs [offset]} params
        offset (if offset (parse-int offset) 0)]
    [:fieldset
       [:legend "頭像一覽"]
       (usericon-select-table (usericons (* *usericon-per-page* offset) *usericon-per-page*))
       (paginate-ajax-div offset (usericons-count) *usericon-per-page*) ]))

(in-ns 'user)
(use 'jinrou-clojure.view.usericon-view)
