(defproject natureofclojure "0.1.0-SNAPSHOT"
  :description "Grid in Processing/Quil"
  :url "http://github.com/mudphone/"
  :license {:name "Create Commons Attribution-ShareAlike 2.0 Generic License"
            :url "http://creativecommons.org/licenses/by-sa/2.0/"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [org.clojure/math.numeric-tower "0.0.2"]
                 [quil "1.6.0"]
                 [bifocals "0.1.0"]
                 [overtone "0.9.0-SNAPSHOT"]]
  :jvm-opts ["-Xmx768M"]
  :main grid.core
  )
