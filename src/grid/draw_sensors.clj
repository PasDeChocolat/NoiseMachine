(ns grid.draw-sensors
  (:require [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH DEPTH_MAX HEIGHT LONG_COLS_START_COLS MARGIN NCOLS NLONGCOLS NROWS RH WIDTH]]
        [grid.state :only [grid-sensors tick]]))

;; remove unused things from "use"

(def SENSOR_WIDTH 5.0)

(defn draw-sensor-point
  [x y]
  (qc/ellipse x y 2 2))

(defn update-sensor-point
  [{:keys [x y] :as sensor} [col row] t]
  (let [new-x (+ (* col CW) (* CW (qc/noise col row t)))
        new-y (+ (* row RH) (* RH (qc/noise col row (inc t))))]
    (do
      (draw-sensor-point new-x new-y)
      (-> sensor
          (assoc-in [:x] new-x)
          (assoc-in [:y] new-y)))))

(defn draw-sensor-points
  []
  (let [t (* 0.02 @tick)]
    (dorun
     (for [col-row (keys @grid-sensors)
           :let [sensor (@grid-sensors col-row)
                 updated (update-sensor-point sensor col-row t)]]
       (swap! grid-sensors #(assoc-in % [col-row] updated))))))

(defn draw-sensor-line
  [n1 n2]
  (let [k (keys @grid-sensors)
        p1 (@grid-sensors (nth k n1))
        p2 (@grid-sensors (nth k n2))
        {x1 :x y1 :y} p1
        {x2 :x y2 :y} p2]
    (qc/stroke 255 50)
    (qc/stroke-weight SENSOR_WIDTH)
    (qc/line x1 y1 x2 y2)
    ))

(defn draw-sensor-lines
  []
  (draw-sensor-line 1 5)
  )

(defn draw-sensor-grid
  []
  (draw-sensor-points)
  (draw-sensor-lines))
