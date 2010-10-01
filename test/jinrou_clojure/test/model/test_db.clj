
(ns jinrou-clojure.test.model.test-db
  (:refer-clojure)
  (:use clojure.test
        [jinrou-clojure.model.db :reload true]))

(deftest test-validate-exist
  (is (= (validate-exist {:a 'b} :a) nil))
  (is (= (validate-exist {:a 'b} :b) "欄位:b為空白"))
  (is (= (validate-exist {:a 'b :db_name "a"} :b) "a欄位:b為空白"))
  (is (= (validate-exist {:a 'b :db_name "a"} :b "c") "a欄位c為空白")))

(deftest test-validate-string>=
  (is (= (validate-string>= {} :a 1) nil))
  (is (= (validate-string>= {:a ""} :a 1) "欄位:a過小 <1"))
  (is (= (validate-string>= {:a 1} :a 1) "欄位:a不為字串"))
  (is (= (validate-string>= {:a "ss"} :a 1) nil)))

(deftest test-validate-string<=
  (is (= (validate-string<= {} :a 1) nil))
  (is (= (validate-string<= {:a ""} :a 1) nil))
  (is (= (validate-string<= {:a 1} :a 1) "欄位:a不為字串"))
  (is (= (validate-string<= {:a "ss"} :a 1) "欄位:a過大 >1")))

(deftest test-validate-string-in
  (is (= (validate-string-in {} :a 1 3) nil))
  (is (= (validate-string-in {:a ""} :a 1 3) "欄位:a過小 <1"))
  (is (= (validate-string-in {:a 1} :a 1 3) "欄位:a不為字串"))
  (is (= (validate-string-in {:a "ssss"} :a 1 3) "欄位:a過大 >3")))

(deftest test-validate-number>=
  (is (= (validate-number>= {} :a 1) nil))
  (is (= (validate-number>= {:a 0} :a 1) "欄位:a過小 <1"))
  (is (= (validate-number>= {:a ""} :a 1) "欄位:a不為數字"))
  (is (= (validate-number>= {:a 2} :a 1) nil)))

(deftest test-validate-number<=
  (is (= (validate-number<= {} :a 1) nil))
  (is (= (validate-number<= {:a 0} :a 1) nil))
  (is (= (validate-number<= {:a ""} :a 1) "欄位:a不為數字"))
  (is (= (validate-number<= {:a 2} :a 1) "欄位:a過大 >1")))

(deftest test-validate-number-in
  (is (= (validate-number-in {} :a 1 1) nil))
  (is (= (validate-number-in {:a 0} :a 1 3) "欄位:a過小 <1"))
  (is (= (validate-number-in {:a ""} :a 1 3) "欄位:a不為數字"))
  (is (= (validate-number-in {:a 4} :a 1 3) "欄位:a過大 >3")))

(run-tests)