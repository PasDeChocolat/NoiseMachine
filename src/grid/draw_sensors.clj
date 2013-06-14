(ns grid.draw-sensors
  (:require [grid.color-schemes :as color-schemes]
            [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_FAR_THRESH DEPTH_START_SECOND_LAYER DEPTH_MAX NCOLS RH WIDTH]]
        [grid.state :only [grid-sensors tick]]))

(defn draw-sensor-point
  [t [[col row] { :keys [x y]}]]
  (let [t (* t 0.1)
        n (qc/noise col row t)
        r (+ 1 (* n 4))]
    (qc/fill 255 255)
    (qc/ellipse x y r r)))

(defn update-sensor-point
  [tick-atom new-grid [[col row :as col-row] { :keys [x y] :as sensor}]]
  (let [t (* 0.01 @tick-atom)
        t (+ (rand 0.01) t) ;; Make a randomness of points a little smoother.
        pos (+ col (* NCOLS row))
        new-x (+ (* (- NCOLS col 1) CW) (* CW (qc/noise pos t)))
        new-y (+ (* row RH) (* RH (qc/noise pos (+ 5.2 t))))
        new-sensor (-> (assoc sensor :x new-x)
                       (assoc :y new-y))]
    (assoc new-grid col-row new-sensor)))

(defn draw-sensor-grid
  []
  (dorun
   (map (partial draw-sensor-point @tick) @grid-sensors))
  (let [new-grid (doall
                  (reduce (partial update-sensor-point tick) {} @grid-sensors))]
    (reset! grid-sensors new-grid)))

(defn display-sensor-element-at
  [col row depth pct-on]
  (let [{:keys [x y] :or {x 0 y 0}} (@grid-sensors [col row])
        ;; diameter (qc/map-range depth 0 DEPTH_MAX 10 20)
        ]
    (when (and (> depth DEPTH_START_SECOND_LAYER) (< depth DEPTH_FAR_THRESH))
      (qc/push-style)
      (qc/push-matrix)
      (qc/color-mode :hsb 0.0 1.0 1.0 1.0)
      (let [hue 276.0
            sat (qc/map-range pct-on 0.0 1.0 1.0 0.0)
            val (qc/map-range pct-on 0.0 1.0 0.4 1.0)
            alpha 1.0]
        (qc/fill hue sat val alpha))
      (qc/translate x y)
      (let [half-side 5
            half-h 4.33
            theta (qc/noise col row (* 0.2 @tick))]
        (qc/rotate theta)
        (qc/triangle (- half-side) half-h 0 (- half-h) half-side half-h))
      (qc/pop-matrix)
      (qc/pop-style))))
