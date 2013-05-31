(ns grid.core
  (:require [quil.core :as qc]
            [bifocals.core :as bifocals]))

(def WIDTH 640.0)
(def HEIGHT 480.0)
;; (def NCOLS 64)
;; (def NROWS 48)
(def NCOLS 32)
(def NROWS 24)

(def MARGIN 0)
(def CW (/ WIDTH NCOLS))
(def RH (/ HEIGHT NROWS))

(def k-col-width (atom 0))
(def k-col-height (atom 0))

(defn setup []
  (qc/frame-rate 15)
  (.setMirror (bifocals/kinect) true)
  (swap! k-col-width  (constantly (int (/ (bifocals/depth-width)  NCOLS))))
  (swap! k-col-height (constantly (int (/ (bifocals/depth-height) NROWS))))

  (qc/stroke 20)
  (qc/stroke-weight 1))

(defn display-at
  [x y depth]
  (qc/fill (qc/map-range depth 0 2048 255 0) 255)
  (qc/rect x y (- CW MARGIN) (- RH MARGIN)))

(defn avg-depth-at
  [col row k-depth-map]
  (let [step 5
        all-d (for [kx (range (* col @k-col-width)  (* (inc col) @k-col-width))
                    ky (range (* row @k-col-height) (* (inc row) @k-col-height))
                    :when (and (= 0 (mod kx step))
                               (= 0 (mod ky step)))
                    :let [n (+ kx (* ky (bifocals/depth-width)))
                          depth (nth k-depth-map n)]]
                depth)]
    (/ (apply + all-d) (count all-d))))

(defn draw []
  (bifocals/tick)
  (let [k-depth-map (.depthMap (bifocals/kinect))]
    (doall
     (for [col (range NCOLS)
           row (range NROWS)
           :let [x (* col CW)
                 y (* row RH)
                 n (+ (* col @k-col-width) (* row (bifocals/depth-width)))
                 ;; depth (nth k-depth-map n)
                 depth (avg-depth-at col row k-depth-map)]]
       (display-at x y depth)))))

(defn run []
  (qc/defsketch grid
    :title "Grid"
    :setup setup
    :draw draw
    :size [WIDTH HEIGHT]))
