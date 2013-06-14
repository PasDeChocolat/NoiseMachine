(ns grid.draw-sensors
  (:require [grid.color-schemes :as color-schemes]
            [quil.core :as qc])
  (:use [grid.setup :only [CW DEPTH_FAR_THRESH DEPTH_START_SECOND_LAYER DEPTH_MAX MAX_SENSOR_BURST_HEALTH NCOLS RH WIDTH]]
        [grid.state :only [grid-sensors tick]]))

(defn draw-sensor-point
  [t [[col row] { :keys [x y]}]]
  (let [t (* t 0.1)
        n (qc/noise col row t)
        r (+ 1 (* n 4))]
    (qc/fill 255 255)
    (qc/ellipse x y r r)))

(defn update-sensor-point
  [tick-atom new-grid [[col row :as col-row] { :keys [x y] :as sensor}]]
  (let [t (* 0.01 @tick-atom)
        t (+ (rand 0.01) t) ;; Make a randomness of points a little smoother.
        pos (+ col (* NCOLS row))
        new-x (+ (* (- NCOLS col 1) CW) (* CW (qc/noise pos t)))
        new-y (+ (* row RH) (* RH (qc/noise pos (+ 5.2 t))))
        new-sensor (-> (assoc sensor :x new-x)
                       (assoc :y new-y))]
    (assoc new-grid col-row new-sensor)))

(defn draw-sensor-grid
  []
  (dorun
   (map (partial draw-sensor-point @tick) @grid-sensors))
  (let [new-grid (doall
                  (reduce (partial update-sensor-point tick) {} @grid-sensors))]
    (reset! grid-sensors new-grid)))


;; Display sensor bursts and such...

(defn update-burst-health
  [sensor health was-on? is-on?]
  (let [new-health (cond
                    (and (not was-on?) is-on?) MAX_SENSOR_BURST_HEALTH
                    (and was-on? is-on? (>= health 0)) (dec health)
                    (and (not is-on?) (>= health 0)) (dec health)
                    :else health)]
    (assoc-in sensor [:burst :health] new-health)))

(defn update-burst-pos
  [{x :x y :y { :keys [drop-y health] :or {drop-y 0} } :burst :as sensor}]
  (let [new-drop (qc/map-range health MAX_SENSOR_BURST_HEALTH 0 0 200)
        new-drop (qc/lerp drop-y new-drop 0.2)]
    (assoc-in sensor [:burst :drop-y] new-drop)))

(defn update-sensor-element-at
  [{{:keys [health]} :burst :as sensor} col row was-on? is-on?]
  (let [new-sensor (-> (update-burst-health sensor health was-on? is-on?)
                       (update-burst-pos))]
    (swap! grid-sensors #(assoc % [col row] new-sensor))
    new-sensor))

(defn draw-burst
  [x y drop-y col row depth is-on? health]
  (qc/push-matrix)
  (let [side (qc/map-range health 0 MAX_SENSOR_BURST_HEALTH 0 10)
        half-side (/ side 2.0)
        h (Math/sqrt (* 0.75 (Math/pow side 2)))
        half-h (/ h 2.0)
        theta (qc/noise col row (* 0.2 @tick))]
    (qc/translate 0 drop-y)
    (qc/rotate theta)
    (qc/triangle (- half-side) half-h 0 (- half-h) half-side half-h))
  (qc/pop-matrix))

(defn display-sensor-element-at
  [{:keys [x y burst]} col row depth pct-on is-on?]
  (let [{:keys [health drop-y] :or {drop-y 0}} burst]
    (when (or
           (> health 0)
           (and (> depth DEPTH_START_SECOND_LAYER)
                (< depth DEPTH_FAR_THRESH)
                (> health 0)))
      (qc/push-style)
      (qc/push-matrix)
      (qc/color-mode :hsb 0.0 1.0 1.0 1.0)
      (let [hue 276.0
            sat (qc/map-range pct-on 0.0 1.0 1.0 0.0)
            val (qc/map-range pct-on 0.0 1.0 0.4 1.0)
            alpha 1.0]
        (qc/fill hue sat val alpha))
      (qc/translate x y)
      (draw-burst x y drop-y col row depth is-on? health)
      (qc/pop-matrix)
      (qc/pop-style))))

(defn update-display-sensor-element-at
  [col row depth was-on? is-on? pct-on]
  (let [{:keys [x y burst] :or {x 0 y 0} :as sensor} (@grid-sensors [col row])
        sensor (update-sensor-element-at sensor col row was-on? is-on?)]
    (display-sensor-element-at sensor col row depth pct-on is-on?)))
