(defproject noisemachine "0.1.0-SNAPSHOT"
  :description "Noise Machine #1"
  :url "http://github.com/mudphone/"
  :license {:name "Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)"
            :url "http://creativecommons.org/licenses/by-sa/3.0/"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [quil "1.6.0"]
                 [bifocals "0.1.0"]
                 [overtone "0.9.0-SNAPSHOT"]]
  :jvm-opts ["-Xmx2048M"]
  :main grid.core
  )
