
(ns jinrou-clojure.view.main-view
  (:refer-clojure)
  (:use hiccup.core
    hiccup.page-helpers
    sandbar.stateful-session
    ring.util.response
    jinrou-clojure.util
    jinrou-clojure.util.captcha
    jinrou-clojure.util.date-util
    jinrou-clojure.model.jinrouuser
    jinrou-clojure.model.jinroudomi
    jinrou-clojure.view.baseview
    jinrou-clojure.view.jinrouuser-view
    jinrou-clojure.properties)
  (:import (java.util Date)))

(defn menu-date-format [#^Date date]
  (let [date (cdate-bean date)
        builder (StringBuilder. "")]
    (doto builder
      (.append (:year date))
      (.append "年")
      (.append (:month date))
      (.append "月")
      (.append (:day date))
      (.append "日"))
    (.toString builder)))

(defn main-div []
  (let [jinrouuser (current-jinrouuser)
        errmsgs (flash-message)]
    [:div#main
     [:div.server-name "人狼爭霸－鸚鵡實驗站"]
     [:noscript "請開啟JavaScript(Please turn javascript on)"]
     [:table.main
      [:tr
       [:td.menu-td
        [:div.menu "居民"]
        [:form#menu-form {:method "POST" :action "menu"}
         (if (not (empty? jinrouuser))
           [:ul.menu-item
            [:li "帳號："
             [:span (:uname jinrouuser)]]
            [:li "暱稱："
             [:span (:handle-name jinrouuser)]]
            [:li "上次："
             [:span (menu-date-format (:last-login jinrouuser))]]
            [:li.center
             [:input {:type "hidden" :name "action" :value "logout"}]
             [:a#logout-button {:href "#"} "登出"] "　"
             [:a {:href "jinrouuser"} "管理"]]]
           ;else
           [:table
            [:tr
             [:td.right "帳號："]
             [:td
              [:input {:type "text" :name "uname" :size "10" :maxlength "20"}]]
             [:tr
              [:td.right "trip："]
              [:td
               [:input {:type "text" :name "trip" :size "10" :maxlength "20"}]]]
             [:tr
              [:td.right "密碼："]
              [:td
               [:input {:type "password" :name "password" :size "10" :maxlength "20"}]]]
             [:tr
              [:td.center {:colspan "2"}
               [:input {:type "hidden" :name "action" :value "login"}]
               [:a#login-button {:href "#"} "登入"] "　"
               [:a {:href "jinrouuser"} "註冊"]]]]])]
        [:div.menu "選單"]
        [:ul.menu-item
         [:li
          [:a {:href "intro"} "製作源由"] "　"
          [:a {:href "version"} "版本更新"]]
         [:li
          [:a {:href "structure"} "遊戲架構"] "　"
          [:a {:href "rule"} "遊戲規則"]]
         [:li
          [:a {:href "usericon"} "頭像一覽"] "　"
          [:a {:href "usericon_upload"} "頭像上傳"]]
         [:li ]]
        [:div.menu "外部連結"]
        [:ul.menu-item ]]
       [:td
        [:div#tabs
         [:ul
          [:li
           [:a#tabs-info-a {:href "#tabs-info"} "伺服器公告"]]
          [:li
           [:a#tabs-main-chat-a {:href "#tabs-main-chat"} "主聊天視窗"]]
          [:li
           [:a#tabs-game-list-a {:href "#tabs-game-list"} "村莊一覽"]]
          [:li
           [:a#tabs-create-game-a {:href "#tabs-create-game"} "建立村莊"]]]
         [:br ]
         [:div#tabs-info
          [:p
           [:fieldset
            [:legend "伺服器公告(Server Information)"]
            [:div.information
             [:h2 "▉版本 Ver. 0.0.0.1 （Alpha） Build0712"]
             [:h1 "▉公告"]"註冊玩家擁有額外功能：建立村莊、使用主聊天視窗、上傳圖片等。"
             [:h3 "本伺服器M群：group576934@msnzone.cn"]
             [:h4 "製作者："
              [:span.parrot "鸚鵡"] "　"
              [:span.godfat "哥德法"]]]]]]
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


(defn main-view
  []  (jinrou-domi-view {:view (main-div) :js "main" :css "main"}))
;([errmsgs] (jinrou-domi-view {:view (main-div errmsgs) :js "main" :css "main"})))

(defn handle-menu [params]
  (let [action (get params "action")]
    (condp = action
      "login"  (handle-jinrouuser-login params)
      "logout" (handle-jinrouuser-logout)
      :else    (main-view))))

(defn ajax-create-game [params]
  ;(println (str params))
  (let [{:strs [gamename description maxplayer roundtime captcha]} params
        jinrouuser  (current-jinrouuser)
        jinroudomi  {:gamename gamename :description description :maxplayer (parse-int maxplayer)
                     :roundtime (parse-int roundtime)}
        msg-captcha (if (= captcha (captcha-answer)) nil
                      (str "驗證碼錯誤 答案：" (captcha-answer) " 你輸入：" captcha))
        msg-gamelimit  (if (< (count (valid-jinroudomi-list)) *jinroudomi-game-limit*) nil
                         (str "村莊已達上限：" *jinroudomi-game-limit* "村"))
        errmsgs      (concat (validate-jinroudomi jinroudomi)
                       (remove nil? (list msg-captcha msg-gamelimit)))]
    (if (not (empty? errmsgs))
      nil ;(do (append-flash-message errmsgs))
    ;else
      (locking *jinroudomi-lock*
        (let [id (inc (jinrou-clojure.model.jinroudomi/jinroudomi-count))]
          (insert-jinroudomi (assoc jinroudomi :id id) params))))
    (encode-json errmsgs)))

(defn ajax-game-list []
  ;(println (str params))
  (let [game-list (valid-jinroudomi-list)]
    (html (mapcat
      (fn [x] [[:strong [:img {:src (str "images/" (:status x) ".gif")} ]
                     [:a {:href (str "game?id=" (:id x) ) :color "blue"} [:small "[" (:id x) "號村]"] (:gamename x) "村"]
                    [:div {:align "right"}
                      [:small "～ " (:description x) " ～ 時間：" (:roundtime x) "秒／回合" "〔" (:maxplayer x) "人用〕"]] 
                [:span "村莊選項：" (map *jinroudomi-options-map* (:options x))]]]) game-list))))


