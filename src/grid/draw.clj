(ns grid.draw
  (:require [bifocals.core :as bifocals]
            [grid.color-schemes :as color-schemes]
            [grid.sound :as dynamic-sound]
            [overtone.inst.drum :as drum]
            [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH DEPTH_MAX HEIGHT LONG_COLS_START_COLS MARGIN NCOLS NLONGCOLS NROWS RH WIDTH grid-state k-col-width k-row-height]]))

(defn turn-on-at [col row depth]
  (when-not (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] depth))
    (dynamic-sound/hit-at col row depth)))

(defn turn-off-at [col row]
  (if (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] false))))

(defn display-grid-element-at
  [x y]
  (qc/rect x y (- CW MARGIN) (- RH MARGIN)))

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
  (qc/rect x y 5 5)
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
        row 0
        long-width (/ WIDTH NLONGCOLS)]
    (doall
     (for [col start-cols]
       (let [grid-depth (simple-depth-at col row k-depth-map)]
         (when (< grid-depth DEPTH_START_SECOND_LAYER)
           (qc/rect (* col @k-col-width) 0
                    long-width HEIGHT)
           (comment (when (= 0 (mod @tick 5))
              (dynamic-sound/rhythm-hit-at col row grid-depth)))))))))

(defn draw []
  (bifocals/tick)
  (swap! tick inc)
  (let [k-depth-map (.depthMap (bifocals/kinect))]
    (draw-grid-instrument k-depth-map)
    (draw-long-cols k-depth-map)))
