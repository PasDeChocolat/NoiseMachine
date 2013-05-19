(ns grid.draw
  (:require [quil.core :as qc]
            [bifocals.core :as bifocals]
            [overtone.inst.drum :as drum])
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

(defn color-scheme-disney-blues
  [scaled-depth alpha]
  (cond
   (> scaled-depth 240) (qc/fill 15 201 242 alpha)
   (> scaled-depth 220) (qc/fill 13 159 217 alpha)
   (> scaled-depth 200) (qc/fill 17 124 217 alpha)
   (> scaled-depth 170) (qc/fill 17 104 217 alpha)
   :default  (qc/fill 18 60 193 alpha)))

(defn color-scheme-healthy-yogurt
  [scaled-depth alpha]
  (cond
   (> scaled-depth 240) (qc/fill 191 191 176 alpha)
   (> scaled-depth 220) (qc/fill 239 242 148 alpha)
   (> scaled-depth 200) (qc/fill 125 166 106 alpha)
   (> scaled-depth 170) (qc/fill 29 78 89 alpha)
   :default  (qc/fill 25 38 64 alpha)))

(defn color-scheme-etro-wallpaper6
  [scaled-depth alpha]
  (cond
   ;; (< scaled-depth 10)  (qc/fill 0 255 0 alpha)
   ;; (> scaled-depth 250) (qc/fill 255 0 0 alpha)
   (> scaled-depth 240) (qc/fill 242 233 216 alpha)
   (> scaled-depth 220) (qc/fill 217 209 186 alpha)
   (> scaled-depth 200) (qc/fill 128 148 166 alpha)
   (> scaled-depth 170) (qc/fill 76 97 115 alpha)
   :default  (qc/fill 28 47 64 alpha)))

(defn color-scheme-font-love
  [scaled-depth alpha]
  (cond
   (> scaled-depth 240) (qc/fill 162 105 254 alpha)
   (> scaled-depth 220) (qc/fill 132 78 233 alpha)
   (> scaled-depth 200) (qc/fill 101 51 203 alpha)
   (> scaled-depth 170) (qc/fill 71 24 173 alpha)
   :default  (qc/fill 42 1 152 alpha)))

(defn color-scheme-emperor-penguin
  [scaled-depth alpha]
  (cond
   (> scaled-depth 240) (qc/fill 254 172 0 alpha)
   (> scaled-depth 220) (qc/fill 252 216 43 alpha)
   (> scaled-depth 200) (qc/fill 94 111 106 alpha)
   (> scaled-depth 170) (qc/fill 28 68 88 alpha)
   :default  (qc/fill 1 40 64 alpha)))

(defn choose-display-color [depth]
  
  (let [g (qc/map-range depth 0 DEPTH_MAX 255 0)
        alpha 160]
    ;; (color-scheme-disney-blues g alpha)
    ;; (color-scheme-healthy-yogurt g alpha)
    ;; (color-scheme-etro-wallpaper6 g alpha)
    ;; (color-scheme-font-love g alpha)
    (color-scheme-emperor-penguin g alpha)
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
