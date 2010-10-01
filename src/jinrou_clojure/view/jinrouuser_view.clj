
(ns jinrou-clojure.view.jinrouuser-view
  (:refer-clojure)
  (:use hiccup.core
    hiccup.form-helpers
    hiccup.page-helpers
    ring.util.response
    jinrou-clojure.util
    jinrou-clojure.util.date-util
    jinrou-clojure.util.captcha
    jinrou-clojure.view.baseview
    jinrou-clojure.view.usericon-view
    jinrou-clojure.model.jinrouuser
    jinrou-clojure.model.jinrouuser-login
    jinrou-clojure.properties)
  (:import (java.util Date Calendar)))

(defn jinrouuser-div [errmsgs params]
  (let [errmsgs (to-list errmsgs)
        jinrouuser (current-jinrouuser)
        params     (or jinrouuser params)
        params     (if (nil? (:sex params))
                     (assoc params :sex "M")
                     params)]
  [:div#jinrouuser
    [:form {:method "POST" :action "jinrouuser"}
      [:div {:class "jinrouuser manage"}
        [:fieldset
          [:legend "居民資料"]
          [:h5 "　註冊玩家擁有額外功能：建立村莊、使用主聊天視窗、上傳圖片等。"]
          #_[:h6 "　註冊成功後會跳回主畫面。"]
          [:table
            [:tr
              [:td
                [:table
                  [:tr
                    [:td.center "帳號名稱"]
                    [:td
                       (if jinrouuser [:span (:uname params)]
                         [:input {:type "text" :name "uname" :value (:uname params) :size "40" :maxlength "30"}])]
                    [:td
                      [:span.small "登入用的「帳號名稱」"]]]
                  [:tr
                    [:td.center "Trip"]
                    [:td
                      [:input {:type "text" :name "trip" :value "" :size "40" :maxlength "30"}]]
                    [:td
                      [:span.small ]]]
                  [:tr
                    [:td.center "暱稱"]
                    [:td
                      [:input {:type "text" :name "handle-name" :value (:handle-name params) :size "40" :maxlength "30"}]]
                    [:td
                      [:span.small "在村中所表示的「暱稱」"]]]
                  [:tr
                    [:td.center "密碼"]
                    [:td
                      [:input {:type "password" :name "password" :value "" :size "40" :maxlength "30"}]]
                    [:td
                      [:span.small "登入所使用的密碼"]]]
                  [:tr
                    [:td.center "重新輸入密碼"]
                    [:td
                      [:input {:type "password" :name "repassword" :value "" :size "40" :maxlength "30"}]]
                    [:td
                      [:span.small "必須和密碼一致"]]]
                  [:tr
                    [:td.center "性別"]
                    [:td
                      (mapcat (fn [[x y]] [[:input {:type "radio" :name "sex" :value x :checked (= (:sex params) x)} ] [:span y]])
                        (seq *jinrouuser-sex-map*))]
                    [:td
                      [:span.small ]]]
                  [:tr
                    [:td.center "電子信箱"]
                    [:td
                      [:input#email {:type "text" :name "email" :value (:email params) :size "40" :maxlength "30"}]]
                    [:td
                      [:span.small ]]]
                  [:tr
                    [:td.center "MSN"]
                    [:td
                      [:input#msn {:type "text" :name "msn" :value (:msn params) :size "40" :maxlength "30"}]]
                    [:td
                      [:span.small
                        [:input#copy-email2msn {:type "button", :value "和信箱相同"} ]]]]
                  [:tr
                    [:td.center "星座"]
                    [:td
                      [:select#zodiac {:name "zodiac"}
                       (map (fn [x] [:option {:value x :selected (= (:zodiac params) x)} (*jinrouuser-zodiac-map* x)]) *jinrouuser-zodiac-list*)]]
                    [:td
                      [:span.small ]]]
                  [:tr
                    [:td.center "備註"]
                    [:td
                      [:textarea {:name "memo" :size "40" :rows "5" :cols "40" :wrap "soft"}  (:memo params) ]]
                    [:td
                      [:span.small "(自我介紹、經驗專長、其他常用暱稱及trip、遊戲展望等)"]]]
                  [:tr
                    [:td.center
                      [:img#captcha {:src "./captcha", :alt ""} ]]
                    [:td
                      [:input {:type "text" :name "captcha" :value "" :size "40" :maxlength "30"}]]
                    [:td
                      [:span.small "(驗證碼)"]
                      [:a#reload-captcha {:href "#"} "重新產生"]]]
                  [:tr
                   [:td.center {:colspan "3"}
                    (usericon-ajax-div nil)
                    ]
                   ]
                  [:tr
                    [:td.center {:colspan "3"}
                      [:input {:type "submit" :value (if (not (empty? jinrouuser)) "　修　　改　" "　建　　立　")}]]]]]]
            [:tr
              [:td
                (unordered-list errmsgs) ]]
            [:tr
              [:td
                [:br ]]]]]]]]))

(defn jinrouuser-view 
  ([]               (sub-view {:view (jinrouuser-div nil nil) :js "jinrouuser"}))
  ([errmsgs params] (sub-view {:view (jinrouuser-div errmsgs params) :js "jinrouuser"})))

(defn handle-jinrouuser [params]
  (let [{:strs [uname trip handle-name password repassword sex email msn zodiac
                memo captcha usericon_id]} params
        jinrouuser  (merge (or (current-jinrouuser) {:uname uname })
                      {:trip trip :handle-name handle-name :password password
                       :sex sex :email email :msn msn :zodiac zodiac :memo memo
                       :usericon-id usericon_id})
        msg-captcha (if (= captcha (captcha-answer)) nil
                      (str "驗證碼錯誤 答案：" (captcha-answer) " 你輸入：" captcha))
        msg-password (if (= password repassword) nil
                       "密碼不一致")
        ; Note: 修改時 uname 不會傳入，所以帳號重複改檢查是否有 uname 為 nil
        msg-uname    (if (empty? (jinrouuser-by-uname uname)) nil
                       "帳號重複")
        errmsgs      (concat (validate-jinrouuser jinrouuser)
                       (remove nil? (list msg-captcha msg-password msg-uname)))]
    (if (not (empty? errmsgs))
      (jinrouuser-view errmsgs jinrouuser)
    ;else
      (let [jinrouuser (assoc jinrouuser :password (encode-password (:password jinrouuser)))
            jinrouuser (if (:trip jinrouuser)
                         (assoc jinrouuser :trip (user-trip (:trip jinrouuser)))
                         jinrouuser)]
        (if (empty? (current-jinrouuser))
          (insert-jinrouuser jinrouuser)
          (do (update-jinrouuser jinrouuser) (current-jinrouuser-set! jinrouuser)))
        (append-flash-message (if (empty? (current-jinrouuser)) "建立成功" "修改成功"))
        (redirect "main")))))

(defn handle-jinrouuser-login [params]
  (let [{:strs [uname trip password]} params
        ip-address (get-ip-address)
        date (add-date (Date.) Calendar/MINUTE -15)
        login-fails (check-jinrouuser-login-fail uname ip-address date)
        jinrouuser            (jinrouuser-auth uname trip password)
        errmsg (cond (>= (count login-fails) 5) "登入失敗錯誤超過安全限制，請於15分鐘之後再行登入"
                 (empty? jinrouuser) (do (jinrouuser-login-fail uname) "帳號、密碼或trip錯誤")
                 :else nil)]
    (if (not (empty? errmsg))
      (append-flash-message errmsg)
      (do (jinrouuser-login jinrouuser) (append-flash-message "登入成功")))
    (redirect "main")))

(defn handle-jinrouuser-logout []
  (let [jinrouuser (current-jinrouuser)]
    (when (not (empty? jinrouuser))
      (append-flash-message "登出成功")
      (jinrouuser-logout jinrouuser))
    (redirect "main")))

    
