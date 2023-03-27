(ns clojure.test-clojure.storm-bodies
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [clojure.test-clojure.storm-test-code.bodies :as b]
            [clojure.test-clojure.storm-utils :as u]))

(use-fixtures :each u/reset-captured-traces-fixture)

(deftest try-catch-test
  (let [r (b/tried)]
    (is (= 4 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.bodies" "tried" [] 524795972]
            [:expr-exec 4 "3,3,3"]
            [:fn-return 4 ""]]           
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

;; (deftest interop-test
;;   (let [r (b/interopter #js {:num 2 :f (fn f [x] x)})]
;;     (is (= 42 r) "function return should be right.")
;;     (is (= []
;;            (u/capture)) "captured traces should match.")))
