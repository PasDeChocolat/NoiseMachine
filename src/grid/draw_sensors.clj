(ns grid.draw-sensors
  (:require [grid.color-schemes :as color-schemes]
            [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_FAR_THRESH DEPTH_START_SECOND_LAYER DEPTH_MAX NCOLS RH]]
        [grid.state :only [grid-sensors tick]]))

(defn draw-sensor-point
  [x y col row]
  (let [t (* @tick 0.1)
        n (qc/noise col row t)
        r (+ 1 (* n 4))]
    (qc/fill 255 255)
    (qc/ellipse x y r r)))

(defn update-sensor-point
  [{:keys [x y] :as sensor} [col row] t]
  (let [t (+ (rand 0.01) t) ;; Make a randomness of points little smoother.
        pos (+ col (* NCOLS row))
        new-x (+ (* (- NCOLS col 1) CW) (* CW (qc/noise pos t)))
        new-y (+ (* row RH) (* RH (qc/noise pos (+ 5.2 t))))]
    (do
      (draw-sensor-point new-x new-y col row)
      (-> sensor
          (assoc-in [:x] new-x)
          (assoc-in [:y] new-y)))))

(defn draw-sensor-points
  []
  (let [t (* 0.01 @tick)]
    (dorun
     (for [col-row (keys @grid-sensors)
           :let [sensor (@grid-sensors col-row)
                 updated (update-sensor-point sensor col-row t)]]
       (swap! grid-sensors #(assoc-in % [col-row] updated))))))

(defn draw-sensor-grid
  []
  (draw-sensor-points))

(defn display-sensor-element-at
  [col row depth pct-on]
  (let [{:keys [x y] :or {x 0 y 0}} (@grid-sensors [col row])
        r (qc/map-range depth 0 DEPTH_MAX 10 20)]
    (when (and (> depth DEPTH_START_SECOND_LAYER) (< depth DEPTH_FAR_THRESH))
      (qc/push-style)
      (qc/color-mode :hsb 0.0 1.0 1.0 1.0)
      (let [hue 276.0
            sat (qc/map-range pct-on 0.0 1.0 1.0 0.0)
            val (qc/map-range pct-on 0.0 1.0 0.4 1.0)
            alpha 1.0]
        (qc/fill hue sat val alpha))
      (qc/ellipse x y r r)
      (qc/pop-style))))
