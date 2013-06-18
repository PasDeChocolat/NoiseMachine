(ns grid.state
  (:require [overtone.at-at :as at-at]))

;; Dirty, Dirty, STATE
(def tick (atom 0))
(def k-col-width (atom 0))
(def k-row-height (atom 0))
(def grid-state (atom {}))
(def long-col-state (atom {}))
(def grid-sensors (atom {}))
(def all-pixies (atom []))
(def note-grid (atom {}))

(def at-at-pool (at-at/mk-pool))
