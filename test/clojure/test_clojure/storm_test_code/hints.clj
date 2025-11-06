(ns clojure.test-clojure.storm-test-code.hints
  (:import (clojure.testfixtures Component MyView MyView$Builder)
           (java.util.concurrent Executors)))

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

(def form-3
  "Returns a function that executes a form that needs the type hint befor the (fn ...) macro to succed"
  (fn []
    (.submit (Executors/newFixedThreadPool 10) ^Callable (fn [] 42))))



