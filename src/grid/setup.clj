(ns grid.setup
  (:require [quil.core :as qc]
            [bifocals.core :as bifocals]))

(def GRID_SETS 1)
(def GRID_SET_WIDTH 640.0)
(def GRID_SET_HEIGHT 480.0)
(def WIDTH (* GRID_SETS GRID_SET_WIDTH))
(def HEIGHT (* GRID_SETS GRID_SET_HEIGHT))

(def GRID_SET_COLS 16)
(def GRID_SET_ROWS 12)
;; (def NCOLS 64)
;; (def NROWS 48)

(def NCOLS (* GRID_SETS GRID_SET_COLS))
(def NROWS (* GRID_SETS GRID_SET_ROWS))

(def MARGIN 0)
(def CW (/ WIDTH NCOLS))
(def RH (/ HEIGHT NROWS))

;; (def DEPTH_OFF_THRESH 500)
;; (def DEPTH_ON_THRESH 2000)
;; (def DEPTH_FAR_THRESH 1400.0)
;; (def DEPTH_FAR_THRESH 3500.0) // Museum setting 1
(def DEPTH_FAR_THRESH 2000.0)
(def DEPTH_MAX 7000.0)

;; Dirty, Dirty, STATE
(def k-col-width (atom 0))
(def k-row-height (atom 0))
(def grid-state (atom {}))

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
  
  (qc/stroke 20)
  (qc/stroke-weight 1))