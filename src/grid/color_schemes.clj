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

;; Based on this theme: https://kuler.adobe.com/#themeID/1979738
(defn color-scheme-emperor-penguin
  ([depth alpha]
     (color-scheme-emperor-penguin depth 0 DEPTH_MAX DEPTH_FAR_THRESH alpha))
  ([depth min-depth max-depth far-thresh alpha]
     (let [thresh-range (- far-thresh min-depth)
           step-1 (* 0.2 thresh-range)
           step-2 (* 0.27 thresh-range)
           step-3 (* 0.43 thresh-range)
           half-far-to-max (* 0.2 (- max-depth far-thresh))]
       (cond
        (< depth 1) (qc/fill 0 0)
        (< depth step-1) (qc/fill 254 172 0 alpha)
        (< depth step-2) (qc/fill 252 216 43 alpha)
        (< depth step-3) (qc/fill 94 111 106 alpha)
        (< depth far-thresh) (qc/fill 186 198 202 alpha)
        (< depth (+ far-thresh half-far-to-max)) (qc/fill 28 68 88 alpha)
        ;; :depth (qc/fill 255 0 0)
        :default (qc/fill 1 40 64 alpha)
        ))))
