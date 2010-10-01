
(ns jinrou-clojure.model.jinrouuser
  (:refer-clojure)
  (:use clojure.contrib.logging
    somnium.congomongo
    sandbar.stateful-session
    jinrou-clojure.model.db
    jinrou-clojure.model.jinrouuser-login
    jinrou-clojure.util)
  (:import (java.util Date)))

(def *current-jinrouuser-session-key* "current-jinrouuser")
(defn current-jinrouuser []
  (session-get *current-jinrouuser-session-key*))

(defn current-jinrouuser-set! [jinrouuser]
  (session-put! *current-jinrouuser-session-key* jinrouuser))

(defn jinrouuser-by-id [id]
  (fetch-by-id :jinrouuser id))

(defn jinrouuser-by-uname [uname]
  (fetch-one :jinrouuser :where {:uname uname}))

(defn jinrouuser-auth [uname trip password]
  (fetch-one :jinrouuser :where {:uname uname :trip (user-trip trip)
                                 :password (encode-password password)}))

(defn insert-jinrouuser [jinrouuser]
  (let [date (Date.)]
    ;(info (str "jinrouuser - insert : " jinrouuser))
    (insert! :jinrouuser (merge jinrouuser {:created date :last-login date :updated date
                                            :created-ip (get-ip-address)}))))

(defn update-jinrouuser [jinrouuser]
  (let [date (Date.)]
    ;(info (str "jinrouuser - update : " jinrouuser))
    (update! :jinrouuser jinrouuser (merge jinrouuser
                                      {:updated date :updated-ip (get-ip-address)}))))


(def *jinrouuser-sex-map* {"M" "男" "F" "女"})
(def *jinrouuser-zodiac-map* {"Ari" "牡羊座", "Tau" "金牛座", "Gem" "雙子座", "Can" "巨蟹座",
                            "Leo" "獅子座", "Vir" "處女座", "Lib" "天秤座", "Sco" "天蠍座",
                            "Sag" "射手座", "Cap" "魔羯座", "Aqu" "水瓶座", "Pis" "雙魚座"})
(def *jinrouuser-zodiac-list* ["Ari", "Tau", "Gem", "Can", "Leo", "Vir", "Lib", "Sco", "Sag", "Cap", "Aqu", "Pis"])

(defn validate-jinrouuser
  "檢查jinrouuser欄位，回傳錯誤List"
  [jinrouuser]
  (remove nil?
    (list
      ;(validate-exist usericon :jinrouuser_id)
      (validate-exist     jinrouuser :uname "帳號")
      (validate-string-in jinrouuser :uname 6 20 "帳號")
      (validate-field     jinrouuser :uname !has-html-code "帳號包含控制碼")
      (validate-exist     jinrouuser :handle-name "暱稱")
      (validate-string-in jinrouuser :handle-name 1 20 "暱稱")
      (validate-field     jinrouuser :handle-name !has-html-code "暱稱包含控制碼")
      (validate-exist     jinrouuser :trip "trip")
      (validate-string-in jinrouuser :trip  6 20 "trip")
      (validate-field     jinrouuser :trip  !has-html-code "trip包含控制碼")
      (validate-exist     jinrouuser :password "密碼")
      (validate-string-in jinrouuser :password  6 20 "密碼")
      (validate-field     jinrouuser :password  !has-html-code "密碼包含控制碼")
      (validate-exist     jinrouuser :sex   "性別")
      (validate-field     jinrouuser :sex   *jinrouuser-sex-map*  "性別格式錯誤")
      (validate-exist     jinrouuser :email "email")
      (validate-string<=  jinrouuser :email 80  "email")
      (validate-field     jinrouuser :email
        #(re-find #".+@.+\.[a-z]+" (str %)) "email格式錯誤")
      (validate-field     jinrouuser :email  !has-html-code "email包含控制碼")
      (validate-exist     jinrouuser :msn "msn")
      (validate-string<=  jinrouuser :msn 80 "msn")
      (validate-field     jinrouuser :msn
        #(re-find #".+@.+\.[a-z]+" (str %)) "msn格式錯誤")
      (validate-field     jinrouuser :msn  !has-html-code "msn包含控制碼")
      (validate-exist     jinrouuser :zodiac "星座")
      (validate-field     jinrouuser :zodiac    *jinrouuser-zodiac-map*  "星座格式錯誤")
      (validate-exist     jinrouuser :memo  "備註")
      (validate-string<=  jinrouuser :memo 3000 "備註"))))

(defn jinrouuser-login-fail [uname]
  (create-jinrouuser-login nil uname "login-fail"))

(defn jinrouuser-login [{:keys [_id uname] :as jinrouuser}]
  (let [updated (or (:updated jinrouuser) (:created jinrouuser))]
    (current-jinrouuser-set! jinrouuser)
    (update-jinrouuser (merge jinrouuser {:last-login updated}))
    (create-jinrouuser-login _id uname "login")))

(defn jinrouuser-logout [{:keys [_id uname]}]
  (current-jinrouuser-set! nil)
  (create-jinrouuser-login _id uname "logout"))
