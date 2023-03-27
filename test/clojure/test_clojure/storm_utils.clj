(ns clojure.test-clojure.storm-utils
  (:require [clojure.string :as str]))

(def captured-traces (atom []))

(defn maybe-serialize [v]
  (if (or (nil? v)    (map? v)    (vector? v)
          (set? v)    (seq? v)    (symbol? v)
          (string? v) (number? v) (boolean? v))
    v

    (-> v
        pr-str
        (str/replace #"#object\[.+\]" "#object[...]"))))

(defn stable-fn-name [fn-name]
  (str/replace fn-name #"\-\-[0-9]+$" "--GEN-ID"))

(defn reset-captured-traces-fixture [f]
  (clojure.storm.Tracer/setTraceFnsCallbacks
   {:trace-fn-call-fn-key
    (fn [_ fn-ns fn-name args form-id]
      (swap! captured-traces conj [:fn-call fn-ns (stable-fn-name fn-name) (into [] (map maybe-serialize) args) form-id]))
    :trace-fn-return-fn-key
    (fn [_ ret-val coord _]
      (swap! captured-traces conj [:fn-return (maybe-serialize ret-val) coord]))
    :trace-expr-fn-key
    (fn [_ val coord _]
                  (swap! captured-traces conj [:expr-exec (maybe-serialize val) coord]))
    :trace-bind-fn-key
    (fn [_ coord sym-name val]
                  (swap! captured-traces conj [:bind sym-name (maybe-serialize val) coord]))})
  (reset! captured-traces [])
  (f)
  (clojure.storm.Tracer/setTraceFnsCallbacks {}))

(defn capture [] @captured-traces)
