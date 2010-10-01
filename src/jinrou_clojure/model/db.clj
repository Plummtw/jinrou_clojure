
(ns jinrou-clojure.model.db
  (:refer-clojure)
  (:use somnium.congomongo
        jinrou-clojure.properties))

(mongo! :db *mongo-db*)

(defn validate-field
  "檢查欄位是否符合限制 [hash key validate_fn output]，
   正確回傳nil，錯誤回傳字串"
  [hash key validate_fn output]
    (when-not (validate_fn (hash key))
      (if (fn? output)
       (output (hash key))
       output)))

(defn validate-exist
  "檢查必要欄位是否存在 [hash key] [hash key field_name]，
   正確回傳nil，錯誤回傳字串"
  ([hash key] (validate-exist hash key (str key)))
  ([hash key field_name]
    (when-not (hash key)
       (str (hash :db_name) "欄位" field_name "為空白"))))

(defn validate-string>=
  "檢查字串欄位長度是否>=限制，
   正確回傳nil，錯誤回傳字串"
  ([hash key limit] (validate-string>= hash key limit (str key)))
  ([hash key limit field_name ]
     (let [string (hash key)]
       (cond
         (nil? string)  nil
         (not (string? string))   (str (hash :db_name) "欄位" field_name "不為字串")
         (< (count string) limit) (str (hash :db_name) "欄位" field_name "過小 <" limit)
         :else nil))))

(defn validate-string<=
  "檢查字串欄位長度是否<=限制，
   正確回傳nil，錯誤回傳字串"
  ([hash key limit] (validate-string<= hash key limit (str key)))
  ([hash key limit field_name ]
     (let [string (hash key)]
       (cond
         (nil? string)  nil
         (not (string? string))   (str (hash :db_name) "欄位" field_name "不為字串")
         (> (count string) limit) (str (hash :db_name) "欄位" field_name "過大 >" limit)
         :else nil))))

(defn validate-string-in
  "檢查字串欄位長度是否>=及<=限制，
   正確回傳nil，錯誤回傳字串"
  ([hash key lowbound upbound] (validate-string-in hash key lowbound upbound (str key)))
  ([hash key lowbound upbound field_name ]
     (let [string (hash key)]
       (cond
         (nil? string)  nil
         (not (string? string))   (str (hash :db_name) "欄位" field_name "不為字串")
         (< (count string) lowbound) (str (hash :db_name) "欄位" field_name "過小 <" lowbound)
         (> (count string) upbound) (str (hash :db_name) "欄位" field_name "過大 >"  upbound)
         :else nil))))

(defn validate-number>=
  "檢查數字欄位長度是否>=限制，
   正確回傳nil，錯誤回傳字串"
  ([hash key limit] (validate-number>= hash key limit (str key)))
  ([hash key limit field_name ]
     (let [number (hash key)]
       (cond
         (nil? number)  nil
         (not (number? number))   (str (hash :db_name) "欄位" field_name "不為數字")
         (< number limit) (str (hash :db_name) "欄位" field_name "過小 <" limit)
         :else nil))))

(defn validate-number<=
  "檢查數字欄位長度是否<=限制，
   正確回傳nil，錯誤回傳字串"
  ([hash key limit] (validate-number<= hash key limit (str key)))
  ([hash key limit field_name ]
     (let [number (hash key)]
       (cond
         (nil? number)  nil
         (not (number? number))   (str (hash :db_name) "欄位" field_name "不為數字")
         (> number limit) (str (hash :db_name) "欄位" field_name "過大 >" limit)
         :else nil))))

(defn validate-number-in
  "檢查數字欄位長度是否>=及<=限制，
   正確回傳nil，錯誤回傳字串"
  ([hash key lowbound upbound] (validate-number-in hash key lowbound upbound (str key)))
  ([hash key lowbound upbound field_name ]
     (let [number (hash key)]
       (cond
         (nil? number)  nil
         (not (number? number))   (str (hash :db_name) "欄位" field_name "不為數字")
         (< number lowbound) (str (hash :db_name) "欄位" field_name "過小 <" lowbound)
         (> number upbound) (str (hash :db_name)  "欄位" field_name "過大 >" upbound)
         :else nil))))
       
(in-ns 'user)
(use 'jinrou-clojure.model.db)


