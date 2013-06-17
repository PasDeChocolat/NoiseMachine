(ns grid.pixies
  (:use [grid.setup :only [DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH]]
        [grid.state :only [all-pixies grid-sensors]])
  (:require [grid.draw-pixie :as draw-pixie]
            [quil.core :as qc]))

(def MAX_HEALTH_MAX 12)
(def MAX_HEALTH_MIN 8)
(def PIXIE_TYPES [:plus :circle])

(defn create-pixie-at-coords
  [x y depth]
  (let [d (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0.1 1.0)
        ;; health (rand (* d MAX_HEALTH))
        health (qc/map-range d 0.1 1.0 MAX_HEALTH_MAX MAX_HEALTH_MIN)]
    {:x x :y y
     :depth depth
     ;; :type (nth PIXIE_TYPES (rand-int (count PIXIE_TYPES)))
     :type :circle
     :health health
     :initial-state { :health health }
     :color [(rand 255) (rand 255) (rand 255)]
     :data {}}))

(defn clean-dead
  [pixies]
  (filterv #(< 0 (:health %)) pixies))

(defn update-pixie
  [pixie]
  (update-in pixie [:health] dec))

(defn draw-all-pixies
  []
  (reset! all-pixies (->> @all-pixies
                          (mapv draw-pixie/draw-pixie)
                          (mapv update-pixie)
                          (clean-dead))))

(defn add-pixie-at
  [col row depth]
  (let [{:keys [x y burst]} (@grid-sensors [col row])
        {:keys [drop-y]} burst
        new-pixie (create-pixie-at-coords x (+ y drop-y) depth)]
    (swap! all-pixies #(conj % new-pixie))))
