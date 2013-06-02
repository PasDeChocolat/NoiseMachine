(ns grid.draw
  (:require [bifocals.core :as bifocals]
            [grid.color-schemes :as color-schemes]
            [grid.draw-sensors :as draw-sensors]
            [grid.pixies :as pixies]
            [grid.sound :as dynamic-sound]
            [overtone.inst.drum :as drum]
            [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH DEPTH_MAX HEIGHT LONG_COLS_START_COLS MARGIN NCOLS NLONGCOLS NROWS RH WIDTH]]
        [grid.state :only [grid-state k-col-width k-row-height tick]]))

(defn turn-on-at [col row depth]
  (when-not (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] depth))
    (dynamic-sound/hit-at col row depth)
    (pixies/add-pixie-at col row)))

(defn turn-off-at [col row]
  (if (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] false))))

(defn display-on-off-indicator-at
  [x y col row depth]
  ;; Draw on/off indicator square:
  (cond
   (and (> depth DEPTH_START_SECOND_LAYER) (< depth DEPTH_FAR_THRESH)) (turn-on-at col row depth)
   :default (turn-off-at col row))
  (cond
   (@grid-state [col row]) (qc/fill 0 255 0 80)
   :default (qc/fill 255 0 0 80))
  (qc/push-matrix)
  (qc/push-style)
  (qc/translate (+ 5 (- x (/ CW 2))) (+ 5 (- y (/ RH 2))) 0)
  (qc/no-stroke)
  (qc/box 2)
  ;; (qc/rect x y 5 5)
  (qc/pop-style)
  (qc/pop-matrix))

(defn display-ghost-column
  [w h z]
  (qc/translate 0 0 (* -0.5 z))
  (qc/fill 100 150)
  (qc/box w h z))

(defn display-grid-element-at
  [x y depth]
  (qc/push-matrix)
  (let [w (- CW MARGIN)
        h (- RH MARGIN)
        z (qc/map-range depth 0 DEPTH_MAX 400 0)]
    (qc/translate x y z)
    (qc/box w h 1)
    (display-ghost-column w h z))
  (qc/pop-matrix))

(defn choose-display-color
  [depth]
  (let [min-depth DEPTH_START_SECOND_LAYER
        alpha 160
        alpha 255
        ]
    (color-schemes/color-scheme-emperor-penguin depth min-depth DEPTH_MAX DEPTH_FAR_THRESH alpha)))

(defn simple-depth-at
  [col row k-depth-map]
  (let [kx (* (+ col 0.5) @k-col-width)
        ky (* (+ row 0.5) @k-row-height)
        n (int (+ kx (* ky (bifocals/depth-width))))]
    (nth k-depth-map n)))

(defn display-at
  [col row k-depth-map]
  (let [x (* (- NCOLS col 1) CW)
        y (* row RH)
        depth (simple-depth-at col row k-depth-map)
        depth (qc/constrain-float depth 0.0 DEPTH_MAX)]
    (comment (when (> depth 1) 
       (choose-display-color depth)
       (display-grid-element-at x y depth)))
    
    (draw-sensors/display-sensor-element-at col row depth)  
    (display-on-off-indicator-at x y col row depth)))

(defn draw-grid-instrument
  [k-depth-map]
  (doall
   (for [col (range NCOLS)
         row (range NROWS)]
     (display-at col row k-depth-map))))

(defn draw []
  (bifocals/tick)
  (swap! tick inc)

  ;; Lights!
  (qc/lights)

  ;; Perspective!
  (comment (let [fov (/ Math/PI 3.0)
         camera-z (/ (/ (qc/height) 2.0) (qc/tan (/ fov 2.0)))
         wh-ratio (/ (qc/width) (qc/height) )]
     (qc/perspective fov wh-ratio (/ camera-z 2.0) (* camera-z 2.0))))

  ;; Camera!
  (let [
        ;; eye-x (/ (qc/width) 2.0)
        eye-x (qc/mouse-x)
        eye-y (/ (qc/height) 2.0)
        ;; eye-z (/ (/ (qc/height) 2.0) (qc/tan (/ Math/PI 6.0)))
        eye-z (/ (/ (qc/height) 2.0) (qc/tan (/ (* Math/PI 60.0) 360.0)))
        zoom-factor (qc/map-range (qc/mouse-y) 0 (qc/height) 0 1.0)
        eye-z (* zoom-factor (* 2.0 eye-z))
        center-x (/ (qc/width) 2.0)
        center-y (/ (qc/height) 2.0)
        center-z 0
        up-x 0
        up-y 1
        up-z 0]
    (qc/camera eye-x eye-y eye-z center-x center-y center-z up-x up-y up-z))
  
  (qc/no-stroke)
  (qc/background 0 0 0 255)
  (let [k-depth-map (.depthMap (bifocals/kinect))]
    (draw-sensors/draw-sensor-grid)
    (draw-grid-instrument k-depth-map)
    (pixies/draw-all-pixies)))
