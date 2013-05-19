(ns grid.draw
  (:require [bifocals.core :as bifocals]
            [grid.color-schemes :as color-schemes]
            [overtone.inst.drum :as drum]
            [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_FAR_THRESH DEPTH_MAX MARGIN NROWS NCOLS RH grid-state k-col-width k-row-height]]))

;; Ideas:
;;  - If sector is commonly used, it could have it's volume degrade
;;  with use.
(defn- hit-at-dispatch [col row depth]
  (cond
   true :bing
   ;; true :piano
   :default :bing))

(defmulti hit-at
  "Play sound, determined by location of movement."
  #'hit-at-dispatch
  :default :bing)

(defmethod hit-at :bing [col row depth]
  (let [
        d (qc/map-range depth 0 DEPTH_FAR_THRESH 0.0 1000.0)
        d (qc/constrain-float d 0.0 1000.0)
        
        ;; amp (qc/map-range d 0.0 1000.0 5.8 0.05)
        amp (qc/map-range row 0 NROWS 0.5 0.01)
        amp (qc/constrain-float amp 0.0 0.8)
        ;; amp 0.4

        freq (qc/map-range col 0 NCOLS 100.0 800.0)
        
        ;; attack (qc/map-range d 0.0 1000.0 1.0 0.01)
        ;; attack (qc/map-range row 0 NROWS 0.5 0.0001)
        attack (qc/map-range row 0 NROWS 0.0001 0.2)
        ;; attack 0.001

        ;; decay 0.1
        decay (qc/map-range d 0.0 1000.0 0.01 1.0)
        ;; decay (qc/map-range d 0.0 1000.0 10.0 0.1)
        decay (qc/constrain-float decay 0.1 1.0)
        ]
    (drum/bing :amp amp :freq freq :attack attack :decay decay)))

(defn turn-on-at [col row depth]
  (when-not (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] true))
    (hit-at col row depth)))

(defn turn-off-at [col row]
  (if (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] false))))

(defn display-grid-element-at
  [x y]
  (qc/rect x y (- CW MARGIN) (- RH MARGIN)))

(defn choose-display-color
  [depth]
  (let [alpha 160]
    (color-schemes/color-scheme-emperor-penguin depth DEPTH_MAX DEPTH_FAR_THRESH alpha)
    ;; (qc/fill g 160)
    ))

(defn display-at
  [x y col row depth]
  (choose-display-color depth)
  ;; (qc/rect x y (- CW MARGIN) (- RH MARGIN))
  (display-grid-element-at x y)
  ;; (display-grid-element-at x y col row)

  ;; Draw on/off indicator square:
  (cond
   (> depth DEPTH_FAR_THRESH) (turn-off-at col row)
   (< depth DEPTH_FAR_THRESH) (turn-on-at col row depth))
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

(defn draw []
  (bifocals/tick)
  (swap! tick inc)
  (let [k-depth-map (.depthMap (bifocals/kinect))]
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
         (draw-simple col row k-depth-map))))))
