
(ns jinrou-clojure.view.usericon-upload-view
  (:refer-clojure)
  (:use [clojure.contrib.def :only [defn-memo]]
    hiccup.core
    hiccup.page-helpers
    ;ring.middleware.multipart-params
    sandbar.stateful-session
    jinrou-clojure.util
    jinrou-clojure.util.multipart-params
    jinrou-clojure.view.baseview
    jinrou-clojure.model.jinrouuser
    jinrou-clojure.model.usericon
    jinrou-clojure.properties)
  (:import (java.io File FileInputStream FileOutputStream)
    (javax.imageio ImageIO)))

(def *usercolor-per-line* 6)
(def *usercolor-seqs* ["00" "33" "66" "99" "CC" "FF"])

(def *usercolors*
  (for [x *usercolor-seqs* y *usercolor-seqs* z *usercolor-seqs*] (str "#" x y z)))

(defn- usercolor-td [usercolor]
  [:td {:bgcolor usercolor}
   [:label {:for usercolor}
    [:input {:type "radio", :id usercolor, :name "color", :value usercolor} ]
    [:font {:color "#FFFFFF"} usercolor]]])

(defn- usercolor-tr [usercolors]
  [:tr (map usercolor-td usercolors)])

(defn-memo usercolor-table []
  [:table.color {:align "center"} (map usercolor-tr (partition *usercolor-per-line* *usercolors*))])

(defn usericon-upload-div [errmsgs]
  (let [jinrouuser (current-jinrouuser)
        errmsgs (to-list errmsgs)]
    [:div#usericon-upload
     [:table {:align "center"}
      [:tr
       [:td.link
        [:a {:href "usericon"} "→頭像一覽"]]]
      [:tr
       [:td.caution "＊頭像大小限制 (寬45pm × 高45pm) 。"]]
      [:tr
       [:td
        (unordered-list errmsgs) ]]
      [:tr
       [:td
        [:fieldset
         [:legend "頭像上傳 (jpg, gif, png 格式。15kByte )"]
         [:form {:method "POST" :enctype "multipart/form-data" :action "usericon_upload"}
          [:table
           [:tr
            [:td
             [:label "頭像選擇"]]
            [:td
             [:input {:type "file" :name "uploaded_file" :size "60"}]
             (when (not (empty? jinrouuser))
               [:input {:type "submit" :value "上傳"}])]]
           [:tr
            [:td
             [:label "頭像名稱"]]
            [:td
             [:input {:type "text" :name "name" :size "20" :maxlength "20"} ] "20字以內。"]]
           [:tr
            [:td
             [:label "邊框顏色"]]
            [:td
             [:input#fix-color {:type "radio", :name "color", :checked "true"} ]
             [:label {:for "fix-color"} "自行輸入"]
             [:input {:type "text", :name "color0", :size "10px", :maxlength "7"} ] "(例：#6699CC)"]]
           [:tr
            [:td {:colspan "2"}
             (usercolor-table)]]]]]]]]]))

(defn usericon-upload-view 
  ([]        (sub-view (usericon-upload-div nil)))
  ([errmsgs] (sub-view (usericon-upload-div errmsgs))))

; Warning 存檔名稱為 010.png, 其中 010 為資料庫目前筆數+1
(defn save-usericon-file [group name color tempfile]
  (locking *usericon-lock*
    (try
      (let [jinrouuser  (current-jinrouuser)
            imagefile   (ImageIO/read (FileInputStream. tempfile))
            width       (.getWidth  imagefile)
            height      (.getHeight imagefile)
            id          (inc (usericons-count))
            filename    (str "upload/" (rz (str id) 3) ".png")
            outname     (str "public/" filename)
            usericon    {:group group :name name :width width :height height
                         :color color :filename filename :id id :jinrouuser_id (:_id jinrouuser)}]
        ;先存檔再存資料庫
        (ImageIO/write imagefile "png" (FileOutputStream. outname))

        ;存資料庫
        (let [errmsgs (validate-usericon usericon)]
          (if (not (empty? errmsgs))
            errmsgs
          ;else
            (do
              (insert-usericon usericon)
              nil))))
      (catch Exception ex (list "檔案存檔錯誤" (.getMessage ex))))))

; {"color0" "dfg", "color" "on", "uploaded_file" {:filename "", :size 0, :content-type "application/octet-stream", :tempfile #}} 
; color == on 時取 color0, 否則取 color
; 如果無檔案上傳, 則檔案大小為 0
(defn handle-usericon-upload [request]
  (let [jinrouuser (current-jinrouuser)
        {:strs [name color0 color uploaded_file]} (parse-multipart-params request "utf-8")
        _ (println name)
        {:keys [filename size content-type tempfile]} uploaded_file
        color  (if (= color "on") color0 color)
        errmsg (cond
                 (empty? jinrouuser) "尚未登入"
                 (= size 0) "上傳檔案為空白"
                 (not (string? content-type)) "無法辨識上傳檔案格式"
                 (not (.startsWith content-type "image/")) "上傳檔案格式非圖檔"
                 :else nil)]
    (if errmsg
      (usericon-upload-view errmsg) ; 檔案不對就直接錯誤訊息跳出了
    ;else
      (let [errmsgs (save-usericon-file 1 name color tempfile)]
        (if (not (empty? errmsgs))
          (usericon-upload-view errmsgs)
        ;else
          (usericon-upload-view "檔案上傳成功"))))))
