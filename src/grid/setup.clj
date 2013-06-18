(ns grid.setup
  (:require [grid.state :as grid-state]
            [quil.core :as qc]
            [bifocals.core :as bifocals])
  (:use [grid.state :only [grid-sensors grid-state k-col-width k-row-height note-grid]]))

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
                                  :burst {:health 0
                                          :drop-y 0}}))))

  ;; Create a note map to grid sectors.
  ;; lowest 60-24-9 = 27
  ;; highest 60+36+9 = 105

  (def NOTE_MIN 27)
  (def NOTE_MAX 105)
  (def MAX_BURST_LEN 60)
  (def MIN_BURST_LEN 10)

  (dorun
   (for [col (range NCOLS)
         row (range NROWS)
         :let [col-factor 2
               left-note (- 60 (int (/ NCOLS 2 col-factor)))
               the-note (int (+ (* (/ 1 col-factor) col) left-note))
               played? (= 0 (mod col col-factor))
               note (cond
                     (>= 2 row) (- the-note 24)
                     (>= 5 row) (- the-note 12)
                     (>= 10 row) the-note
                     (>= 15 row) (+ the-note 12)
                     (>= 20 row) (+ the-note 24)
                     (< 20 row) (+ the-note 36))
               max-len (qc/map-range note NOTE_MIN NOTE_MAX MAX_BURST_LEN MIN_BURST_LEN)]]
     (swap! note-grid #(assoc % [col row] {:note note :played? played? :max-len max-len})))))

(defn setup []
  (qc/frame-rate 30)
  (.setMirror (bifocals/kinect) true)

  (setup-state)
  
  (qc/smooth)
  (qc/stroke 20)
  (qc/stroke-weight 1))
