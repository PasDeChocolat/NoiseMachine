(ns grid.sound
  (:use [grid.setup :only [DEPTH_FAR_THRESH DEPTH_START_SECOND_LAYER NCOLS NROWS]]
        [grid.state :only [at-at-pool long-col-state]]
        [overtone.live])
  (:require [grid.harpsichord :as gharp]
            [overtone.inst.drum :as drum]
            [overtone.at-at :as at]
            [quil.core :as qc]))

;;
;; Ideas:
;;  - If sector is commonly used, it could have it's volume degrade
;;  with use.

(defn hit-at-bing
  [col row depth]
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

(defn hit-at-harpsichord
  [col row depth]
  (let [col-factor 2
        left-note (- 60 (int (/ NCOLS 2 col-factor)))
        the-note (int (+ (* (/ 1 col-factor) col) left-note))
        ;;_ (println "the-note:" the-note)
        duration  (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 0.5 50)
        ;; duration  (qc/map-range depth DEPTH_START_SECOND_LAYER DEPTH_FAR_THRESH 100 1)
        ]
    (when (and
           (= 0 (mod col col-factor)))
      (cond
       (= 3 row) (gharp/play-single-note-by-int  (- the-note 12) duration)
       (= 8 row) (gharp/play-single-note-by-int the-note duration)
       (= 13 row) (gharp/play-single-note-by-int (+ the-note 12) duration)
       (= 18 row) (gharp/play-single-note-by-int (+ the-note 24) duration)
       (= 23 row) (gharp/play-single-note-by-int (+ the-note 36) duration)))))

(defn hit-at
  [col row depth]
  ;; (hit-at-bing col row depth)
  (hit-at-harpsichord col row depth))

;; Binging
;; (defn bing-with
;;   [freq]
;;   (drum/bing :amp 2.0 :freq freq :attack 0.01 :decay 0.5))

;; (defn rhythm-hit-at-dispatch [col row depth]
;;   :bing)

;; (defmulti rhythm-hit-at
;;   "Play the rhythm part of this thing."
;;   #'rhythm-hit-at-dispatch
;;   :default :bing)

;; (defmethod rhythm-hit-at :bing [col row depth]
;;   (let [freq (qc/map-range col 0 (last LONG_COLS_START_COLS) 120.0 180.0)
;;         timing [0 400 800 1600 400 800 400]
;;         last-index (dec (count timing))
;;         do-bing (fn [index]
;;                   (if (= index last-index)
;;                     (swap! long-col-state #(assoc % col false)))
;;                   (bing-with freq)
;;                   )]
;;     ;; (drum/bing :amp 2.0 :freq freq :attack 0.01 :decay 0.5)
;;     (dorun (map-indexed #(at/at (+ (at/now) %2) (do-bing %1) at-at-pool) timing))
;;     ))

;; (defmethod rhythm-hit-at :kick [col row depth]
;;   (let [freq (qc/map-range col 0 (last LONG_COLS_START_COLS) 200.0 250.0)]
;;     (drum/kick :amp 0.00001 :freq freq :attack 0.01 :decay 0.5)))

;; (drum/bing :amp 2.0 :freq 120.0 :attack 0.01 :decay 0.5)
;; (drum/kick :amp 2.0 :freq 200.0 :attack 0.01 :decay 0.5)
;; (drum/dance-kick)
;; (dorun (map #(at (+ (now) %) (drum/bing :amp 2.0 :freq 180)) [0 200 400 800 200 400 200]))
