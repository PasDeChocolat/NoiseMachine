(ns grid.sound
  (:require [overtone.inst.drum :as drum]
            [quil.core :as qc])
  (:use [grid.setup :only [DEPTH_FAR_THRESH LONG_COLS_START_COLS NCOLS NROWS]]))
;;
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

(defn rhythm-hit-at-dispatch [col row depth]
  :bing)

(defmulti rhythm-hit-at
  "Play the rhythm part of this thing."
  #'rhythm-hit-at-dispatch
  :default :bing)

(defmethod rhythm-hit-at :bing [col row depth]
  (let [freq (qc/map-range col 0 (last LONG_COLS_START_COLS) 120.0 180.0)]
    (drum/bing :amp 2.0 :freq freq :attack 0.01 :decay 0.5)))

(defmethod rhythm-hit-at :kick [col row depth]
  (let [freq (qc/map-range col 0 (last LONG_COLS_START_COLS) 200.0 250.0)]
    (drum/kick :amp 0.00001 :freq freq :attack 0.01 :decay 0.5)))

;; (drum/bing :amp 2.0 :freq 120.0 :attack 0.01 :decay 0.5)
;; (drum/kick :amp 2.0 :freq 200.0 :attack 0.01 :decay 0.5)
;; (drum/dance-kick)