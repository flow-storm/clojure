(ns clojure.test-clojure.storm-core-async
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [clojure.test-clojure.storm-test-code.core-async :as ca]
            [clojure.core.async :as async]
            [clojure.test-clojure.storm-utils :as u]))

(use-fixtures :each u/reset-captured-traces-fixture)

#_(deftest producer-consumer-test
  (let [c (async/chan 100)
        prod-res (async/<!! (ca/produce c))
        cons-res (async/<!! (ca/consume c))]
    (is (= true prod-res) "producer return should be right.")
    (is (= 45 cons-res) "consumer return should be right.")
    (is (= []
           (u/capture)) "captured traces should match.")))




























