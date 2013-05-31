(ns grid.simple-grid
  (:require [quil.core :as qc]
            [bifocals.core :as bifocals]))

(def WIDTH 640.0)
(def HEIGHT 480.0)
(def NCOLS 96)
(def NROWS 72)
(def MARGIN 0)
(def CW (/ WIDTH NCOLS))
(def RH (/ HEIGHT NROWS))

(defn setup []
  (qc/frame-rate 15)
  (.setMirror (bifocals/kinect) true))

(defn draw []
  (bifocals/tick)
  (qc/stroke 20)
  (qc/stroke-weight 1)
  (let [k-depth-map (.depthMap (bifocals/kinect))]
    (doall
     (for [col (range NCOLS)
           row (range NROWS)
           :let [x (* col CW)
                 y (* row RH)
                 kx (qc/map-range x 0 WIDTH 0 (bifocals/depth-width))]]
       (do
         (let [depth (nth k-depth-map (+ kx (* (bifocals/depth-width) row)))]
           (qc/fill (qc/map-range depth 0 3000 255 0) 255))
         (qc/rect x y (- CW MARGIN) (- RH MARGIN)))))))

(defn run []
  (qc/defsketch grid
    :title "Grid"
    :setup setup
    :draw draw
    :size [WIDTH HEIGHT]))
