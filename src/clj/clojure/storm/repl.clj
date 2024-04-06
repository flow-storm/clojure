(ns clojure.storm.repl  
  (:import [clojure.storm Emitter Tracer]))

(def ^:private storm-initialized? (atom false))

(defn- call-flow-storm [fn-symb & args]
  (try
    (let [fqsym (symbol "flow-storm.storm-api" (name fn-symb))
          f (requiring-resolve fqsym)]
      (when f
        (apply f args)))
    (catch Throwable _)))

(defn- print-storm-help []
  (println "ClojureStorm Help\n")
  (println "ClojureStorm settings: \n")
  (println (format "  Instrumentation enable : %s" (Emitter/getInstrumentationEnable)))
  (when-let [pref (Emitter/makePrefixesString (Emitter/getInstrumentationOnlyPrefixes))] 
    (println (format "  Instrument only prefixes : %s" pref)))
  (when-let [pref (Emitter/makePrefixesString (Emitter/getInstrumentationSkipPrefixes))] 
    (println (format "  Instrument skip prefixes : %s" pref)))  
  (when-let [regex (Emitter/getInstrumentationSkipRegex)] 
    (println (format "  Instrument skip regex : %s" (.pattern regex))))
  (println)
  (println "ClojureStorm Commands: \n")  
  (println "  :inst       - Enable instrumentation.")
  (println "  :noinst     - Disable instrumentation. When instrumentation is disable, everything compiled will not be instrumented.")
  (println "                Useful for profiling, etc. Code already instrumented will remain instrumented until you recompile it.")  
  (println)
  (println "ClojureStorm JVM config properties: \n")  
  (println "  -Dclojure.storm.instrumentEnable        [true|false]")
  (println "  -Dclojure.storm.instrumentSkipPrefixes  Ex: clojure.,flow-storm.,cider.,nrepl.")
  (println "  -Dclojure.storm.instrumentOnlyPrefixes  Ex: my-project-ns.,my-lib-ns.core")
  (println)
  (call-flow-storm 'print-flow-storm-help))

(defn maybe-execute-storm-specials [input]
  (case input
    :help       (do (print-storm-help)                          true)
    :inst       (do (Emitter/setInstrumentationEnable true)     true)
    :noinst     (do (Emitter/setInstrumentationEnable false)    true)
    (call-flow-storm 'maybe-execute-flow-storm-specials input)))

(defn init-flow-storm-if-needed []  
  (when-not @storm-initialized?    
    (call-flow-storm 'start-recorder)
    (reset! storm-initialized? true)))

