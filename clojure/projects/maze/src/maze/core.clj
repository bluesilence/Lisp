(ns maze.core
  (:gen-class))

(def ^:dynamic *debug* true)

(defmacro debug-do [& body]
    (when *debug*
          `(do ~@body)))

;merge-with
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/merge-with
;into
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/into
;rand-nth
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/rand-nth
;disj
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/disj
;iterate
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/iterate
;comp
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/comp
;zipmap
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/zipmap
;take-while
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/take-while
;next
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/next
;if-let
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/if-let
;when-let
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/when-let
;recur
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/recur
(defn maze
  "Returns a random maze carved out of walls; walls is a set of
  2-item sets #{a b} where a and b are locations.
  The returned maze is a set of the remaining walls."
  [walls]
  (let [paths (reduce (fn [index [a b]]
                        (merge-with into index {a [b] b [a]}))
                      {} (map seq walls))
        start-loc (rand-nth (keys paths))]
    (debug-do [(println "StartLoc: ")
               (println start-loc)])
    (loop [walls walls
           unvisited (disj (set (keys paths)) start-loc)]
      (if-let [loc (when-let [s (seq unvisited)] (rand-nth s))]
        (let [walk (iterate (comp rand-nth paths) loc)
              steps (zipmap (take-while unvisited walk) (next walk))]
          (debug-do [(println "-------------------Loc---------------------")
                     (println loc)
                     (println "-------------------Walks-------------------")
                     (println (paths loc))
                     (println "-------------------Unvisited---------------")
                     (println unvisited)
                     (println "-------------------Steps---------------")
                     (println steps)])
          (recur (reduce disj walls (map set steps))
                 (reduce disj unvisited (keys steps))))
        walls))))

;concat
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/concat
(defn grid
  [w h]
  (set (concat
         (for [i (range (dec w)) j (range h)] #{[i j] [(inc i) j]})
         (for [i (range w) j (range (dec h))] #{[i j] [i (inc j)]}))))

;doto
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/doto
;
;proxy
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/proxy
;
;doseq
;http://clojure.github.io/clojure/clojure.core-api.html#clojure.core/doseq
(defn draw
  [w h maze]
  (doto (javax.swing.JFrame. "Maze")
    (.setContentPane
      (doto (proxy [javax.swing.JPanel] []
              (paintComponent [^java.awt.Graphics g]
                (let [g (doto ^java.awt.Graphics2D (.create g)
                          (.scale 10 10)
                          (.translate 1.5 1.5)
                          (.setStroke (java.awt.BasicStroke. 0.4)))]
                  (.drawRect g -1 -1 w h)
                  (doseq [[[xa ya] [xb yb]] (map sort maze)]
                    (let [[xc yc] (if (= xa xb)
                                    [(dec xa) ya]
                                    [xa (dec ya)])]
                      (.drawLine g xa ya xc yc))))))
        (.setPreferredSize (java.awt.Dimension.
                             (* 10 (inc w)) (* 10 (inc h))))))
    .pack
    (.setVisible true)))

(defn -main
  "A maze generator with supplied width and height."
  [& args]
  (println "Hello, Maze!")
  (println "Input the width:")
  (when-let [w (read)]
    (println "Input the height:")
    (when-let [h (read)]
      (draw w h (maze (grid w h))))))
