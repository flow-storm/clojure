(ns clojure.test-clojure.storm-functions
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [clojure.test-clojure.storm-test-code.functions :as fns]
            [clojure.test-clojure.storm-utils :as u]))

(use-fixtures :each u/reset-captured-traces-fixture)

(deftest empty-body-fn-test
  (let [r (fns/empty-body 40 2)]
    (is (= nil r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.functions" "empty-body" [40 2] -1793659886]
            [:fn-return nil ""]]
           (u/capture)) "captured traces should match.")))

(deftest simple-fn-test
  (let [r (fns/simple 40 2)]
    (is (= 42 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.functions" "simple" [40 2] 1637581100]
            [:bind "a" 40 ""]
            [:bind "b" 2 ""]
            [:expr-exec 40 "3,1"]
            [:expr-exec 2 "3,2"]
            [:expr-exec 42 "3"]
            [:fn-return 42 ""]]
           (u/capture)) "captured traces should match.")))

(deftest defed-fn-test
  (let [r (fns/defed)]
    (is (= 42 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.functions" "defed" [] 419115123]
            [:fn-return 42 "2"]]
           (u/capture)) "captured traces should match.")))

(deftest multi-arity-fn-test
  (let [r (fns/multi-arity 40)]
    (is (= 42 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.functions" "multi-arity" [40] -231028429]
            [:bind "a" 40 ""]
            [:expr-exec 40 "2,1,1"]
            [:fn-call "clojure.test-clojure.storm-test-code.functions" "multi-arity" [40 2] -231028429]
            [:bind "a" 40 ""]
            [:bind "b" 2 ""]
            [:expr-exec 40 "3,1,1"]
            [:expr-exec 2 "3,1,2"]
            [:expr-exec 42 "3,1"]
            [:fn-return 42 ""]
            [:expr-exec 42 "2,1"]
            [:fn-return 42 ""]]
           (u/capture)) "captured traces should match.")))

(deftest args-destructuring-fn-test
  (let [r (fns/args-destructuring {:a 1 :b 2} 3 [4 5])]
    (is (= 15 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.functions" "args-destructuring" [{:a 1, :b 2} 3 [4 5]] -384994215]
            [:bind "c" 3 ""]
            [:bind "a" 1 ""]
            [:bind "b" 2 ""]
            [:bind "d" 4 ""]
            [:bind "e" 5 ""]
            [:expr-exec 1 "3,1"]
            [:expr-exec 2 "3,2"]
            [:expr-exec 3 "3,3"]
            [:expr-exec 4 "3,4"]
            [:expr-exec 5 "3,5"]
            [:expr-exec 15 "3"]
            [:fn-return 15 ""]]
           (u/capture)) "captured traces should match.")))

(deftest variadic-fn-test
  (let [r (fns/variadic 1 2 3 4)]
    (is (= 10 r) "function return should be right.")
    (is (= '[[:fn-call "clojure.test-clojure.storm-test-code.functions" "variadic" [(1 2 3 4)] -594273188]
             [:bind "nums" (1 2 3 4) ""]
             [:expr-exec "#object[...]" "3,1"]
             [:expr-exec (1 2 3 4) "3,2"]
             [:expr-exec 10 "3"]
             [:fn-return 10 ""]]
           (u/capture)) "captured traces should match.")))

(deftest tail-recursive-fn-test
  (let [r (fns/tail-recursive 0 2)]
    (is (= 3 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.functions" "tail-recursive" [0 2] 171071930]
            [:bind "s" 0 ""]
            [:bind "n" 2 ""]
            [:expr-exec 2 "3,1,1"]
            [:expr-exec false "3,1"]
            [:expr-exec 0 "3,3,1,1"]
            [:expr-exec 2 "3,3,1,2"]
            [:expr-exec 2 "3,3,1"]
            [:expr-exec 2 "3,3,2,1"]
            [:expr-exec 1 "3,3,2"]
            [:expr-exec 1 "3,1,1"]
            [:expr-exec false "3,1"]
            [:expr-exec 2 "3,3,1,1"]
            [:expr-exec 1 "3,3,1,2"]
            [:expr-exec 3 "3,3,1"]
            [:expr-exec 1 "3,3,2,1"]
            [:expr-exec 0 "3,3,2"]
            [:expr-exec 0 "3,1,1"]
            [:expr-exec true "3,1"]
            [:expr-exec 3 "3,2"]
            [:expr-exec 3 "3"]
            [:fn-return 3 ""]]
           (u/capture)) "captured traces should match.")))

(deftest recursive-fn-test
  (let [r (fns/factorial 4)]
    (is (= 24 r) "function return should be right.")
    (is (=  [[:fn-call "clojure.test-clojure.storm-test-code.functions" "factorial" [4] -1111894630]
             [:bind "n" 4 ""]
             [:expr-exec 4 "3,1,1"]
             [:expr-exec false "3,1"]
             [:expr-exec 4 "3,3,1"]
             [:expr-exec 4 "3,3,2,1,1"]
             [:expr-exec 3 "3,3,2,1"]
             [:fn-call "clojure.test-clojure.storm-test-code.functions" "factorial" [3] -1111894630]
             [:bind "n" 3 ""]
             [:expr-exec 3 "3,1,1"]
             [:expr-exec false "3,1"]
             [:expr-exec 3 "3,3,1"]
             [:expr-exec 3 "3,3,2,1,1"]
             [:expr-exec 2 "3,3,2,1"]
             [:fn-call "clojure.test-clojure.storm-test-code.functions" "factorial" [2] -1111894630]
             [:bind "n" 2 ""]
             [:expr-exec 2 "3,1,1"]
             [:expr-exec false "3,1"]
             [:expr-exec 2 "3,3,1"]
             [:expr-exec 2 "3,3,2,1,1"]
             [:expr-exec 1 "3,3,2,1"]
             [:fn-call "clojure.test-clojure.storm-test-code.functions" "factorial" [1] -1111894630]
             [:bind "n" 1 ""]
             [:expr-exec 1 "3,1,1"]
             [:expr-exec false "3,1"]
             [:expr-exec 1 "3,3,1"]
             [:expr-exec 1 "3,3,2,1,1"]
             [:expr-exec 0 "3,3,2,1"]
             [:fn-call "clojure.test-clojure.storm-test-code.functions" "factorial" [0] -1111894630]
             [:bind "n" 0 ""]
             [:expr-exec 0 "3,1,1"]
             [:expr-exec true "3,1"]
             [:expr-exec 1 "3"]
             [:fn-return 1 ""]
             [:expr-exec 1 "3,3,2"]
             [:expr-exec 1 "3,3"]
             [:expr-exec 1 "3"]
             [:fn-return 1 ""]
             [:expr-exec 1 "3,3,2"]
             [:expr-exec 2 "3,3"]
             [:expr-exec 2 "3"]
             [:fn-return 2 ""]
             [:expr-exec 2 "3,3,2"]
             [:expr-exec 6 "3,3"]
             [:expr-exec 6 "3"]
             [:fn-return 6 ""]
             [:expr-exec 6 "3,3,2"]
             [:expr-exec 24 "3,3"]
             [:expr-exec 24 "3"]
             [:fn-return 24 ""]]
           (u/capture)) "captured traces should match.")))

(deftest multimethods-test
  (let [r (fns/area {:type :square :side 4})]
    (is (= 16 r) "function return should be right.")
    (is (=  [[:fn-call "clojure.test-clojure.storm-test-code.functions" "area" [{:type :square, :side 4}] -1174353746]
             [:bind "side" 4 ""]
             [:expr-exec 4 "4,1"]
             [:expr-exec 4 "4,2"]
             [:expr-exec 16 "4"]
             [:fn-return 16 ""]]
            (u/capture)) "captured traces should match.")))
