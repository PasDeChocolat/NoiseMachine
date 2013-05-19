(ns grid.color-schemes
  (:require [quil.core :as qc])
  (:use [grid.setup :only [DEPTH_FAR_THRESH DEPTH_MAX]]))

;; (defn color-scheme-disney-blues
;;   [scaled-depth alpha]
;;   (cond
;;    (> scaled-depth 240) (qc/fill 15 201 242 alpha)
;;    (> scaled-depth 220) (qc/fill 13 159 217 alpha)
;;    (> scaled-depth 200) (qc/fill 17 124 217 alpha)
;;    (> scaled-depth 170) (qc/fill 17 104 217 alpha)
;;    :default  (qc/fill 18 60 193 alpha)))

;; (defn color-scheme-healthy-yogurt
;;   [scaled-depth alpha]
;;   (cond
;;    (> scaled-depth 240) (qc/fill 191 191 176 alpha)
;;    (> scaled-depth 220) (qc/fill 239 242 148 alpha)
;;    (> scaled-depth 200) (qc/fill 125 166 106 alpha)
;;    (> scaled-depth 170) (qc/fill 29 78 89 alpha)
;;    :default  (qc/fill 25 38 64 alpha)))

;; (defn color-scheme-etro-wallpaper6
;;   [scaled-depth alpha]
;;   (cond
;;    ;; (< scaled-depth 10)  (qc/fill 0 255 0 alpha)
;;    ;; (> scaled-depth 250) (qc/fill 255 0 0 alpha)
;;    (> scaled-depth 240) (qc/fill 242 233 216 alpha)
;;    (> scaled-depth 220) (qc/fill 217 209 186 alpha)
;;    (> scaled-depth 200) (qc/fill 128 148 166 alpha)
;;    (> scaled-depth 170) (qc/fill 76 97 115 alpha)
;;    :default  (qc/fill 28 47 64 alpha)))

;; (defn color-scheme-font-love
;;   [scaled-depth alpha]
;;   (cond
;;    (> scaled-depth 240) (qc/fill 162 105 254 alpha)
;;    (> scaled-depth 220) (qc/fill 132 78 233 alpha)
;;    (> scaled-depth 200) (qc/fill 101 51 203 alpha)
;;    (> scaled-depth 170) (qc/fill 71 24 173 alpha)
;;    :default  (qc/fill 42 1 152 alpha)))

(defn color-scheme-emperor-penguin
  [depth alpha]
  (let [half-far (* 0.5 (- DEPTH_MAX DEPTH_FAR_THRESH))]
    (cond
     (< depth 411) (qc/fill 254 172 0 alpha)
     (< depth 960) (qc/fill 252 216 43 alpha)
     (< depth 1510) (qc/fill 94 111 106 alpha)
     (< depth DEPTH_FAR_THRESH) (qc/fill 172 202 186 alpha)
     (< depth (+ DEPTH_FAR_THRESH half-far)) (qc/fill 28 68 88 alpha)   

     
     ;; (< depth 2333) (qc/fill 28 68 88 alpha)

     ;; (< depth DEPTH_FAR_THRESH) (qc/fill 255 0 0 alpha)
     :default (qc/fill 1 40 64 alpha))))







