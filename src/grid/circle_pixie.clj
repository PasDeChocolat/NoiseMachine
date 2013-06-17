(ns grid.circle-pixie
  (:use [grid.setup :only [DEPTH_FAR_THRESH DEPTH_START_SECOND_LAYER]])
  (:require [grid.math.vector :as math-vector]
            [quil.core :as qc]))

(def MAX_THINGS 3)
(def MAX_HEIGHT 40)
(def MAX_VEL 3)

(defn up-down
  [x]
  (* x (cond
        (< (rand 1) 0.5) -1.0
        :else 1.0)))

(defn create-thing
  [depth]
  (let [d (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0.1 1.0)
        rvel (mapv up-down (mapv #(+ % 1.0) [(rand 100) (rand 100)]))
        nvel (math-vector/normalize rvel)
        [vel-x vel-y] (->> nvel
                           (math-vector/multiply (* d MAX_VEL))
                           (mapv #(+ % (rand (* 0.3 MAX_VEL)))))]
    {:x 0 :y 0 :height (+ 10 (rand MAX_HEIGHT)) :velocity [vel-x vel-y]}))

(defn init-things
  [depth]
  (let [max-things (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0 MAX_THINGS)
        max-things (int max-things)
        max-things (rand-int max-things)
        max-things (max 2 max-things)]
   (doall
    (vec
     (for [x (range 1 max-things)]
       (create-thing depth))))))

(defn draw-thing
  [pct-complete {:keys [x y height velocity] :or {y 0} :as thing}]
  (let [diameter (* pct-complete (* 1.5 height))
        [x-vel y-vel] velocity]
    ;; (qc/no-fill)
    (qc/fill 255 (* (- 1.0 pct-complete) 255))
    ;; (qc/stroke 255)
    ;; (qc/stroke-weight 0.5)
    (qc/ellipse x y diameter diameter)
    (-> thing
        (update-in [:x] #(+ % x-vel))
        (update-in [:y] #(+ % y-vel)))))

(defn draw-pixie
  [{:keys [x y depth health color initial-state data] :as pixie}]
  (qc/translate x y)
  (let [{things :things :or {things (init-things depth)}} data
        {initial-health :health} initial-state
        pct-complete (qc/map-range health initial-health 0 0.0 1.0)
        pct-left (- 1.0 pct-complete)
        alpha (* pct-left 255)]
    (apply qc/stroke (conj color alpha))
    (let [drawn-things (mapv (partial draw-thing pct-complete) things)]
      (assoc-in pixie [:data :things] drawn-things))))
