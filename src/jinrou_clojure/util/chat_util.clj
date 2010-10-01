
(ns jinrou-clojure.util.chat-util
  (:refer-clojure)
  (:use hiccup.core
    jinrou-clojure.model.jinrouuser
    jinrou-clojure.util)
  (:import (java.util Date)))

;(def *main-chat-lock* (Object.))
(def *main-chat-buffer* (ref '()))

(defn main-chat []
  (apply str (mapcat (fn [x]
                       (html [:tr [:td.chat-user-name [:font {:style "color:#990000"} "◆"] (:handle-name x)]
                              [:td {:class (:type x)} (:text x)]]))
               @*main-chat-buffer*)))
;<tr class="user-talk">
;<td class="user-name"><font style="color:#990000">☻</font>Sze</td>
;<td class="say normal">「很囧的說XD」</td>
;</tr>

(def *chat-limit* 50)
(defn process-chat [chat-list chat]
  (take *chat-limit*
    (cons chat chat-list)))

(defn ajax-main-chat [{:strs [text type]}]
  (let [jinrouuser (current-jinrouuser)]
    (when (and (not (empty? jinrouuser))
            (not (blank? text))
            (not (blank? type)))
      (let [text (cut-string text 200)]
        (dosync
          (alter *main-chat-buffer* process-chat 
            {:jinrouuser_id (:_id jinrouuser) :handle-name (:handle-name jinrouuser)
             :text (html-encode text) :type type :created (Date.)}))))
    ;(println "A")
    (main-chat)))
