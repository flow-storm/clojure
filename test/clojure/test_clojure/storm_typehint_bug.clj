(ns clojure.test-clojure.storm-typehint-bug
  (:require
   [clojure.test :refer [deftest is testing]]
   [clojure.test-clojure.storm-test-code.hints :as hints]))

(deftest wrong-type-hint-falls-back-to-reflection-test
  (testing "Flow-Storm instrumentation matches Clojure's fallback for incorrect hints"
    (is (thrown? java.lang.ClassCastException (hints/form-1)))
    (is (some? (hints/form-2)))
    (is (some? (hints/form-3)))))
