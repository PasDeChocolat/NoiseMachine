(ns grid.circle-pixie
  (:use [grid.setup :only [DEPTH_FAR_THRESH DEPTH_START_SECOND_LAYER]])
  (:require [grid.math.vector :as math-vector]
            [quil.core :as qc]))

(def MAX_THINGS 3)
(def MAX_HEIGHT_MAX 40)
(def MAX_HEIGHT_MIN 20)
(def MAX_VEL_MIN 0.5)
(def MAX_VEL_MAX 10.0)

(defn up-down
  [x]
  (* x (cond
        (< (rand 1) 0.5) -1.0
        :else 1.0)))

(defn create-thing
  [depth]
  (let [
        ;;d (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0.1 1.0)
        ;;d (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0.5 10.0)
        d (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0.1 1.0)
        v-factor (qc/map-range d 0.1 1.0 MAX_VEL_MIN MAX_VEL_MAX)
        h (qc/map-range d 0.1 1.0 MAX_HEIGHT_MIN MAX_HEIGHT_MAX)
        rvel (mapv up-down (mapv #(+ % 1.0) [(rand 100) (rand 100)]))
        nvel (math-vector/normalize rvel)
        [vel-x vel-y] (->> nvel
                           (math-vector/multiply v-factor)
                           ;; (mapv #(+ % (rand (* 0.3 MAX_VEL))))
                           )
        ;; h (* h-factor (+ 10 (rand MAX_HEIGHT)))
        ]
    {:x 0 :y 0 :height h :velocity [vel-x vel-y]}
    ))

(defn init-things
  [depth]
  (let [
        ;; max-things (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0 MAX_THINGS)
        max-things MAX_THINGS
        max-things (int max-things)
        max-things (rand-int max-things)
        max-things (max 2 max-things)]
   (doall
    (vec
     (for [x (range 1 max-things)]
       (create-thing depth))))))

(defn draw-thing
  [pct-complete depth {:keys [x y height velocity] :or {y 0} :as thing}]
  (let [
        d (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0.5 2.0)
        diameter (* d pct-complete (* 1.5 height))
        [x-vel y-vel] velocity]
    ;; (qc/no-fill)
    (qc/fill 255 (* (- 1.0 pct-complete) 255))
    ;; (qc/stroke 255)
    ;; (qc/stroke-weight 0.5)
    (qc/ellipse x y diameter diameter)
    (-> thing
        (update-in [:x] #(+ % x-vel))
        (update-in [:y] #(+ % y-vel)))))

;; Each pixie is made up of multiple "things"
(defn draw-pixie
  [{:keys [x y depth health color initial-state data] :as pixie}]
  (qc/translate x y)
  (let [{things :things :or {things (init-things depth)}} data
        {initial-health :health} initial-state
        pct-complete (qc/map-range health initial-health 0 0.0 1.0)
        pct-left (- 1.0 pct-complete)
        alpha (* pct-left 255)]
    (apply qc/stroke (conj color alpha))
    (let [drawn-things (mapv (partial draw-thing pct-complete depth) things)]
      (assoc-in pixie [:data :things] drawn-things))))
