(ns grid.setup
  (:require [grid.state :as grid-state]
            [quil.core :as qc]
            [bifocals.core :as bifocals])
  (:use [grid.state :only [grid-sensors grid-state k-col-width k-row-height]]))

;; (def WIDTH 1400.0)
;; (def HEIGHT 1000.0)

;; 1080p
;; (def WIDTH 1920.0)
;; (def HEIGHT 1080.0)

;; 1920 x 1200 scaled
(def WIDTH 1920.0)
(def HEIGHT 1200.0)

;; (def NCOLS 24)
;; (def NROWS 18)
;; (def NCOLS 48)
;; (def NROWS 36)
(def NCOLS 36)
(def NROWS 27)

;; (setup-state)

(def MARGIN 20)
(def CW (/ WIDTH NCOLS))
(def RH (/ HEIGHT NROWS))

(def DEPTH_FAR_THRESH 4200.0)         ;; Museum setting 2
;; (def DEPTH_START_SECOND_LAYER 1500.0) ;;

;;(def DEPTH_FAR_THRESH 4000.0) ;; Kitchen setting

;; (def DEPTH_FAR_THRESH 2000.0) ;; Kitchen setting
;;(def DEPTH_START_SECOND_LAYER (/ DEPTH_FAR_THRESH 2.0))
(def DEPTH_START_SECOND_LAYER 1000)
(def DEPTH_MAX 7000.0)

(def MAX_SENSOR_BURST_HEALTH 30)

(defn setup-state
  []
  ;; Remember col/row sizes:
  (reset! k-col-width  (int (/ (bifocals/depth-width)  NCOLS)))
  (reset! k-row-height (int (/ (bifocals/depth-height) NROWS)))

  ;; Initialize grid state: true/false = on/off
  (dorun
   (for [col (range NCOLS)
         row (range NROWS)]
     (swap! grid-state #(assoc % [col row] false))))

  ;; Initialize grid sensor state:
  (dorun
   (for [col (range NCOLS)
         row (range NROWS)
         :let [x (+ (* col CW) (rand-int CW))
               y (+ (* row RH) (rand-int RH))]]
     (swap! grid-sensors #(assoc % [col row]
                                 {:x x :y y
                                  :burst {:health MAX_SENSOR_BURST_HEALTH
                                          :drop-y 0}})))))

(defn setup []
  (qc/frame-rate 30)
  (.setMirror (bifocals/kinect) true)

  (setup-state)
  
  (qc/smooth)
  (qc/stroke 20)
  (qc/stroke-weight 1))
