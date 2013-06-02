(ns grid.pixies
  (:use [grid.state :only [all-pixies grid-sensors]])
  (:require [grid.draw-pixie :as draw-pixie]
            [quil.core :as qc]))

(def MAX_HEALTH 20)
(def PIXIE_TYPES [:plus])

(defn create-pixie-at-coords
  [x y]
  (let [health (rand MAX_HEALTH)]
    {:x x :y y
     :type (nth PIXIE_TYPES (rand-int (count PIXIE_TYPES)))
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

(defn update-draw-pixies
  [pixies]
  (mapv update-pixie
        (mapv draw-pixie/draw-pixie pixies)))

(defn draw-all-pixies
  []
  (let [pixies (doall (update-draw-pixies @all-pixies)) ]
    (reset! all-pixies (clean-dead pixies))))

(defn add-pixie-at
  [col row]
  (let [{:keys [x y]} (@grid-sensors [col row])
        new-pixie (create-pixie-at-coords x y)]
    (swap! all-pixies #(conj % new-pixie))))
