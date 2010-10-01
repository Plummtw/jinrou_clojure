
(ns jinrou-clojure.properties
  (:refer-clojure)
  (:use somnium.congomongo
        sandbar.stateful-session))

(def *mongo-db* "jinrou-clojure")
(def *jinroudomi-game-limit* 3)

(def *request* {})

#_(def *messages*  [])
; 註： *messages* 必須已 bind
#_(defn append-message [message]
  (cond
    (nil? message)  *messages*
    (coll? message) (set! *messages* (concat *messages* message))
    :else           (set! *messages* (conj *messages* message))))

(def *flash-session-key* "flash")
(defn append-flash-message [message]
  (let [flash (session-get *flash-session-key* [])
        new-flash (cond
                    (nil? message)  flash
                    (coll? message) (concat flash message)
                    :else           (conj flash message))]
    (session-put! *flash-session-key* new-flash)))

(defn flash-message []
  (let [flash (session-get *flash-session-key* [])]
    (session-put! *flash-session-key* nil)
    flash))






