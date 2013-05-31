(ns visuals.bloom
  (:require [quil.core :as qc]))

(def MAX_HEALTH 200)
(def BLOOM_TYPES [:bass :treble :harmony :single-line :multi-line])

(defn create-bloom
  [x y]
  (let [health (rand MAX_HEALTH)]
    {:x x :y y
     :type (nth BLOOM_TYPES (rand-int (count BLOOM_TYPES)))
     :health health
     :initial-state { :health health }
     :color [(rand 255) (rand 255) (rand 255)]
     :data {}}))

(defn clean-dead
  [blooms]
  (filterv #(< 0 (:health %)) blooms))

(defn update-bloom
  [bloom]
  (update-in bloom [:health] dec))
