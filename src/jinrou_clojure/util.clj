
(ns jinrou-clojure.util
  (:refer-clojure)
  (:use clojure.xml
        ring.util.codec
        jinrou-clojure.properties)
  (:import (org.apache.commons.codec.digest DigestUtils)))

(defn parse-xml-file [file-name]
  (parse (java.io.File. file-name)))

(declare xml2sexp)

(defn content2sexp [depth content]
  (if (map? content)
    (xml2sexp content (inc depth))
    ;else
    (str "\"" (.trim (str content)) "\"" )))

(defn xml2sexp
  ([xml] (xml2sexp xml 1))
  ([xml depth]
    (str "\n"(apply str (repeat depth "  "))
      "[" (:tag xml) " " (when-let [attrs (:attrs xml)] (str attrs " "))
      (apply str (map (partial content2sexp depth) (:content xml)))
      "]")))

(def *html-encode-hash* {
  \& "&amp;"
  \< "&lt;"
  \> "&gt;"
  \" "&quot;"
  \return "<br/>"
  \newline "<br/>"})

(defn has-html-code [#^String string]
  (some *html-encode-hash* string))

(defn !has-html-code [#^String string]
  (not (has-html-code string)))

(defn html-encode [#^String string]
  (let [string-builder (StringBuilder.)
        count          (count string)]
    (loop [i 0 escape-r false]
      (if (>= i count)
        (.toString string-builder)
      ;else
        (let [c (nth string i)
              h (get *html-encode-hash* c c)]
           (if (and escape-r (= c \newline))
             (recur (inc i) false)
           ;else
             (do
               (.append string-builder h)
               (recur (inc i) (= c \return)))))))))

(defn lookup [hash x]
  "於Hash中找出第一個Value對應之Key值"
  (loop [seq-hash (seq hash)]
    (let [[f & r] seq-hash]
      (cond
        (nil? f) nil
        (= (second f) x) (first f)
        :else (recur r)))))

(defn to-list 
  "將非集合轉為list"
  [x]
  (if (or (nil? x) (coll? x)) x
  ;else
  (list x)))

(defn parse-int
  ([#^String number] (parse-int number 0))
  ([#^String number default]
    (try (Integer/parseInt number)
    (catch Exception _ default))))

(defn blank?
  "是否為nil、非字串、或空白字串"
  [#^String string]
  (or (nil? string)
    (not (string? string))
    (=  (.trim string) "")))

(defn cut-string
  "將字串斬至指定長度"
  [#^String string length]
  (if (not (string? string))
    string
  ;else
    (let [max-length (min (.length string) length)]
      (.substring string 0 max-length))))


(defn rz 
  "將字串左方補0，直到長度達到指定長度"
  [#^String string length]
  (str (apply str (repeat (- length (count string)) "0")) string))

(defn get-ip-address []
  (and *request*
    (let [available? (fn [x] (not (or (nil? x) (= x "") (= x "unknown"))))
          headers    (:headers *request*)]
      (cond
        (available? (headers "X-Forwarded-For")) (headers "X-Forwarded-For")
        (available? (headers "Proxy-Client-IP")) (headers "Proxy-Client-IP")
        (available? (headers "WL-Proxy-Client-IP")) (headers "WL-Proxy-Client-IP")
        :else       (:remote-addr *request*)))))

(defn sha1-base64-encode [#^String string]
  (base64-encode (.getBytes (DigestUtils/shaHex string))))

(defn user-trip [#^String string]
  (.substring (sha1-base64-encode string) 1 9))

(defn encode-password [#^String string]
  (.substring (sha1-base64-encode string) 0 20))

(declare encode-json)
(defn- encode-json-coll [coll]
  (str "[" (apply str (interpose "," (map encode-json coll))) "]"))

(defn- encode-json-map [hash]
  (let [encode-json-2 (fn [[x y]]
                           (str (encode-json x) ":" (encode-json y)))]
    (str "{" (apply str (interpose "," (map encode-json-2 (seq hash)))) "}")))

(defn encode-json [data]
  (cond
    (map?  data) (encode-json-map  data)
    (coll? data) (encode-json-coll data)
    (string? data) (str "\"" data "\"")
    (keyword? data) (str "\"" (name data) "\"")
    (symbol? data) (str "\"" (name data) "\"")
    :else          (str data)))

(in-ns 'user)
(use 'jinrou-clojure.util)