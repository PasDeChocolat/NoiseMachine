(ns grid.core
  (:use [overtone.live])
  (:require [quil.core :as qc]
            [bifocals.core :as bifocals]
            [overtone.inst.drum :as drum]))

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
(def k-row-height (atom 0))

(defn setup []
  (qc/frame-rate 15)
  (.setMirror (bifocals/kinect) true)
  (swap! k-col-width  (constantly (int (/ (bifocals/depth-width)  NCOLS))))
  (swap! k-row-height (constantly (int (/ (bifocals/depth-height) NROWS))))

  (qc/stroke 20)
  (qc/stroke-weight 1))

(defn display-at
  [x y depth]
  (let [g (qc/map-range depth 0 2048 255 0)]
    (qc/fill g 255)
    (qc/rect x y (- CW MARGIN) (- RH MARGIN))
    (if (and false
         (> g 255)
             (> 0.999999 (rand)))
      (drum/quick-kick :amp (qc/map-range g 0 255 0 0.6)))))

(defn avg-depth-at
  [col row k-depth-map]
  (let [step 5
        all-d (for [kx (range (* col @k-col-width)  (* (inc col) @k-col-width))
                    ky (range (* row @k-row-height) (* (inc row) @k-row-height))
                    :when (and (= 0 (mod kx step))
                               (= 0 (mod ky step)))
                    :let [n (+ kx (* ky (bifocals/depth-width)))
                          depth (nth k-depth-map n)]]
                depth)]
    (/ (apply + all-d) (count all-d))))

(defn simple-depth-at
  [col row k-depth-map]
  (let [kx (* (+ col 0.5) @k-col-width)
        ky (* (+ row 0.5) @k-row-height)
        n (int (+ kx (* ky (bifocals/depth-width))))]
    (nth k-depth-map n)))

(defn draw []
  (bifocals/tick)
  (let [k-depth-map (.depthMap (bifocals/kinect))]
    (doall
     (for [col (range NCOLS)
           row (range NROWS)
           :let [x (* col CW)
                 y (* row RH)
                 depth (simple-depth-at col row k-depth-map)
                 ;; depth (avg-depth-at col row k-depth-map)
                 ]]
       (display-at x y depth)))))

(defn run-sketch []
  (qc/defsketch grid
    :title "Grid"
    :setup setup
    :draw draw
    :size [WIDTH HEIGHT]))


(def m (metronome 128))

(defn player
  [beat]
  (let [next-beat (inc beat)]
    (at (m beat)
        (drum/quick-kick :amp 0.5)
        (if (zero? (mod beat 2))
          (drum/open-hat :amp 0.1)))
    (at (m (+ 0.5 beat))
        (drum/haziti-clap :decay 0.05 :amp 0.3))

    (when (zero? (mod beat 3))
      (at (m (+ 0.75 beat))
          (drum/soft-hat :decay 0.03 :amp 0.2)))

    (when (zero? (mod beat 8))
      (at (m (+ 1.25 beat))
          (drum/soft-hat :decay 0.03)))

    (apply-at (m next-beat) #'player [next-beat])))

;;(player (m))
;;(stop)


(defsynth dubstep [bpm 120 wobble 1 note 50 snare-vol 1 kick-vol 1 v 1]
 (let [trig (impulse:kr (/ bpm 120))
       freq (midicps note)
       swr (demand trig 0 (dseq [wobble] INF))
       sweep (lin-exp (lf-tri swr) -1 1 40 3000)
       wob (apply + (saw (* freq [0.99 1.01])))
       wob (lpf wob sweep)
       wob (* 0.8 (normalizer wob))
       wob (+ wob (bpf wob 1500 2))
       wob (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

       kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
       kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
       kick (clip2 kick 1)

       snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
       snare (+ snare (bpf (* 4 snare) 2000))
       snare (clip2 snare 1)]

   (out 0    (* v (clip2 (+ wob (* kick-vol kick) (* snare-vol snare)) 1)))))

(dubstep)

(comment
  ;;Control the dubstep synth with the following:
  (def d (dubstep))
  (ctl d :wobble 8)
  (ctl d :note 40)
  (ctl d :bpm 250)
  (stop)
  )


(comment
  ;;For connecting with a monome to control the wobble and note
  (require '(polynome [core :as poly]))
  (def m (poly/init "/dev/tty.usbserial-m64-0790"))
  (def notes (reverse [25 27 28 35 40 41 50 78]))
  (poly/on-press m (fn [x y s]
                   (do
                     (let [wobble (inc y)
                           note (nth notes x)]
                       (println "wobble:" wobble)
                       (println "note:" note)
                       (poly/clear m)
                       (poly/led-on m x y)
                       (ctl dubstep :wobble wobble)
                       (ctl dubstep :note note)))))
  (poly/disconnect m))

(comment
  ;;For connecting with a monome to drive two separate dubstep bass synths
  (do
    (require '(polynome [core :as poly]))
    (def m (poly/init "/dev/tty.usbserial-m64-0790"))
    (def curr-vals (atom {:b1 [0 0]
                          :b2 [5 0]}))
    (def curr-vol-b1 (atom 1))
    (def curr-vol-b2 (atom 1))

    (at (+ 1000 (now))
        (def b1 (dubstep))
        (def b2 (dubstep)))

    (defn swap-vol
      [v]
      (mod (inc v) 2))

    (defn fetch-note
      [base idx]
      (+ base (nth-interval :minor-pentatonic idx)))

    (defn relight
      []
      (poly/clear m)
      (apply poly/led-on m (:b1 @curr-vals))
      (apply poly/led-on m (:b2 @curr-vals)))

    (defn low-bass
      [x y]
      (println "low" [x y])
      (if (= [x y]
             (:b1 @curr-vals))
        (ctl b1 :v (swap! curr-vol-b1 swap-vol))
        (do
          (ctl b1 :wobble (inc x) :note (fetch-note 20 y))
          (swap! curr-vals assoc :b1 [x y])))
      (relight))

    (defn hi-bass
      [x y]
      (println "hi" [x y])
      (if (= [x y]
             (:b2 @curr-vals))
        (ctl b2 :v (swap! curr-vol-b2 swap-vol))
        (do
          (ctl b2 :wobble (- x 3) :note (fetch-note 40 y))
          (swap! curr-vals assoc :b2 [x y])))
      (relight))

    (poly/on-press m (fn [x y s]
                       (if (< x 4)
                         (apply #'low-bass [x y])
                         (apply #'hi-bass [x y]))))

    (poly/on-press m (fn [x y s]
                       (poly/toggle-led m x y))))
)

;;(stop)