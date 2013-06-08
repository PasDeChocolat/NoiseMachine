(ns grid.math.vector
  (:require [clojure.math.numeric-tower :as math]))

(defn add [& vs]
  (apply mapv + vs))

(defn subtract [& vs]
  (apply mapv - vs))

(defn magnitude [v]
 (math/sqrt (reduce + (map #(math/expt % 2) v))))

(defn normalize [v]
  (let [m (magnitude v)]
    (mapv #(/ % m) v)))

(defn multiply [scalar v]
  (mapv * (repeat scalar) v))

(defn divide [v scalar]
  (mapv / v (repeat scalar)))

(defn limit [upper v]
  (let [m (magnitude v)]
    (if (> m upper)
      (multiply upper (normalize v))
      v)))

(defn set-magnitude [mag v]
  (multiply mag (normalize v)))

(defn random-2d []
  (normalize [(- (rand 2) 1.0) (- (rand 2) 1.0)]))
