
(ns jinrou-clojure.controller
  (:refer-clojure)
  (:use [clojure.contrib.def :only [defonce-]]
        clojure.contrib.logging
        compojure.core
        ring.adapter.jetty
        ;ring.middleware.session
        sandbar.stateful-session
        ring.middleware.stacktrace
        ;ring.middleware.flash
        ;ring.middleware.multipart-params
        ring.util.response
        hiccup.core
        jinrou-clojure.properties
        jinrou-clojure.view.main-view
        jinrou-clojure.view.jinrouuser-view
        jinrou-clojure.view.intro-view
        jinrou-clojure.view.version-view
        jinrou-clojure.view.structure-view
        jinrou-clojure.view.rule-view
        jinrou-clojure.view.usericon-view
        jinrou-clojure.view.usericon-upload-view
        jinrou-clojure.util
        jinrou-clojure.util.captcha
        jinrou-clojure.util.chat-util
        [jinrou-clojure.army :as army])
  (:require [compojure.route :as route]))

(defn wrap-charset [handler charset]
  (fn [request]
    (if-let [response (handler request)]
      (if-let [content-type (get-in response [:headers "Content-Type"])]
        (if (.contains content-type "charset")
          response
          (assoc-in response
            [:headers "Content-Type"]
            (str content-type "; charset=" charset)))
        response))))

(defn wrap-request [handler]
  (fn [request]
    (binding [*request* request]
      ;(info (str (:param request)))
      (handler request))))

(defroutes *routes*
; index
  (GET  "/index"  [] "<h1>Hello World Wide Web 成功!</h1>") ;(index-html session))

; main
  (GET  "/main" [] (main-view))
  (POST "/menu" {params :params} (handle-menu params))
  (GET  "/main-chat" [] (main-chat))
  (POST "/main-chat" {params :params} (ajax-main-chat params))
  (POST "/create-game" {params :params} (ajax-create-game params))
  (GET  "/game-list"   {params :params} (ajax-game-list))

  (GET  "/jinrouuser" [] (jinrouuser-view))
  (POST "/jinrouuser" {params :params} (handle-jinrouuser params))

; menu
  (GET  "/intro"    [] (intro-view))
  (GET  "/version"  [] (version-view))
  (GET  "/structure" [] (structure-view))
  (GET  "/rule"     [] (rule-view))
  (GET  "/usericon" {params :params} (usericon-view params))
  (GET  "/usericon_upload" [] (usericon-upload-view))
  (POST "/usericon_upload" request (handle-usericon-upload request))

  (GET  "/captcha"  []  (captcha-view))

; 固定檔案
  (route/files "/"  {:root "./public"})

; 轉址
  (GET  "/"    []    (redirect "/index"))
  (GET  "/index.html" [] (redirect "/index"))
  (GET  "/index.htm"  [] (redirect "/index"))

; 測試用
  (GET "/test" []   (html [:h1 "中文"]))
  (GET "/request" request (str request))
  (GET "/ipaddress" [] (get-ip-address))

  (GET  "/army" [] (army/army-view nil nil))
  (POST "/army" {params :params} (army/handle-view params))
;  (GET  "/index.html" (redirect-to "/index"))
;  (GET  "/index.htm"  (redirect-to "/index"))
;  (GET  "/"           (redirect-to "/index"))
;  (ANY  "*"           404)
)

;;======== Code below handles paths when running as a servlet ==================
;; This caches the context path of the servlet
(def context-path (atom nil))

(defn- get-context-path
  "Returns the context path when running as a servlet"
  ([] @context-path)
  ([servlet-req]
    (if (nil? @context-path)
      (reset! context-path (.getContextPath servlet-req)))
    @context-path))

(defn- wrap-context
  "Removes the deployed servlet context from a URI when running as a   deployed web application"
  [handler]
  (fn [request]
    (if-let [servlet-req (:servlet-request request)]
      (let [context (get-context-path servlet-req)
            uri (:uri request)]
        (if (.startsWith uri context)
          (handler (assoc request :uri
                     (.substring uri (.length context))))
          (handler request)))
      (handler request))))

(wrap! *routes* :context)

;
;  Server 設定區
;

(defonce- *server* (ref nil))

(defn stop-server []
  (when @*server*
    (.stop @*server*)))

(defn start-server []
  (dosync (ref-set  *server*  
            (run-jetty (-> #'*routes*
                         wrap-request
                         wrap-stacktrace
                         wrap-stateful-session
                         ;(wrap-multipart-params :encoding "utf16")
                         ;wrap-flash
                         (wrap-charset "utf-8"))
              {:port 8081 :join? false} ))))

(defn restart-server []
  (stop-server)
  (start-server))



(in-ns 'user)
(use 'jinrou-clojure.controller)
