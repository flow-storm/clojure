(ns clojure.test-clojure.storm-bodies
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [clojure.test-clojure.storm-test-code.bodies :as b]
            [clojure.test-clojure.storm-utils :as u]))

(use-fixtures :each u/reset-captured-traces-fixture)

(deftest try-catch-test
  (let [r (b/tried)]
    (is (= 4 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "tried" [] 524795972]
            [:expr-exec "#error[Dummy]" "3,2,1"]
            [:expr-exec 4 "3,3,3"]
            [:fn-return 4 ""]]           
           (u/capture)) "captured traces should match.")))

(deftest uncached-throw-test
  (let [r (try
            (b/uncached-throw)
            (catch Exception e :throwed))]
    (is (= :throwed r) "function should have throwed")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "uncached-throw" [] 856953197]
            [:expr-exec "#error[Dang]" "3,1"]
            [:fn-unwind "Dang" ""]]           
           (u/capture)) "captured traces should match.")))

(deftest uncached-throw-inner-test
  (let [r (try
            (b/uncached-throw-inner)
            (catch Exception e :throwed))]
    (is (= :throwed r) "function should have throwed")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "uncached-throw-inner" [] -1606443558]
            [:bind "f" "#object[...]" "3"]
            [:expr-exec "#object[...]" "3,2,0"]
            [:fn-call "clojure.test-clojure.storm-test-code.bodies" "uncached-throw-inner/inner--GEN-ID" [] -1606443558]
            [:expr-exec "#error[Dang]" "3,1,1,3,1"]
            [:fn-unwind "Dang" "3,1,1"]
            [:fn-unwind "Dang" ""]]           
           (u/capture)) "captured traces should match.")))

(deftest letfn-test
  (let [r (b/letfn-fn)]
    (is (= 5 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "letfn-fn" [] 1519931677]
            [:expr-exec "#object[...]" "3,2,2,0"]
            [:fn-call "clojure.test-clojure.storm-test-code.bodies" "letfn-fn/square--GEN-ID" [2] 1519931677]
            [:bind "x" 2 ""]
            [:expr-exec 2 "3,1,0,2,1"]
            [:expr-exec 2 "3,1,0,2,2"]
            [:expr-exec 4 "3,1,0,2"]
            [:fn-return 4 ""]
            [:expr-exec 4 "3,2,2"]
            [:expr-exec 5 "3,2"]
            [:fn-return 5 ""]]
           (u/capture)) "captured traces should match.")))

(deftest loops-test
  (let [r (b/looper)]
    (is (= 3 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "looper" [] -2008026743]
            [:bind "s" 0 "3"]
            [:bind "n" 2 "3"]
            [:bind "s" 0 "3"]
            [:bind "n" 2 "3"]
            [:expr-exec 2 "3,2,1,1"]
            [:expr-exec 0 "3,2,3,1,1"]
            [:expr-exec 2 "3,2,3,1,2"]
            [:expr-exec 2 "3,2,3,1"]
            [:expr-exec 2 "3,2,3,2,1"]
            [:expr-exec 1 "3,2,3,2"]
            [:bind "s" 2 "3"]
            [:bind "n" 1 "3"]
            [:expr-exec 1 "3,2,1,1"]
            [:expr-exec 2 "3,2,3,1,1"]
            [:expr-exec 1 "3,2,3,1,2"]
            [:expr-exec 3 "3,2,3,1"]
            [:expr-exec 1 "3,2,3,2,1"]
            [:expr-exec 0 "3,2,3,2"]
            [:bind "s" 3 "3"]
            [:bind "n" 0 "3"]
            [:expr-exec 0 "3,2,1,1"]
            [:expr-exec 3 "3,2,2"]
            [:expr-exec 3 "3,2"]
            [:expr-exec 3 "3"]
            [:fn-return 3 ""]]
           (u/capture)) "captured traces should match.")))

(deftest let-test
  (let [r (b/letter)]
    (is (= 15 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "letter" [] 1755477002]
            [:bind "a" 5 "3"]
            [:expr-exec 5 "3,1,3,1"]
            [:expr-exec 10 "3,1,3"]
            [:bind "b" 10 "3"]
            [:expr-exec 5 "3,1,5,1,1,1"]
            [:expr-exec 10 "3,1,5,1,1,2"]
            [:expr-exec 15 "3,1,5,1,1"]
            [:bind "z" 15 "3,1,5"]
            [:expr-exec 15 "3,1,5,2"] 
            [:expr-exec 15 "3,1,5"]
            [:bind "c" 15 "3"]
            [:expr-exec 15 "3,2"]
            [:expr-exec 15 "3"]
            [:fn-return 15 ""]]
           (u/capture)) "captured traces should match.")))

(deftest case-test
  (let [r (b/casey :first)]
    (is (= 42 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "casey" [":first"] 1958052943]
            [:bind "x" ":first" ""]
            [:expr-exec ":first" "3,1"]
            [:expr-exec 42 "3,3"]
            [:expr-exec 42 "3"]
            [:fn-return 42 ""]]
           (u/capture)) "captured traces should match.")))

(deftest do-test
  (let [r (b/doer)]
    (is (= 8 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "doer" [] -378760067]
            [:expr-exec 8 "3,3,2"]
            [:fn-return 8 ""]]           
           (u/capture)) "captured traces should match.")))

(deftest constructor-test
  (let [r (b/constructor)]
    (is (= r "ctor@@") "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "constructor" [] -2137784979]
            [:expr-exec "ctor" "3,1"]
            [:expr-exec "#object[...]" "3,2,1"]
            [:expr-exec "@@" "3,2"]
            [:expr-exec "ctor@@" "3"]
            [:fn-return "ctor@@" ""]]    
           (u/capture)) "captured traces should match.")))

(deftest method-value-test
  (let [r (b/method-value)]
    (is (= r [4 2]) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "method-value" [] -277140200]
            [:bind "parser" "#object[...]" "3"]
            [:expr-exec "#object[...]" "3,2,1"]
            [:fn-call "clojure.test-clojure.storm-test-code.bodies" "method-value/invoke--Integer-parseInt--GEN-ID" ["4"] -277140200]
            [:bind "arg1" "4" ""]
            [:expr-exec 4 "3,1,1"]
            [:fn-return 4 "3,1,1"]
            [:fn-call "clojure.test-clojure.storm-test-code.bodies" "method-value/invoke--Integer-parseInt--GEN-ID" ["2"] -277140200]
            [:bind "arg1" "2" ""]
            [:expr-exec 2 "3,1,1"]
            [:fn-return 2 "3,1,1"]
            [:expr-exec [4 2] "3,2"]
            [:expr-exec [4 2] "3"]
            [:fn-return [4 2] ""]]    
           (u/capture)) "captured traces should match.")))

(deftest hinted-and-static-test
  (let [r (b/hinted-and-static 42)]
    (is (= r 174) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "hinted-and-static" [42] 1633944069]
            [:bind "n" 42 ""]
            [:expr-exec "#object[...]" "3,1,1"]
            [:bind "arr" "#object[...]" "3"]
            [:expr-exec "#object[...]" "3,1,3,1"]
            [:expr-exec 3 "3,1,3"]
            [:bind "e" 3 "3"]
            [:expr-exec 3 "3,1,5,1"]
            [:expr-exec 3 "3,1,5"]
            [:bind "l" 3 "3"]
            [:expr-exec 42 "3,1,7,1"]
            [:expr-exec 168 "3,1,7"]
            [:bind "b" 168 "3"]
            [:expr-exec 3 "3,2,1"]
            [:expr-exec 3 "3,2,2"]
            [:expr-exec 168 "3,2,3"]
            [:expr-exec 174 "3,2"]
            [:expr-exec 174 "3"]
            [:fn-return 174 ""]]    
           (u/capture)) "captured traces should match.")))

;; (deftest interop-test
;;   (let [r (b/interopter #js {:num 2 :f (fn f [x] x)})]
;;     (is (= 42 r) "function return should be right.")
;;     (is (= []
;;            (u/capture)) "captured traces should match.")))
