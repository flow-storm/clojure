(ns clojure.storm.repl
  (:require [clojure.storm.tutorials.basics :as tut-basics])
  (:import [clojure.storm Emitter Tracer]))

(def ^:private storm-initialized? (atom false))

(defn- call-flow-storm [fn-symb & args]
  (let [fqsym (symbol "flow-storm.storm-api" (name fn-symb))
        f (requiring-resolve fqsym)]
    (if f
      (apply f args)
      (println "Storm error, couldn't find function" fqsym))))

(defn- print-storm-help []
  (println "Clojure Storm Help\n")
  (println "Current settings: \n")
  (println (format "  Recording : %s" (Tracer/getTraceEnable)))
  (println (format "  Instrumentation enable : %s" (Emitter/getInstrumentationEnable)))
  (println (format "  Fn expressions limit : %d" (call-flow-storm 'get-fn-expr-limit)))
  (when-let [pref (Emitter/makePrefixesString (Emitter/getInstrumentationSkipPrefixes))] 
    (println (format "  Instrument skip prefixes : %s" pref)))
  (when-let [pref (Emitter/makePrefixesString (Emitter/getInstrumentationOnlyPrefixes))] 
    (println (format "  Instrument only prefixes : %s" pref)))
  (println)
  (println "Commands: \n")
  (println "  :dbg        - Show the FlowStorm debugger UI, you can dispose it by closing the window.")
  (println "  :rec        - Start recording. All instrumented code traces will be recorded.")
  (println "  :stop       - Stop recording. Instrumented code will execute but nothing will be recorded, so no extra heap will be consumed.")  
  (println "  :inst       - Enable instrumentation.")
  (println "  :noinst     - Disable instrumentation. When instrumentation is disable, everything compiled will not be instrumented.")
  (println "                Useful for profiling, etc. Code already instrumented will remain instrumented until you recompile it.")
  (println "  :ex         - Focus the last recorded exception.")
  (println "  :last       - Focus the last recorded expression on this thread.")
  (println "  :help       - Print this help.")
  (println "  :tut/basics - Starts the basics tutorial.")
  (println)
  (println "JVM config properties: \n")
  (println "  -Dclojure.storm.traceEnable             [true|false]")
  (println "  -Dclojure.storm.instrumentEnable        [true|false]")
  (println "  -Dclojure.storm.instrumentSkipPrefixes  Ex: clojure.,flow-storm.,cider.,nrepl.")
  (println "  -Dclojure.storm.instrumentOnlyPrefixes  Ex: my-project-ns.,my-lib-ns.core")
  (println "  -Dflowstorm.fnExpressionsLimit          INTEGER (defaults to 10000)")
  (println "  -Dflowstorm.theme                       [dark|light|auto] (defaults to auto)")
  (println))

(defn- storm-rec-start []
  (call-flow-storm 'reset-all-threads-trees-build-stack)
  (Tracer/enableThreadsTracing))

(defn maybe-execute-storm-specials [input]
  (case input
    :help       (do (print-storm-help)                                     true)
    :dbg        (do (call-flow-storm 'start-debugger)                      true)
    :ex         (do (call-flow-storm 'jump-to-last-exception)              true)
    :last       (do (call-flow-storm 'jump-to-last-expression)             true)   
    :rec        (do (storm-rec-start)                                      true)
    :stop       (do (Tracer/disableThreadsTracing)                                       true)
    :inst       (do (Emitter/setInstrumentationEnable true)  true)
    :noinst     (do (Emitter/setInstrumentationEnable false) true)
    :tut/basics (do (tut-basics/start)                               true)
    :tut/next   (do (tut-basics/step-next)                                true)
    :tut/prev   (do (tut-basics/step-prev)                                true)
    false))

(defn init-flow-storm-if-needed []  
  (when-not @storm-initialized?    
    (call-flow-storm 'start-recorder)
    (reset! storm-initialized? true)))

