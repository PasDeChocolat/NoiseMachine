(ns grid.plus-pixie
  (:require [quil.core :as qc]))

(def TRAVEL_DIST 100)
(def MAX_THINGS 12)
(def MAX_HEIGHT 40)
(def MAX_VEL 5)

(defn create-thing
  []
  {:x 0 :y 0 :height (+ 10 (rand MAX_HEIGHT)) :velocity [(- (/ MAX_VEL 2.0) (rand MAX_VEL))
                                                         (- (/ MAX_VEL 2.0) (rand MAX_VEL))]})

(defn init-things
  []
  (doall
   (vec
    (for [x (range (rand-int MAX_THINGS))]
      (create-thing)))))

(defn draw-thing
  [pct-complete {:keys [x y height velocity] :or {y 0} :as thing}]
  (let [half-height (* 0.5 pct-complete height)
        [x-vel y-vel] velocity]
    (qc/line x (- y half-height) x (+ y half-height))
    (qc/line (- x half-height) y (+ x half-height) y)
    (-> thing
        (update-in [:x] #(+ % x-vel))
        (update-in [:y] #(+ % y-vel)))))

(defn draw-pixie
  [{:keys [x y health color initial-state data] :as pixie}]
  (qc/translate x y)
  (let [{things :things :or {things (init-things)}} data
        {initial-health :health} initial-state
        pct-complete (qc/map-range health initial-health 0 0.0 1.0)
        pct-left (- 1.0 pct-complete)
        alpha (* pct-left 255)]
    (apply qc/stroke (conj color alpha))
    (let [drawn-things (mapv (partial draw-thing pct-complete) things)]
      (assoc-in pixie [:data :things] drawn-things))))

