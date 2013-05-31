(ns grid.setup
  (:require [quil.core :as qc]
            [bifocals.core :as bifocals])
  (:use [grid.state :only [grid-sensors grid-state k-col-width k-row-height long-col-state]]))

(def WIDTH 1280.0)
(def HEIGHT 1024.0)

(def NCOLS 24)
(def NROWS 18)

(def MARGIN 20)
(def CW (/ WIDTH NCOLS))
(def RH (/ HEIGHT NROWS))

;; Long Columns Layer
(def NLONGCOLS 4)
(def LONG_COLS_START_COLS (filter #(= 0 (mod % (int (/ NCOLS NLONGCOLS))))
                                  (range NCOLS)))

;; (def DEPTH_FAR_THRESH 3500.0) ;; Museum setting 1
(def DEPTH_FAR_THRESH 4000.0)  ;; Museum setting 2
;; (def DEPTH_FAR_THRESH 2000.0)  ;; Kitchen setting
(def DEPTH_MAX 7000.0)
(def DEPTH_START_SECOND_LAYER 1500.0)

(defn setup []
  (qc/frame-rate 15)
  (.setMirror (bifocals/kinect) true)

  ;; Remember col/row sizes:
  (swap! k-col-width  (constantly (int (/ (bifocals/depth-width)  NCOLS))))
  (swap! k-row-height (constantly (int (/ (bifocals/depth-height) NROWS))))

  ;; Initialize grid state: true/false = on/off
  (dorun
   (for [col (range NCOLS)
         row (range NROWS)]
     (swap! grid-state #(assoc % [col row] false))))
  (dorun
   (for [col LONG_COLS_START_COLS]
     (swap! long-col-state #(assoc % col false))))

  ;; Initialize grid sensor state:
  (dorun
   (for [col (range NCOLS)
         row (range NROWS)
         :let [x (+ (* col CW) (rand-int CW))
               y (+ (* row RH) (rand-int RH))]]
     (swap! grid-sensors #(assoc % [col row] {:x x :y y}))))

  (qc/smooth)
  (qc/stroke 20)
  (qc/stroke-weight 1))
