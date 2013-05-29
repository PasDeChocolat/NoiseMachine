(ns grid.draw
  (:require [bifocals.core :as bifocals]
            [grid.color-schemes :as color-schemes]
            [grid.sound :as dynamic-sound]
            [overtone.inst.drum :as drum]
            [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH DEPTH_MAX HEIGHT LONG_COLS_START_COLS MARGIN NCOLS NLONGCOLS NROWS RH WIDTH grid-state k-col-width k-row-height long-col-state]]))

(defn turn-on-at [col row depth]
  (when-not (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] depth))
    (dynamic-sound/hit-at col row depth)))

(defn turn-off-at [col row]
  (if (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] false))))

(defn display-grid-element-at
  [x y]
  (qc/push-matrix)
  (qc/translate x y 0)
  (qc/box (- CW MARGIN) (- RH MARGIN) 10)
  ;; (qc/rect x y (- CW MARGIN) (- RH MARGIN))
  (qc/pop-matrix))

(defn choose-display-color
  [depth]
  (let [min-depth DEPTH_START_SECOND_LAYER
        alpha 160]
    (color-schemes/color-scheme-emperor-penguin depth min-depth DEPTH_MAX DEPTH_FAR_THRESH alpha)))

(defn display-at
  [x y col row depth]
  (choose-display-color depth)
  (display-grid-element-at x y)

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
  (qc/pop-matrix)
  )

(defn simple-depth-at
  [col row k-depth-map]
  (let [kx (* (+ col 0.5) @k-col-width)
        ky (* (+ row 0.5) @k-row-height)
        n (int (+ kx (* ky (bifocals/depth-width))))]
    (nth k-depth-map n)))

(def tick (atom 0))

(defn draw-simple
  [col row k-depth-map]
  (let [x (* col CW)
        y (* row RH)
        depth (simple-depth-at col row k-depth-map)
        depth (qc/constrain-float depth 0.0 DEPTH_MAX)]
    (display-at x y col row depth)))

(defn draw-grid-instrument
  [k-depth-map]
  (doall
   (if (even? @tick)
     (for [col (range NCOLS)
           row (range NROWS)
           :when (or
                  (and
                   (even? col)
                   (even? row))
                  (and
                   (odd? col)
                   (odd? row)))]
       (draw-simple col row k-depth-map))
     (for [col (range NCOLS)
           row (range NROWS)
           :when (or
                  (and
                   (odd? col)
                   (even? row))
                  (and
                   (even? col)
                   (odd? row)))]
       (draw-simple col row k-depth-map)))))

(defn draw-long-cols
  [k-depth-map]
  (let [start-cols LONG_COLS_START_COLS
        row 1
        long-width (/ WIDTH NLONGCOLS)
        long-height HEIGHT
        ]
    (doall
     (for [col start-cols]
       (let [grid-depth (simple-depth-at col row k-depth-map)]
         (when (< grid-depth DEPTH_START_SECOND_LAYER)
           (qc/fill 255 0 255 50)
           (qc/push-matrix)
           (qc/translate (* col CW) RH 0)
           (qc/box long-width long-height 10)
           ;; (qc/rect (* col CW) RH
           ;;          long-width long-height)
           (qc/pop-matrix)
           (when (and (= 0 (mod @tick 10)) (not (@long-col-state col)))
             (swap! long-col-state #(assoc % col true))
             (dynamic-sound/rhythm-hit-at col row grid-depth))))))))

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
  ;; (qc/background 0 0 0 1)
  (let [k-depth-map (.depthMap (bifocals/kinect))]
    (draw-grid-instrument k-depth-map)
    (draw-long-cols k-depth-map)))
