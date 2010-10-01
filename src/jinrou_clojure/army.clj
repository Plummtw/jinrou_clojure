
(ns jinrou-clojure.army
  (:refer-clojure)
  (:use hiccup.core
    hiccup.form-helpers
    jinrou-clojure.util))

(defrecord army [name cname attack life speed])
(def archer   (army. 'archer "弓兵" 3 2 6))
(def pikeman  (army. 'pikeman "槍兵" 4 3 5))
(def cavalry  (army. 'cavalry "騎兵" 5 4 4))
(def halberd  (army. 'halberd "戟兵" 6 5 3))
(def infantry (army. 'infantry "輕步" 7 6 2))
(def heavy    (army. 'heavy "重步" 8 7 1))
(def mage     (army. 'mage "法師" 0 1 1))
(def cleric   (army. 'cleric "牧師" 2 3 1))

(defn army-eval [string]
  (condp = string
    "archer" archer
    "pikeman" pikeman
    "cavalry" cavalry
    "halberd" halberd
    "infantry" infantry
    "heavy" heavy
    "mage" mage
    "cleric" cleric))



; 顯示順位
(def armies-by-view [archer pikeman cavalry halberd infantry heavy mage cleric])
; 防禦順位
(def armies-by-def  [heavy infantry halberd cavalry pikeman archer mage cleric])


; 測試用資料
(def army-ga {archer 3  cavalry  3  infantry 3 cleric 10})
(def army-gb {cavalry 5 infantry 4})
(def army-gc {heavy 5 cleric 4})
(def aa      {archer 1 pikeman 1 cavalry 1 halberd 1 infantry 1 heavy 1 mage 1 cleric 1})

(defn get-army [army-group army-type]
  (get army-group army-type 0))

(defn get-army-ws [army-group army-type]
  (let [result (get-army army-group army-type)]
    (if (= 0 result)
      ""
      result)))

(defn total-army [army-group]
  (reduce + 0 (map second army-group)))

(defn army-damage [army-group speed]
  (let [mages    (get-army army-group mage)
        army-seq (filter #(= (-> % first :speed) speed) (seq army-group))]
    (+ (reduce #(+ %1 (* (:attack (first %2)) (second %2))) 0 army-seq) mages)))

(defn army-suffer- [{:keys [army-group damage] :as result} army-type]
  (if (<= damage 0)
    result
    (let [life            (:life army-type)
          num             (get-army army-group army-type)
          damage-received (min damage (* num life))
          army-left       (- num (quot damage-received life))
          damage-left     (- damage damage-received)]
      {:army-group (merge army-group {army-type army-left}) :damage damage-left})))

(defn army-suffer [army-group damage]
  (let [clerics (get-army army-group cleric)
        damage (- damage (* 2 clerics))]
    (loop [{:keys [army-group damage] :as result} {:army-group army-group :damage damage}]
      ;(println result)
      (if (or (<= damage 0)
            (= (total-army army-group) 0))
        result
        (recur (reduce army-suffer- result armies-by-def))))))

(defn print-army [army-group]
  (doseq [[{name :name} number] (seq army-group)]
    (when (not= number 0)
      (printf "%s %d " name number)))
  (println ""))

(defn format-result [army-group1 army-group2]
  (let [result    (cond
                    (and (= (total-army army-group1) 0)
                      (= (total-army army-group2) 0)) "雙方皆全滅"
                    (= (total-army army-group1) 0) "軍團 2 獲勝"
                    (= (total-army army-group2) 0) "軍團 1 獲勝"
                    :else                          "無法分出勝負")]
    #_(println result)
    #_(println "Army-group 1")
    #_(print-army army-group1)
    #_(println "Army-group 2")
    #_(print-army army-group2)
    {:result result :army-group1 army-group1 :army-group2 army-group2}))

(defn army-fight [army-group1 army-group2]
  (loop [army-group1 army-group1 army-group2 army-group2 speed 7
         old-group1  army-group1 old-group2  army-group2]
    ;(println speed)
    (if (or (= (total-army army-group1) 0)
          (= (total-army army-group2) 0))
      (format-result army-group1 army-group2)
      ;else
      (let [army-damage1   (army-damage army-group1 speed)
            army-damage2   (army-damage army-group2 speed)
            army-group1-r  (:army-group (army-suffer army-group1 army-damage2))
            army-group2-r  (:army-group (army-suffer army-group2 army-damage1))
            rec-group1     (if (<= speed 1) army-group1-r old-group1)
            rec-group2     (if (<= speed 1) army-group2-r old-group2) ]
        (if (and (<= speed 1)
              (= army-group1-r old-group1)
              (= army-group2-r old-group2))
          (format-result army-group1 army-group2)
          ;(println army-group1-r)
          ;else
          (recur army-group1-r army-group2-r (if (<= speed 1) 6 (dec speed))
            rec-group1 rec-group2))
        ))))

(defn army-view [setup result]
  (html [:h1 "簡易戰鬥測試"]
    (form-to [:POST "/army"]
      [:table
       [:tr
        [:td "軍團 1" [:br]
         (map (fn [army]
                [:span (label (str (:name army) "1") (:cname army))
                 (text-field (str (:name army) "1") (get-army-ws (:army-group1 setup) army)) [:br]]) armies-by-view)
         ]
        [:td "軍團 2" [:br]
         (map (fn [army]
                [:span (label (str (:name army) "2") (:cname army))
                 (text-field (str (:name army) "2") (get-army-ws (:army-group2 setup) army)) [:br]]) armies-by-view)
         ]]
       [:tr [:td {:colspan "2"} (submit-button "測試") (reset-button "reset")]]
       (when result
         [:tr [:td {:colspan "2"} [:span (:result result)]]])
       (when result
         [:tr
          [:td "軍團 1 剩餘" [:br]
           (map (fn [army]
                  [:span (label (str (:name army) "_1") (:cname army))
                   (text-field (str (:name army) "_1") (get-army-ws (:army-group1 result) army) ) [:br]]) armies-by-view)
           ]

          [:td "軍團 2 剩餘" [:br]
           (map (fn [army]
                  [:span (label (str (:name army) "_2") (:cname army))
                   (text-field (str (:name army) "_2") (get-army-ws (:army-group2 result) army)) [:br]]) armies-by-view)
           ]

          ])])))

(defn handle-view [params]
  (let [armies (reduce (fn [prev param-key]
                         (let [[army-all army-type army-group-num]
                               (re-find #"([A-Za-z]+)([12])" param-key)]
                           (if army-all
                             (assoc-in prev
                               [(keyword (str "army-group" army-group-num))
                                (army-eval army-type)]
                               (parse-int (get params param-key)))
                             ;else
                             prev)
                           )) {} (keys params))]
    (army-view armies (army-fight (:army-group1 armies) (:army-group2 armies)))))

(in-ns 'user)
(use 'jinrou-clojure.army)

;(army-fight army-ga army-gb)



