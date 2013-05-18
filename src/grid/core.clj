(ns grid.core
  (:use [overtone.live])
  (:require [quil.core :as qc]
            [bifocals.core :as bifocals]
            [overtone.inst.drum :as drum]
            [overtone.inst.piano :as piano]
            [overtone.inst.sampled-piano :as s-piano]
            [overtone.samples.piano :as samples-piano]
            [overtone.studio.inst :as stdinst]
            [overtone.gui.scope :as scope]
            [clojure.core.match :as match]))

(definst v-sampled-piano
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1 curve -4 gate 1 amp 0.5]
  (let [buf (index:kr (:id samples-piano/index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)
        snd (* env (scaled-play-buf 2 buf :level level :loop loop? :action FREE))]
    (detect-silence snd 0.005 :action FREE)
    (* amp snd)))

(definst v-piano [amp 0.5
                  note 60
                gate 1
                vel 100
                decay 0.8
                release 0.8
                hard 0.8
                velhard 0.8
                muffle 0.8
                velmuff 0.8
                velcurve 0.8
                stereo 0.2
                tune 0.5
                random 0.1
                stretch 0.1
                sustain 0.1]
  (let [snd (mda-piano {:freq (midicps note)
                        :gate gate
                        :vel vel
                        :decay decay
                        :release release
                        :hard hard
                        :velhard velhard
                        :muffle muffle
                        :velmuff velmuff
                        :velcurve velcurve
                        :stereo stereo
                        :tune tune
                        :random random
                        :stretch stretch
                        :sustain sustain})]
    (detect-silence snd 0.005 :action FREE)
    (* amp snd)))

;;(def piano s-piano/sampled-piano)
;;(def piano piano/piano)
(def piano v-piano)
(def piano v-sampled-piano)
;;(stop)

(def GRID_SETS 2)
(def GRID_SET_WIDTH 640.0)
(def GRID_SET_HEIGHT 480.0)
(def WIDTH (* GRID_SETS GRID_SET_WIDTH))
(def HEIGHT (* GRID_SETS GRID_SET_HEIGHT))

(def GRID_SET_COLS 16)
(def GRID_SET_ROWS 12)
;; (def NCOLS 64)
;; (def NROWS 48)

(def NCOLS (* GRID_SETS GRID_SET_COLS))
(def NROWS (* GRID_SETS GRID_SET_ROWS))

(def MARGIN 0)
(def CW (/ WIDTH NCOLS))
(def RH (/ HEIGHT NROWS))

;; (def DEPTH_OFF_THRESH 500)
;; (def DEPTH_ON_THRESH 2000)
;; (def DEPTH_FAR_THRESH 1400.0)
(def DEPTH_FAR_THRESH 3500.0)
(def DEPTH_MAX 7000.0)

;; Dirty, Dirty, STATE
(def k-col-width (atom 0))
(def k-row-height (atom 0))
(def grid-state (atom {}))

(defn setup []
  (qc/frame-rate 15)
  (.setMirror (bifocals/kinect) true)

  ;; Remember col/row sizes:
  (swap! k-col-width  (constantly (int (/ (bifocals/depth-width)  NCOLS))))
  (swap! k-row-height (constantly (int (/ (bifocals/depth-height) NROWS))))

  ;; Initialize grid state: true/false = on/off
  (dorun
   (for [col (range NCOLS)
         row (range NROWS)]
     (swap! grid-state #(assoc % [col row] false))))
  
  (qc/stroke 20)
  (qc/stroke-weight 1))

;; Ideas:
;;  - If sector is commonly used, it could have it's volume degrade
;;  with use.
(defn- hit-at-dispatch [col row depth]
  (cond
   true :bing
   true :piano
   ;; true :bing
   (< col (/ NCOLS 2)) :piano
   :default :bing))

(defmulti hit-at
  "Play sound, determined by location of movement."
  #'hit-at-dispatch
  :default :bing)

(defmethod hit-at :piano [col row depth]
  (let [
        freq (qc/map-range col 0 (/ NCOLS 2) 100 200)
        decay (qc/map-range depth 0 DEPTH_MAX 0.001 1.0)
        attack 0.001

        ;; piano-key
        progression [:c :d :e :f :g :a :b]
        note-key (keyword (str "c" (- NROWS row)))

        amp (qc/map-range row 0 NROWS 0.8 0.3)
        curve 0
        ]
    ;; (drum/quick-kick :freq freq :decay decay :attack attack)
    ;; (drum/quick-kick :decay 0.1)
    ;; (* 0.3 (piano (note :c4)))
    ;; (at (+ (now) 10) (piano (note :c4)))
    ;; (at (+ (now) 250) (piano :note (note note-key) :amp amp))
    (at (+ (now) 250) (piano :note (note note-key) :amp amp :curve curve :sustain 0.1 :decay 0.8))
    ))
;;(stop)

;;(piano (note :c1))
;;(stdinst/inst-volume! piano 0.3)

(defmethod hit-at :clap [col row depth]
  (let [
        freq (qc/map-range col 0 (/ NCOLS 2) 100 1000)
        decay (qc/map-range depth 0 DEPTH_MAX 0.001 1.0)
        attack 0.001
        rq 0.0001
        ]
    (drum/haziti-clap :freq freq :decay decay :attack attack :rq rq)))

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

(defn turn-on-at [col row depth]
  (when-not (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] true))
    (hit-at col row depth)))

(defn turn-off-at [col row]
  (if (@grid-state [col row])
    (swap! grid-state #(assoc % [col row] false))))

(defn draw-triangle-tip-at
  [x y]
  (let [next-y (+ RH y)]
    (qc/triangle x y
                 (+ x CW) next-y
                 (- x CW) next-y)))

(defn draw-triangle-bottom-at
  [x y]
  (qc/triangle (- x CW) y
               (+ x CW) y
               x (+ y RH)))

(defn display-grid-element-at
  ([x y]
     (qc/rect x y (- CW MARGIN) (- RH MARGIN)))
  ([x y col row]
     (cond
      (and (= 0 (mod col 2)) (= 0 (mod row 2))) (draw-triangle-tip-at x y)
      (and (= 0 (mod col 2)) (= 1 (mod row 2))) (draw-triangle-bottom-at x y)
      (and (= 1 (mod col 2)) (= 0 (mod row 2))) (draw-triangle-bottom-at x y)
      (and (= 1 (mod col 2)) (= 1 (mod row 2))) (draw-triangle-tip-at x y))))

(defn color-scheme-disney-blues
  [scaled-depth alpha]
  (cond
   (> scaled-depth 240) (qc/fill 15 201 242 alpha)
   (> scaled-depth 220) (qc/fill 13 159 217 alpha)
   (> scaled-depth 200) (qc/fill 17 124 217 alpha)
   (> scaled-depth 170) (qc/fill 17 104 217 alpha)
   :default  (qc/fill 18 60 193 alpha)))

(defn color-scheme-healthy-yogurt
  [scaled-depth alpha]
  (cond
   (> scaled-depth 240) (qc/fill 191 191 176 alpha)
   (> scaled-depth 220) (qc/fill 239 242 148 alpha)
   (> scaled-depth 200) (qc/fill 125 166 106 alpha)
   (> scaled-depth 170) (qc/fill 29 78 89 alpha)
   :default  (qc/fill 25 38 64 alpha)))

(defn color-scheme-etro-wallpaper6
  [scaled-depth alpha]
  (cond
   ;; (< scaled-depth 10)  (qc/fill 0 255 0 alpha)
   ;; (> scaled-depth 250) (qc/fill 255 0 0 alpha)
   (> scaled-depth 240) (qc/fill 242 233 216 alpha)
   (> scaled-depth 220) (qc/fill 217 209 186 alpha)
   (> scaled-depth 200) (qc/fill 128 148 166 alpha)
   (> scaled-depth 170) (qc/fill 76 97 115 alpha)
   :default  (qc/fill 28 47 64 alpha)))

(defn color-scheme-font-love
  [scaled-depth alpha]
  (cond
   (> scaled-depth 240) (qc/fill 162 105 254 alpha)
   (> scaled-depth 220) (qc/fill 132 78 233 alpha)
   (> scaled-depth 200) (qc/fill 101 51 203 alpha)
   (> scaled-depth 170) (qc/fill 71 24 173 alpha)
   :default  (qc/fill 42 1 152 alpha)))

(defn color-scheme-emperor-penguin
  [scaled-depth alpha]
  (cond
   (> scaled-depth 240) (qc/fill 254 172 0 alpha)
   (> scaled-depth 220) (qc/fill 252 216 43 alpha)
   (> scaled-depth 200) (qc/fill 94 111 106 alpha)
   (> scaled-depth 170) (qc/fill 28 68 88 alpha)
   :default  (qc/fill 1 40 64 alpha)))

(defn choose-display-color [depth]
  
  (let [g (qc/map-range depth 0 DEPTH_MAX 255 0)
        alpha 160]
    ;; (color-scheme-disney-blues g alpha)
    ;; (color-scheme-healthy-yogurt g alpha)
    ;; (color-scheme-etro-wallpaper6 g alpha)
    ;; (color-scheme-font-love g alpha)
    (color-scheme-emperor-penguin g alpha)
    ;; (qc/fill g 160)
    ))

(defn display-at
  [x y col row depth]
  (choose-display-color depth)
  ;; (qc/rect x y (- CW MARGIN) (- RH MARGIN))
  (display-grid-element-at x y)
  ;; (display-grid-element-at x y col row)

  ;; Draw on/off indicator square:
  (cond
   (> depth DEPTH_FAR_THRESH) (turn-off-at col row)
   (< depth DEPTH_FAR_THRESH) (turn-on-at col row depth))
  (cond
   (@grid-state [col row]) (qc/fill 0 255 0 80)
   :default (qc/fill 255 0 0 80))
  (qc/rect x y 5 5)
  )

(defn simple-depth-at
  [col row k-depth-map]
  (let [kx (* (+ col 0.5) @k-col-width)
        ky (* (+ row 0.5) @k-row-height)
        n (int (+ kx (* ky (bifocals/depth-width))))]
    (nth k-depth-map n)))

(def tick (atom 0))

(defn draw-simple
  [col row k-depth-map]
  (let [x (* col CW)
        y (* row RH)
        depth (simple-depth-at col row k-depth-map)
        depth (qc/constrain-float depth 0.0 DEPTH_MAX)]
    (display-at x y col row depth)))

(defn draw []
  (bifocals/tick)
  (swap! tick inc)
  (let [k-depth-map (.depthMap (bifocals/kinect))]
    (doall
     (if (even? @tick)
       (for [col (range NCOLS)
             row (range NROWS)
             :when (or
                    (and
                     (even? col)
                     (even? row))
                    (and
                     (odd? col)
                     (odd? row)))]
         (draw-simple col row k-depth-map))
       (for [col (range NCOLS)
             row (range NROWS)
             :when (or
                    (and
                     (odd? col)
                     (even? row))
                    (and
                     (even? col)
                     (odd? row)))]
         (draw-simple col row k-depth-map))))))

(defn on-close-sketch []
  (stop))

(defn run-sketch []
  (qc/defsketch grid
    :title "Grid"
    :setup setup
    :draw draw
    :on-close on-close-sketch
    :size [WIDTH HEIGHT]))

;;(run-sketch)
;;(qc/sketch-stop grid)
;;(qc/sketch-start grid)
;;(qc/sketch-close grid)

;;(drum/bing :freq 500)

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

(comment 
  (drum/quick-kick :amp 0.7)
  (drum/haziti-clap :decay 0.05 :amp 0.5)
  (drum/haziti-clap :decay 0.1 :amp 0.5)
  (drum/soft-hat :decay 0.05 :amp 0.8)
  (drum/kick :amp 0.8)
  (drum/kick2 :amp 0.8)
  (drum/kick3 :amp 0.8)
  (drum/kick4 :amp 0.8)
  )


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

(comment
  ;;Control the dubstep synth with the following:
  (def d (dubstep))
  (ctl d :wobble 8)
  (ctl d :note 40)
  (ctl d :bpm 250)
  (ctl d :v 0.2)
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



;; Dan Stowells' Dubstep Synth:
;; SClang version:
;;
;;s.waitForBoot{Ndef(\a).play;Ndef(\a,
;;{
;;var trig, freq, notes, wob, sweep, kickenv, kick, snare, swr, syn, bpm, x;
;;x = MouseX.kr(1, 4);
;;
;;
;;// START HERE:
;;
;;bpm = 120;
;;
;;notes = [40, 41, 28, 28, 28, 28, 27, 25, 35, 78];
;;
;;trig = Impulse.kr(bpm/120);
;;freq = Demand.kr(trig, 0, Dxrand(notes, inf)).lag(0.25).midicps;
;;swr = Demand.kr(trig, 0, Dseq([1, 6, 6, 2, 1, 2, 4, 8, 3, 3], inf));
;;sweep = LFTri.ar(swr).exprange(40, 3000);
;;
;;
;;// Here we make the wobble bass:
;;wob = Saw.ar(freq * [0.99, 1.01]).sum;
;;wob = LPF.ar(wob, sweep);
;;wob = Normalizer.ar(wob) * 0.8;
;;wob = wob + BPF.ar(wob, 1500, 2);
;;wob = wob + GVerb.ar(wob, 9, 0.7, 0.7, mul: 0.2);
;;
;;
;;// Here we add some drums:
;;kickenv = Decay.ar(T2A.ar(Demand.kr(Impulse.kr(bpm / 30),0,Dseq([1,0,0,0,0,0,1,0, 1,0,0,1,0,0,0,0],inf))),0.7);
;;kick = SinOsc.ar(40+(kickenv*kickenv*kickenv*200),0,7*kickenv).clip2;
;;snare = 3*PinkNoise.ar(1!2)*Decay.ar(Impulse.ar(bpm / 240, 0.5),[0.4,2],[1,0.05]).sum;
;;snare = (snare + BPF.ar(4*snare,2000)).clip2;
;;
;;// This line actually outputs the sound:
;;(wob + kick + snare).clip2;
;;
;;})}
;;
;; Directly translated to Overtone:

(defsynth dubstep-synth [bpm 120]
  (out 0
        (let [
              ;;bpm     120
              notes   [40 41 28 28 28 27 25 35 78]
              trig    (impulse:kr (/ bpm 120))
              freq    (midicps (lag (demand trig 0 (dxrand notes INF)) 0.25))
              swr     (demand trig 0 (dseq [1 6 6 2 1 2 4 8 3 3] INF))
              sweep   (lin-exp (lf-tri swr) -1 1 40 3000)
              wob     (mix (saw (* freq [0.99 1.01])))
              wob     (lpf wob sweep)
              wob     (* 0.8 (normalizer wob))
              wob     (+ wob (bpf wob 1500 2))
              wob     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

              kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
              kick    (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
              kick    (clip2 kick 1)

              snare   (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
              snare   (+ snare (bpf (* 4 snare) 2000))
              snare   (clip2 snare 1)]

          (clip2 (+ wob kick snare) 1))))
(comment
  (def d (dubstep-synth))
  (ctl d :bpm 250)
  )
;;(stop)




;;Dan Stowell's dubstep bass

;;//s.boot
;;{
;; var trig, note, son, sweep;
;;
;; trig = CoinGate.kr(0.5, Impulse.kr(2));
;;
;; note = Demand.kr(trig, 0, Dseq((22,24..44).midicps.scramble, inf));
;;
;; sweep = LFSaw.ar(Demand.kr(trig, 0, Drand([1, 2, 2, 3, 4, 5, 6, 8, 16], inf))).exprange(40, 5000);
;;
;; son = LFSaw.ar(note * [0.99, 1, 1.01]).sum;
;; son = LPF.ar(son, sweep);
;; son = Normalizer.ar(son);
;; son = son + BPF.ar(son, 2000, 2);
;;
;; //////// special flavours:
;; // hi manster
;; son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, 1000) * 4]);
;; // sweep manster
;; son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, HPF.ar(son, sweep) * 4]);
;; // decimate
;; son = Select.ar(TRand.kr(trig: trig) < 0.05, [son, son.round(0.1)]);
;;
;; son = (son * 5).tanh;
;; son = son + GVerb.ar(son, 10, 0.1, 0.7, mul: 0.3);
;; son.dup
;;}.play

(defsynth dubstep-bass []
  (out 0
       (let [trig (coin-gate 0.5 (impulse:kr 2))
             note (demand trig 0 (dseq (shuffle (map midi->hz (conj (range 24 45) 22))) INF))
             sweep (lin-exp (lf-saw (demand trig 0 (drand [1 2 2 3 4 5 6 8 16] INF))) -1 1 40 5000)

             son (mix (lf-saw (* note [0.99 1 1.01])))
             son (lpf son sweep)
             son (normalizer son)
             son (+ son (bpf son 2000 2))

             ;;special flavours
             ;;hi manster
             son (select (< (t-rand:kr :trig trig) 0.05) [son (* 4 (hpf son 1000))])

             ;;sweep manster
             son (select (< (t-rand:kr :trig trig) 0.05) [son (* 4 (hpf son sweep))])

             ;;decimate
             son (select (< (t-rand:kr :trig trig) 0.05) [son (round son 0.1)])

             son (tanh (* son 5))
             son (+ son (* 0.3 (g-verb son 10 0.1 0.7)))
             son (* 0.3 son)]

         [son son])))

(comment
  (def db (dubstep-bass))
  )

;;(stop)




;; DUBSTEP POLYNOME/MONOME
(defonce dub-vol (atom 1))
(declare dmon)

;;(stop)
(definst dubstep-mon [note 40 wob 2 hi-man 0 lo-man 0 sweep-man 0 deci-man 0 tan-man 0 shape 0 sweep-max-freq 3000 hi-man-max 1000 lo-man-max 500 beat-vol 0.5 amp 1 bpm 300]
  (let [;;bpm 300
        shape (select shape [(lf-tri wob) (lf-saw wob)])
        sweep (lin-exp shape -1 1 40 sweep-max-freq)
        snd   (mix (saw (* (midicps note) [0.99 1.01])))
        snd   (lpf snd sweep)
        snd   (normalizer snd)

        snd   (+ snd (bpf snd 1500 2))
        ;;special flavours
        ;;hi manster
        snd   (select (> hi-man 0.05) [snd (* 4 (hpf snd hi-man-max))])

        ;;sweep manster
        snd   (select (> sweep-man 0.05) [snd (* 4 (hpf snd sweep))])

        ;;lo manster
        snd   (select (> lo-man 0.05) [snd (lpf snd lo-man-max)])

        ;;decimate
        snd   (select (> deci-man 0.05) [snd (round snd 0.1)])

        ;;crunch
        snd   (select (> tan-man 0.05) [snd (tanh (* snd 5))])

        snd   (* 0.5 (+ (* 0.8 snd) (* 0.3 (g-verb snd 100 0.7 0.7))))

        kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
        kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
        kick (clip2 kick 1)

        snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
        snare (+ snare (bpf (* 4 snare) 2000))
        snare (clip2 snare 1)
        beat (* beat-vol (+ kick snare))
        ]
    (* amp (+ (pan2 snd shape) beat))))

(comment
  (def dmon (dubstep-mon))
  )

;;(stop)
;;(dubstep-mon)

;; (def m (poly/init "/dev/tty.usbserial-m64-0790"))
;;(def m beatbox.core/m)
;;(poly/disconnect m)
;; (poly/remove-all-callbacks m)

(def id->dub-ctl {0 :hi-man
                  1 :lo-man
                  2 :deci-man
                  3 :tan-man
                  4 :beat-vol})

(defn toggle-vol
  []
  (ctl dmon :amp (swap! dub-vol #(mod (inc %) 2))))

(defn toggle-fx
  [ctl-num val]
  (when-let [ctl-name (get id->dub-ctl ctl-num)]
    ;; (poly/toggle-led m x y)
    (let [
          ;; val (poly/led-activation m x y)
          ]
      (ctl dmon ctl-name val))))

(defn modulate-pitch-wob
  [x y]
  (let [wob x
        note (nth (scale :g1 :minor-pentatonic) y)]
    (ctl dmon :note note)
    (ctl dmon  :wob wob)))

;; (poly/on-press m ::foo (fn [x y s]
;;                          (match/match [x y]
;;                            [0 7] (toggle-vol)
;;                            [0 _] (toggle-fx x y)
;;                            [_ _] (modulate-pitch-wob x y))))


(comment
  (def dmon (dubstep-mon))
  (modulate-pitch-wob 5 2)
  (toggle-fx 0 100)
  (toggle-fx 1 500)
  (toggle-fx 2 0.05)
  (toggle-fx 3 0.05)
  (toggle-fx 4 0.8)
  (toggle-vol)
  )
;;(stop)
(comment

  (ctl dmon :lo-man-max 900)
  (ctl dmon :hi-man-max 200)
  (ctl dmon :sweep-max-freq 3000)
  (ctl dmon :note 40)
  (stop)
  )






(comment
  (def flute-buf (load-sample (freesound-path 35809)))

  ;; Now the audio data for the sample is loaded into a buffer.  You can
  ;; view the buffer in the scope window too.  Click in the scope tab on
  ;; the right, and evaluate this.
  (scope/pscope :buf flute-buf)

  ;; If you just want to play a buffer and adjust the speed or looping
  ;; play-buf is probably the easiest way.
  (odoc sample-player)

  (sample-player flute-buf)

  ;; to stop, just use the (stop) fn:
  (stop)
  )