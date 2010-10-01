
(ns jinrou-clojure.model.jinrouuser-login
  (:refer-clojure)
  (:use somnium.congomongo
    jinrou-clojure.model.db
    jinrou-clojure.util)
  (:import (java.util Date)))

(def *jinrouuser-login-type-map* {"login" "登入" "logout" "登出" "login-fail" "登入失敗"})


(defn check-jinrouuser-login-fail [uname ip-address date]
  (concat (fetch :jinrouuser :where {:uname uname :login-type "login-fail"
                                     :created {:$gt date}})
    (fetch :jinrouuser :where {:ip-address ip-address :login-type "login-fail"
                                     :created {:$gt date}})))
            
(defn create-jinrouuser-login
  [id uname login-type]
  {:pre [(contains? #{"login" "logout" "login-fail"} login-type)]}
  (insert! :jinrouuser-login
    {:jinrouuser-id id :uname uname :login-type login-type
     :created (Date. ) :ip-address (get-ip-address)}))