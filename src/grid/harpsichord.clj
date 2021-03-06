(ns grid.harpsichord
  (:use [overtone.core]))

(definst harpsichord [freq 440 initial-duration 1]
  (let [duration initial-duration]
    (*
      (line:kr 1 1 duration FREE)
      (pluck (* (white-noise) (env-gen (perc 0.001 5) :action FREE))
             1 1 (/ 1 freq) (* duration 2) 0.25))))
 
(def melody 
  (let [pitches
         [:C4 :C4 :C4 :D4 :E4         
          ; Row, row, row your boat,
          :E4 :D4 :E4 :F4 :G4
          ; Gently down the stream,
          :C5 :C5 :C5 :G4 :G4 :G4 :E4 :E4 :E4 :C4 :C4 :C4 
          ; Merrily, merrily, merrily, merrily,
          :G4 :F4 :E4 :D4 :C4]
          ; Life is but a dream! 
        durations
         [1 1 2/3 1/3 1
          2/3 1/3 2/3 1/3 2
          1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3 1/3
          2/3 1/3 2/3 1/3 2]
        times (reductions + 0 durations)]
    (map vector times pitches)))

(defn play-single-note-by-key
  [note-by-key]
  (-> note-by-key note midi->hz harpsichord))

(defn play-single-note-by-int
  [note-int duration]
  ;;(play (metronome 120) melody)
  (let [harp (fn [freq] (harpsichord freq duration))]
    (-> note-int
       midi->hz
       harp)))

(defn play [metro notes] 
  (let [play-note (fn [[beat pitch]] (at (metro beat) (-> pitch note midi->hz harpsichord)))]
    (dorun (map play-note notes)))) 
 
(defn play-round [metro notes]
  (let [after (fn [beats metro] (comp metro #(+ % beats)))]
    (play metro notes)
    (play (after 4 metro) notes)
    (play (after 8 metro) notes)
    (play (after 16 metro) notes)))

(defn play-intro
  []
  (let [notes melody
        metro (metronome 120)
        after (fn [beats metro] (comp metro #(+ % beats)))]
    (play metro notes)
    (play (after 4 metro) notes)))

;(play (metronome 120) melody)
;(play-round (metronome 120) melody)
