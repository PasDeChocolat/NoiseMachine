(ns grid.core
  (:use [grid.setup :only [HEIGHT WIDTH]])
  (:require [grid.draw :as dynamic-draw]
            [grid.setup :as dynamic-setup]
            [quil.core :as qc]))

(defn on-close-sketch []
  ;;(stop)
  )

(defn run-sketch []
  (qc/defsketch grid
    :title "Grid"
    :renderer :p3d
    :setup dynamic-setup/setup
    :draw dynamic-draw/draw
    :on-close on-close-sketch
    :size [WIDTH HEIGHT]))

(defn stop-sketch [] (qc/sketch-stop grid))
(defn restart-sketch [] (qc/sketch-start grid))
(defn close-sketch [] (qc/sketch-close grid))

;;(run-sketch)
;;(qc/sketch-stop grid)
;;(qc/sketch-start grid)
;;(qc/sketch-close grid)
