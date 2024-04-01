(ns clojure.test-clojure.storm-test-code.bodies)

(defn tried []
  (try
    (+ 1 1)
    (throw (Exception. "Dummy"))
    (catch Exception e
      (+ 2 2))))

(defn uncached-throw []
  (throw (Exception. "Dang")))

(defn uncached-throw-inner []
  (let [f (fn inner []
            (throw (Exception. "Dang")))]
    (f)))

(defn letfn-fn []
  (letfn [(square [x]
            (* x x))]
    (+ 1 (square 2))))

(defn looper []
  (loop [s 0
         n 2]
    (if (zero? n)
      s
      (recur (+ s n) (dec n)))))

(defn letter []
  (let [a 5
        b (* a 2)
        c (let [z (+ a b)]
            z)]
    c))

(defn casey [x]
  (case x
    :first (+ 40 2)
    :second 1
    0))

(defn doer []
  (do
    (+ 1 1)
    (+ 2 2)
    (do
      (+ 3 3)
      (+ 4 4))))

(defn constructor []
  (count (String. "ctor")))

(defn hinted-and-static ^long [^long n]
  (let [arr (byte-array [1 2 3 4])
        e (aget arr 2)
        l (long e)
        b (bit-shift-left n 2)]
    (+ e l b)))

(defn interopter [o]
  ;; TODO
  )
