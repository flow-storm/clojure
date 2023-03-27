(ns clojure.test-clojure.storm-types
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [clojure.test-clojure.storm-test-code.types :as ts]
            [clojure.test-clojure.storm-utils :as u]))

(use-fixtures :each u/reset-captured-traces-fixture)

(deftest defrecord-test
  (let [s (ts/->Square 5)
        r (ts/area s)]
    (is (= 25 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.types" "area" [] 1908634760]
            [:expr-exec 5 "4,2,1"]
            [:expr-exec 5 "4,2,2"]
            [:expr-exec 25 "4,2"]
            [:fn-return 25 "4,0"]]
           (u/capture)) "captured traces should match.")))

(deftest deftype-test
  (let [r (ts/area (ts/->Circle 2))]
    (is (= 12.56 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.types" "area" [] 441667179]
            [:expr-exec 2 "4,2,2"]
            [:expr-exec 2 "4,2,3"]
            [:expr-exec 12.56 "4,2"]
            [:fn-return 12.56 "4,0"]]
           (u/capture)) "captured traces should match.")))

(deftest extend-type-basic-test
  (let [r (ts/area "tha-shape")]
    (is (= 9 r) "function return should be right.")
    (is (=  [[:fn-call "clojure.test-clojure.storm-test-code.types" "area" ["tha-shape"] 1050802064]
             [:bind "s" "tha-shape" ""]
             [:expr-exec "tha-shape" "3,2,1"]
             [:expr-exec 9 "3,2"]
             [:fn-return 9 ""]]
           (u/capture)) "captured traces should match.")))

(deftest extend-type-proto-test
  (let [tr (ts/->Triangle 2 5)
        r (ts/sides-count tr)]
    (is (= 3 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.types" "sides-count" [tr] -335308803]
            [:expr-exec 2 "3,2,2"]
            [:expr-exec 3 "3,2"]
            [:fn-return 3 ""]]
           (u/capture)) "captured traces should match.")))

(deftest extend-proto-basic-test
  (let [r (ts/area 10)]
    (is (= 100 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.types" "area" [10] -1332394678]
            [:bind "n" 10 ""]
            [:expr-exec 10 "3,2,1"]
            [:expr-exec 10 "3,2,2"]
            [:expr-exec 100 "3,2"]
            [:fn-return 100 ""]]
           (u/capture)) "captured traces should match.")))

(deftest extend-proto-type-test
  (let [rect (ts/->Rectangle 2 4)
        r (ts/area rect)]
    (is (= 8 r) "function return should be right.")
    (is (= [[:fn-call "clojure.test-clojure.storm-test-code.types" "area" [rect] 50784036]
            [:bind "r" rect ""]
            [:expr-exec rect "3,2,1,1"]
            [:expr-exec 2 "3,2,1"]
            [:expr-exec rect "3,2,2,1"]
            [:expr-exec 4 "3,2,2"]
            [:expr-exec 8 "3,2"]
            [:fn-return 8 ""]]
           (u/capture)) "captured traces should match.")))
