
(ns jinrou-clojure.model.jinroudomi
  (:refer-clojure)
  (:use somnium.congomongo
    sandbar.stateful-session
    jinrou-clojure.model.db
    jinrou-clojure.model.jinrouuser
    jinrou-clojure.util)
  (:import (java.util Date)))

(def *jinroudomi-lock* (Object.))

(def *current-jinroudomi-session-key* "current-jinroudomi")
(defn current-jinroudomi []
  (session-get *current-jinroudomi-session-key*))

(defn current-jinroudomi-set! [jinroudomi]
  (session-put! *current-jinroudomi-session-key* jinroudomi))

(defn jinroudomi-by-id [id]
  (fetch-by-id :jinroudomi id))

(defn valid-jinroudomi-list []
  (fetch :jinroudomi :where {:status {:$in ["waiting", "playing" ]}}))

(defn jinroudomi-count []
  (fetch-count :jinroudomi))

(defn validate-jinroudomi
  "檢查jinroudomi欄位，回傳錯誤List"
  [jinroudomi]
  (remove nil?
    (list
      (validate-exist     jinroudomi :gamename "村莊名稱")
      (validate-string-in jinroudomi :gamename 1 30 "村莊名稱")
      (validate-exist     jinroudomi :description "村莊說明")
      (validate-string-in jinroudomi :description 1 80 "村莊說明")
      (validate-exist     jinroudomi :maxplayer "村莊人數")
      (validate-number-in jinroudomi :maxplayer 9 9 "村莊人數")
      (validate-exist     jinroudomi :roundtime "回合時間")
      (validate-number-in jinroudomi :roundtime 60 999 "回合時間"))))

(def *jinroudomi-options-map* {"testmode" "[測]"
                               "wishrole" "[希]"})


(defn insert-jinroudomi [jinroudomi options]
  (let [jinrouuser (current-jinrouuser)
        options (filter #(= (options %) "on") (keys *jinroudomi-options-map*))
        date (Date.)]
    ;(info (str "jinrouuser - insert : " jinrouuser))
    (insert! :jinroudomi (merge jinroudomi {:created date :jinrouuser_id (:_id jinrouuser)
                                            :created-ip (get-ip-address) :status "waiting"
                                            :options options}))))



