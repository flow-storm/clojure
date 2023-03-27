(ns clojure.test-clojure.storm-test-code.types)

(defprotocol ShapeP
  (area [_])
  (perimeter [_]))

(defprotocol SidedP
  (sides-count [_]))

(defrecord Square [side]
  ShapeP
  (area [_] (* side side))
  (perimeter [_] (* side 4)))

(deftype Circle [rad]
  ShapeP
  (area [_] (* 3.14 rad rad))
  (perimeter [_] (* 2 3.14 rad)))

(defrecord Rectangle [w h])
(defrecord Triangle [b h])

(extend-type String
  ShapeP
  (area [s] (count s))
  (perimeter [s] 1))

(extend-type Triangle
  SidedP
  (sides-count [_] (+ 1 (+ 1 1))))

(extend-protocol ShapeP
  Rectangle
  (area [r] (* (:w r) (:h r)))
  (perimeter [r] (+ (:w r) (:w r)
                    (:h r) (:h r)))

  Triangle
  (area [t] (/ (* (:b t) (:h t))
               2))

  Long
  (area [n] (* n n))
  (perimeter [n] (* 2 n)))
