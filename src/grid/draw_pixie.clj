(ns grid.draw-pixie
  (:require [quil.core :as qc]
            [grid.plus-pixie :as plus-pixie]))

;; Must remove and re-add the multi-method when working in the REPL.
;; Step 1: C-c C-e the following line:
;; (ns-unmap 'grid.draw-pixie 'draw-specific-pixie)
;; Step 2: C-c C-k

(defn- draw-specific-pixie-dispatch
  [{:keys [type]}]
  ;; :plus
  type)

(defmulti draw-specific-pixie
  #'draw-specific-pixie-dispatch
  :default nil)

(defmethod draw-specific-pixie :plus
  [pixie]
  (plus-pixie/draw-pixie pixie))

(defn draw-pixie
  [pixie]
  (qc/push-matrix)
  (qc/push-style)
  (let [b (draw-specific-pixie pixie)]
    (qc/pop-style)
    (qc/pop-matrix)
    b))
