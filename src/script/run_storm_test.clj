(System/setProperty "java.awt.headless" "true")
;; (clojure.storm.Emitter/setInstrumentationEnable true)
;; (clojure.storm.Emitter/addInstrumentationOnlyPrefix "clojure.test-clojure.storm-test-code")
(require
 '[clojure.string :as str]
 '[clojure.test :as test]
 '[clojure.tools.namespace.find :as ns])
(def namespaces (filter (fn [ns-symb]
                          (str/includes? (str ns-symb) "storm"))
                        (ns/find-namespaces-in-dir (java.io.File. "test"))))
(println "Instrumenting everything under: " (->> (clojure.storm.Emitter/getInstrumentationOnlyPrefixes)
                                                 (map #(clojure.lang.Compiler/demunge %))
                                                 (str/join ",")))
(doseq [ns namespaces] (require ns))
(let [summary (apply test/run-tests namespaces)]
  (System/exit (if (test/successful? summary) 0 -1)))
