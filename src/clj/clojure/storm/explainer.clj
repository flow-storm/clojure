(ns clojure.storm.explainer
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [clojure.storm.explainer-printer :as explainer-printer]
            [clojure.test :refer [deftest testing is]]))

(defmulti explain-macro-syntax-spec-fail-message (fn [symbol spec-problem] symbol))

(defmethod explain-macro-syntax-spec-fail-message 'clojure.core/fn
  [_ {:keys [reason path pred val] :as spec-problem}]
  (println "@@@@" spec-problem)
  (cond
    (= path [:fn-name])
    "Function name should be a symbol"
    
    (= path [:fn-tail :arity-1 :params])
    "Wrong parameters format"

    (=  (#{[:fn-tail :arity-n :params]
           [:fn-tail :arity-n :bodies :params]}
         path))
    (cond
      (and (map? val) (:keys val) (not (vector? (:keys val))))
      "The form after :keys should be a vector"
      
      (and (map? val) (:as val) (not (symbol? (:as val))))
      "The form after :as should be a symbol"

      (and (map? val) (not (every? #{"keys" "strs" "as" "or" "syms"} (->> val keys (filter keyword?) (map name)))))
      "Map destructuring keys should be any of :keys, :strs, :as, :or, :syms"

      (not (or (vector? val) (map? val)))
      "Wrong destructuring. Only {} or [] can be used here")))

(defmethod explain-macro-syntax-spec-fail-message 'clojure.core/defn
  [_ spec-problem]
  (explain-macro-syntax-spec-fail-message 'clojure.core/fn spec-problem))

(defmethod explain-macro-syntax-spec-fail-message 'clojure.core/defn-
  [_ spec-problem]
  (explain-macro-syntax-spec-fail-message 'clojure.core/fn spec-problem))

(defmethod explain-macro-syntax-spec-fail-message 'clojure.core/let
  [s {:keys [reason path pred val] :as spec-problem}]
  (cond
    (= path [:bindings])
    (cond
      (not (vector? val))
      "Let bindings should be wrapped in a vector"
      
      (not (even? (count val)))
      "Missing let binding")

    (#{[:bindings :form :local-symbol]
       [:bindings :form :seq-destructure]
       [:bindings :form :map-destructure]}
     path)
    "Wrong let binding destructuring. Only {} or [] can be used here."))

(defmethod explain-macro-syntax-spec-fail-message :default
  [s spec-problem]
  (str "Unhandled spec problem macroexpanding "
       s
       ".\nExtend clojure.storm.explainer/explain-macro-syntax-spec-fail-message to provide a better message.\nProblem : "
       spec-problem))

(defn deduplicate-in-order [coll]
  (let [seen (atom #{})]
    (filterv (fn [x]
               (if (@seen x)
                 false
                 (swap! seen conj x)))
             coll)))

(defn explain-macro-syntax-spec-fail [{:clojure.error/keys [spec symbol]}]
  (let [{:clojure.spec.alpha/keys [value problems]} spec
        full-form (conj value symbol)
        coord-problems (reduce (fn [r [in problems]]
                                 (let [coord (if (empty? in) in (update in 0 inc))
                                       messages (->> problems
                                                     (map (fn [p] (explain-macro-syntax-spec-fail-message symbol p)))
                                                     deduplicate-in-order
                                                     (str/join ". "))]
                                   (assoc r coord messages)))
                               {}            
                               (group-by :in problems))]
    (explainer-printer/render-form-msg-problems full-form coord-problems)))

(defn explain-macro-syntax [{:clojure.error/keys [phase source path line column symbol class cause spec]
                             :as triage-data}]
  (let [loc (str (or path source "REPL") ":" (or line 1) (if column (str ":" column) ""))]
    (format "Syntax error macroexpanding %sat (%s).%n%s"
            (if symbol (str symbol " ") "")
            loc
            (if spec
              (explain-macro-syntax-spec-fail triage-data)
              (format "%s%n" cause)))))

(defn eval-err-string [form-str]
  (try
    (eval (read-string form-str))
    (catch Throwable t
      (with-out-str
        (binding [*err* *out*]
          ((requiring-resolve 'clojure.main/repl-caught) t))))))

(defmacro is-form-msg [form-str msg]
  `(is (= ~msg
          (eval-err-string ~form-str))
       (str "Wrong message for " ~form-str)))

(deftest defn-expansion-test
  (testing "defn macroexpansion"
    
    (is-form-msg "(defn :foo [])"
                 
"Syntax error macroexpanding clojure.core/defn at (REPL:1:1).
(clojure.core/defn :foo [])
                   ^_ Function name should be a symbol
")

    (is-form-msg "(defn foo :asb [] 42)"
                 
"Syntax error macroexpanding clojure.core/defn at (REPL:1:1).
(clojure.core/defn foo :asb [] 42)
                       ^_ Wrong parameters format.
")
    
    (is-form-msg "(defn foo [{:keys [b] :as [j]}])"

"Syntax error macroexpanding clojure.core/defn at (REPL:1:1).
(clojure.core/defn foo [{:keys [b]  :as [j]}])
                        ^_ Wrong parameters format. The form after :as should be a symbol
")

    (is-form-msg "(defn foo [{:keys b}])"

"Syntax error macroexpanding clojure.core/defn at (REPL:1:1).
(clojure.core/defn foo [{:keys b}])
                        ^_ Wrong parameters format. The form after :keys should be a vector
")

    (is-form-msg "(defn foo [{:key [b]}])"

                 "Syntax error macroexpanding clojure.core/defn at (REPL:1:1).
(clojure.core/defn foo [{:key [b]}])
                        ^_ Wrong parameters format. Map destructuring keys should be any of :keys, :strs, :as, :or, :syms
")
    
    (is-form-msg "(defn foo [(a b)])"

                 "Syntax error macroexpanding clojure.core/defn at (REPL:1:1).
(clojure.core/defn foo [(a b)])
                        ^_ Wrong parameters format. Wrong destructuring. Only {} or [] can be used here
")))


(deftest let-expansion-test
  (testing "let macroexpansion"
    
    (is-form-msg "(let [a b c] a)"
                 
"Syntax error macroexpanding clojure.core/let at (REPL:1:1).
(clojure.core/let [a b c] a)
                  ^_ Missing let binding
")

    (is-form-msg "(let (a b c d) a)"
                 
"Syntax error macroexpanding clojure.core/let at (REPL:1:1).
(clojure.core/let (a b c d) a)
                  ^_ Let bindings should be wrapped in a vector
")

    (is-form-msg "(let [(a b) (c d)] a)"
                 
"Syntax error macroexpanding clojure.core/let at (REPL:1:1).
(clojure.core/let [(a b) (c d)] a)
                   ^_ Wrong let binding destructuring. Only {} or [] can be used here.
")

    ))

(comment

  (require 'clojure.storm.explainer-printer :reload)
  (require 'clojure.storm.explainer :reload)
  (clojure.storm.explainer/defn-expansion-test)
  (clojure.storm.explainer/let-expansion-test)
  )
