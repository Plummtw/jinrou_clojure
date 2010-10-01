
(ns jinrou-clojure.view.game-view
  (:refer-clojure)
  (:use hiccup.core
    hiccup.page-helpers
    sandbar.stateful-session
    ring.util.response
    jinrou-clojure.util
    jinrou-clojure.util.date-util
    jinrou-clojure.model.jinrouuser
    jinrou-clojure.model.jinroudomi
    jinrou-clojure.view.baseview
    jinrou-clojure.properties)
  (:import (java.util Date)))

(defn game-div []
  (let [jinrouuser (current-jinrouuser)
        errmsgs (flash-message)]
    [:div#game
     [:div.server-name "人狼爭霸－鸚鵡實驗站"]
     [:noscript "請開啟JavaScript(Please turn javascript on)"]
      [:div#tabs
       [:ul
        [:li
         [:a#tabs-info-a {:href "#tabs-game"} "訊息視窗"]]
        [:li
         [:a#tabs-game-list-a {:href "#tabs-action"} "操作視窗"]]
        [:li
         [:a#tabs-main-chat-a {:href "#tabs-main-chat"} "主聊天視窗"]]]
       [:br ]
       [:div#tabs-game]
       [:div#tabs-action]
         [:div#tabs-main-chat
          [:p
           [:fieldset
            [:legend "主聊天視窗(Main Chat)"]
            [:div {:class "chat main-chat"}
             (if (not (empty? jinrouuser))
               [:form
                [:table
                 [:tr [:td [:textarea#main-chat-textarea {:name "chat-text" :size "30" :rows "3" :cols "70" :wrap "soft"}  ]]
                  [:td
                   [:input#main-chat-button {:type "button" :value "　送　　出　"}] [:br]
                   [:select#main-chat-select {:name "chat-type"}
                    [:option {:value "chat20 red"} "強勢發言(紅)"]
                    [:option {:value "chat20"} "強勢發言"]
                    [:option {:value "chat16"} "稍強發言"]
                    [:option {:value "chat12" :selected true} "普通發言"]
                    [:option {:value "chat8"} "小聲發言"]
                    [:option {:value "chat8 blue"} "小聲發言(藍)"]
                    [:option {:value "chat8 green"} "小聲發言(綠)"]]]]]])
             [:table#main-chat-table {:class "chat-table main-chat-table"}
              [:tr
               [:td] [:td]]]]]]]
         [:div#tabs-game-list
          [:p
           [:fieldset
            [:legend "村莊一覽(Game List)"]
            [:div.game-list
             [:div#game-list-table {:class "game-list-table" :width "100%"}
              [:br] [:br]]]]]]
         [:div#tabs-create-game
          [:p
           [:fieldset
            [:legend "建立村莊(Create Game)"]
            [:div.create-game
             [:form
              [:table
               [:tr [:td "　村莊名稱："]
                [:td [:input#gamename {:type "text" :name "gamename" :size "40" :maxlength "30"}] "村"]]
               [:tr [:td "　村莊說明："]
                [:td [:input#description {:type "text" :name "description" :size "50" :maxlength "40"}]]]
               [:tr [:td "　村莊人數："]
                [:td [:select#maxplayer {:name "maxplayer"}
                    [:option {:value "9" :selected true} "簡易9人場"]
                 ]]]
               [:tr [:td "　限時時間："]
                [:td [:input#roundtime {:type "text" :name "roundtime" :size "3" :maxlength "3" :value "180"}] "秒／回合" ]]
               [:tr [:td "　測試模式："]
                [:td [:input#testmode {:type "checkbox" :name "testmode"}] "(打勾後開啟測試模式)" ]]
               [:tr [:td "　希望角色："]
                [:td [:input#wishrole {:type "checkbox" :name "wishrole"}] "(打勾後將可選擇希望角色)" ]]
               (when (not (empty? jinrouuser))
                 [:tr [:td.center
                        [:img#captcha {:src "./captcha", :alt ""} ]]
                      [:td
                        [:input#captcha-input {:type "text" :name "captcha" :value "" :size "40" :maxlength "30"}]
                        [:span.small "(驗證碼)"]
                        [:a#reload-captcha {:href "#"} [:font {:color "blue"} "重新產生"]]]])
               [:tr [:td ]
                [:td (when (not (empty? jinrouuser))
                       [:input#create-game-button {:type "button" :name "create-game-button" :value "　建　立　村　莊　"}])  ]]]]]]]]]]]]
     (unordered-list errmsgs)
     [:div.footer "Copyright Plummtw 2010"]]))

;[:head
;      [:script {:id "main", :src "js/main.js", :type "text/javascript"} ]
;      [:link {:rel "stylesheet", :href "css/main.css"} ]]


(defn game-view
  []  (jinrou-domi-view {:view (game-div) :js "game" :css "game"}))


