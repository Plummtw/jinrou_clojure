
(ns jinrou-clojure.view.baseview
  (:refer-clojure)
  (:use hiccup.core))

(defn html-header [css js]
  `[:head
      [:meta {:http-equiv "Content-Type", :content "text/html; charset=utf-8"} ]
      [:meta {:http-equiv "Content-Style-Type", :content "text/css"} ]
      [:meta {:http-equiv "Content-Script-Type", :content "text/javascript"} ]
      [:title "人狼爭霸－鸚鵡實驗站"]
      [:script.jquery    {:src "http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js", :type "text/javascript"} ]
      [:script.jquery-ui {:src "http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.5/jquery-ui.min.js", :type "text/javascript"} ]
      ~@(map (fn [x] [:script {:src (str "js/" x ".js"), :type "text/javascript"} ]) js)
      [:link {:rel "stylesheet", :href "css/jinrou_domi.css"} ]
      ~@(map (fn [x] [:link {:rel "stylesheet", :href (str "css/" x ".css")} ]) css)
    ])

(defn jinrou-domi-view [{:keys [view css js] :as hash}]
  (let [view (if (map? hash) view hash)
        css  (if (or (coll? css) (nil? css)) css (list css))
        js   (if (or (coll? js) (nil? js)) js (list js))]
    (html
      [:html
       (html-header css js)
       [:body
        [:div {:class "body"}
         view ]]])))

(defn sub-view [{:keys [view css js] :as hash}]
  (let [view (if (map? hash) view hash)
        css  (if (or (coll? css) (nil? css)) css (list css))
        js   (if (or (coll? js) (nil? js)) js (list js))]
    (html
      [:html
       (html-header css js)
       [:body 
        [:div.back-main
         [:a {:href "main"} "←回主選單"]]
        [:div.body
         view ]
        [:div.back-main
         [:a {:href "main"} "←回主選單"]]]])))

(defn paginate [offset number per-page]
  (assert (number? offset))
  (assert (number? number))
  (assert (number? per-page))
  (let [last-page (quot (dec number) per-page)
        current-page (cond
                       (<= offset 0) 0
                       (>= offset last-page) last-page
                       :else offset)]
    {:first? (= current-page 0)
     :last?  (= current-page last-page)
     :last    last-page
     :current current-page
     :prevs  (range (max 0 (- current-page 3)) current-page)
     :nexts  (range (inc current-page) (inc (min  (+ current-page 3) last-page)))}))

(defn paginate-div [link offset number per-page]
  (let [{:keys [first? last? last current prevs nexts]}
        (paginate offset number per-page)
        page-link  (fn ([offset_ text] [:a {:href (str link "?offset=" offset_)} text])
                     ([offset_] [:a {:href (str link "?offset=" offset_)} (str offset_)]))
        edge-link  (fn [text? offset_ text] (if text? text (page-link offset_ text)))]
    [:div.paginate
     (edge-link first? "0" "<<")
     "｜"
     (edge-link first? (dec current) "<")
     "｜"
     [:span (interpose "｜" (map page-link prevs))]
     (str "[" current "]")
     [:span (interpose "｜" (map page-link nexts))]
     "｜"
     (edge-link last? (inc current) ">")
     "｜"
     (edge-link last? last ">>")]))

(defn paginate-ajax-div [offset number per-page]
  (let [{:keys [first? last? last current prevs nexts]}
        (paginate offset number per-page)
        page-link  (fn ([offset_ text] [:a {:href "#" :offset offset_} text])
                     ([offset_] [:a {:href "#" :offset offset_} (str offset_)]))
        edge-link  (fn [text? offset_ text] (if text? text (page-link offset_ text)))]
    [:div.paginate
     (edge-link first? "0" "<<")
     "｜"
     (edge-link first? (dec current) "<")
     "｜"
     [:span (interpose "｜" (map page-link prevs))]
     (str "[" current "]")
     [:span (interpose "｜" (map page-link nexts))]
     "｜"
     (edge-link last? (inc current) ">")
     "｜"
     (edge-link last? last ">>")]))

