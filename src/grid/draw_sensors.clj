(ns grid.draw-sensors
  (:require [grid.color-schemes :as color-schemes]
            [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH DEPTH_MAX HEIGHT LONG_COLS_START_COLS MARGIN NCOLS NLONGCOLS NROWS RH WIDTH]]
        [grid.state :only [grid-sensors tick]]))

;; remove unused things from "use"

(def SENSOR_LINE_WIDTH 5.0)

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

(defn draw-sensor-line
  [n1 n2]
  (let [k (keys @grid-sensors)
        p1 (@grid-sensors (nth k n1))
        p2 (@grid-sensors (nth k n2))
        {x1 :x y1 :y} p1
        {x2 :x y2 :y} p2
        m (/ (- y2 y1) (- x2 x1))
        b (- y1 (* m x1))
        offset 1000
        x-left (- offset)
        y-left (+ (* m x-left) b)
        x-right (+ WIDTH offset)
        y-right (+ (* m x-right) b)]
    (qc/stroke 255 50)
    (qc/stroke-weight SENSOR_LINE_WIDTH)
    (qc/line x-left y-left x-right y-right)))

(defn draw-sensor-lines
  []
  (let [num-sensors (count (keys @grid-sensors))]
    (dorun
     (for [x (range num-sensors)
           :when (= 0 (mod x 10))
           :let [y (mod (+ 500 x) num-sensors)]]
       (draw-sensor-line x y)
       ))))

(defn draw-sensor-grid
  []
  (draw-sensor-points)
  ;; (draw-sensor-lines)
  )

(defn display-sensor-element-at
  [col row depth]
  (let [{:keys [x y] :or {x 0 y 0}} (@grid-sensors [col row])
        a (qc/map-range depth 0 DEPTH_MAX 255 0)
        r (qc/map-range depth 0 DEPTH_MAX 10 0)]
    (qc/fill 255 a)
    (when (< depth DEPTH_FAR_THRESH)
      (color-schemes/color-scheme-emperor-penguin depth 100)
      (qc/ellipse x y r r))))
