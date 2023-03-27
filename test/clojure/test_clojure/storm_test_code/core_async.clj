(ns clojure.test-clojure.storm-test-code.core-async
  (:require [clojure.core.async :as async]))

(defn produce [c]
  (async/go-loop [[i & r] (range 10)]
    (if i
      (do
        (async/>! c i)
        (recur r))
      (async/>! c :done))))

(defn consume [c]
  (async/go-loop [v (async/<! c)
                  sum 0]
    (if (= v :done)
      sum
      (recur (async/<! c) (+ sum v)))))


