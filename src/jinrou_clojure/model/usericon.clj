
(ns jinrou-clojure.model.usericon
  (:refer-clojure)
  (:use somnium.congomongo
    jinrou-clojure.model.db
    jinrou-clojure.util)
  (:import (java.util Date)))

(def *usericon-lock* (Object.))

(defn validate-usericon
  "檢查usericon欄位，回傳錯誤List"
  [usericon]
  (remove nil? 
    (list
      ;(validate-exist usericon :jinrouuser_id)
      (validate-exist     usericon :group)
      (validate-number>=  usericon :group 0)
      (validate-exist     usericon :name "名稱")
      (validate-string-in usericon :name 1 20 "名稱")
      (validate-field     usericon :name !has-html-code "名稱包含控制碼")
      (validate-exist     usericon :filename)
      (validate-string-in usericon :filename 1 80)
      (validate-exist     usericon :width "寬度" )
      (validate-number-in usericon :width 20 50 "寬度")
      (validate-exist     usericon :height "長度" )
      (validate-number-in usericon :height 20 50 "長度")
      (validate-exist     usericon :color "顏色")
      (validate-field     usericon :color
        #(re-find #"^#[0-9A-f]{6}$" (str %)) "顏色格式錯誤"))))

(defn usericons 
  ([] (fetch :usericon))
  ([offset limit] (fetch :usericon :skip offset :limit limit)))

(defn usericons-count []
  (fetch-count :usericon))

(defn insert-usericon [usericon]
  (insert! :usericon (merge usericon {:created (Date.) })))

(defn usericon-by-id [id]
  (fetch-by-id :usericon id))

; 建立初期的 UserIcon
(when (empty? (fetch-one :usericon))
  (doseq [default-usericon [["明灰",   "001.gif", "#DDDDDD", 1]
                            ["暗灰",   "002.gif", "#999999", 2]
                            ["黄色",   "003.gif", "#FFD700", 3]
                            ["橘色",   "004.gif", "#FF9900", 4]
                            ["紅色",   "005.gif", "#FF0000", 5]
                            ["水色",   "006.gif", "#99CCFF", 6]
                            ["青",     "007.gif", "#0066FF", 7]
                            ["緑",     "008.gif", "#00EE00", 8]
                            ["紫",     "009.gif", "#CC00CC", 9]
                            ["櫻花色", "010.gif", "#FF9999",10]]]
    (insert-usericon {:group 0 :name (first default-usericon) :width 45 :height 45
                      :filename (str "default_usericons/" (second default-usericon))
                      :color (nth default-usericon 2) :id (nth default-usericon 3)})))

(in-ns 'user)
(use 'jinrou-clojure.model.usericon)