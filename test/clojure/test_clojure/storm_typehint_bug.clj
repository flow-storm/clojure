(ns clojure.test-clojure.storm-typehint-bug
  (:require
   [clojure.test :refer [deftest is testing]])
  (:import (clojure.testfixtures Component MyView MyView$Builder)))

(def form-1
  "Returns a function that executes the inline .build call (previously failing)."
  (fn []
    (-> (Component/newBuilder)
        (.setMyView ^MyView$Builder
         (.build (MyView/newBuilder)))
        .build)))

(def form-2
  "Returns a function that executes the threaded -> form (was expected to succeed)."
  (fn []
    (-> (Component/newBuilder)
        (.setMyView ^MyView$Builder
         (-> (MyView/newBuilder)
             .build))
        .build)))

(deftest wrong-type-hint-falls-back-to-reflection-test
  (testing "Flow-Storm instrumentation matches Clojure's fallback for incorrect hints"
    (is (thrown? java.lang.ClassCastException (form-1)))
    (is (some? (form-2)))))
