
(ns jinrou-clojure.util.date-util
  (:refer-clojure)
  (:import (java.util Date Calendar)))

(defn get-calendar [#^Date date]
  (let [calendar (Calendar/getInstance)]
    (.setTime calendar date)
    calendar))

(defn add-date [#^Date date kind value]
  (let [calendar (get-calendar date)]
    (.add calendar kind value)
    (.getTime calendar)))

(defn date-bean [#^Date date]
  (let [calendar (get-calendar date)
        year  (.get calendar Calendar/YEAR)
        month (inc (.get calendar Calendar/MONTH))
        day   (.get calendar Calendar/DAY_OF_MONTH)
        hour  (.get calendar Calendar/HOUR_OF_DAY)
        minute  (.get calendar Calendar/MINUTE)
        second  (.get calendar Calendar/SECOND)]
    {:year year :month month :day day
     :hour hour :minute minute :second second}))

(defn cdate-bean [#^Date date]
  (let [calendar (get-calendar date)
        year  (- (.get calendar Calendar/YEAR) 1911)
        month (inc (.get calendar Calendar/MONTH))
        day   (.get calendar Calendar/DAY_OF_MONTH)
        hour  (.get calendar Calendar/HOUR_OF_DAY)
        minute  (.get calendar Calendar/MINUTE)
        second  (.get calendar Calendar/SECOND)]
    {:year year :month month :day day
     :hour hour :minute minute :second second}))
