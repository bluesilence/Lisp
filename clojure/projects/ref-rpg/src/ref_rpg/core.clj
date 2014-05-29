(ns ref-rpg.core
  (:gen-class))

(defmacro futures
  [n & exprs]
  (vec (for [_ (range n)
             expr exprs]
         `(future ~expr))))

(defmacro wait-futures
  [& args]
  `(doseq [f# (futures ~@args)]
     @f#))

(defn character
  [name & {:as opts}]
  (ref (merge {:name name :items #{} :health 500}
              opts)))

(def smaug (character "Smaug" :health 500 :strength 400 :items (set (range 50))))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))

(defn loot
  [from to]
  (dosync
    (when-let [item (first (:items @from))]
      (commute to update-in [:items] conj item)
      (alter from update-in [:items] disj item))))

(defn attack
  [aggressor target]
  (dosync
    (let [damage (* (rand 0.1) (:strength @aggressor))]
      (commute target update-in [:health] #(max 0 (- % damage))))))

(defn heal
  [healer target]
  (dosync
    (let [aid (* (rand 0.1) (:mana @healer))]
      (when (pos? aid)
        (commute healer update-in [:mana] - (max 5 (/ aid 5)))
        (commute target update-in [:health] + aid)))))

(def alive? (comp pos? :health))

(defn play
  [character action other]
  (while (and (alive? @character)
              (alive? @other)
              (action character other))
    (Thread/sleep (rand-int 50))))

(defn -main
  "P181 of Clojure Programming: RPG game as a demo of ref."
  [& args]
  (println "Hello, RPG!")
  (println "Before loot: ")
  (println @smaug)
  (println @bilbo)
  (println @gandalf)
  (wait-futures 1
                (while (loot smaug bilbo))
                (while (loot smaug gandalf)))
  (println "After loot: ")
  (println @smaug)
  (println @bilbo)
  (println @gandalf)
  (println "bilbo combat with smaug:")
  (wait-futures 1
                (play bilbo attack smaug)
                (play smaug attack bilbo))
  (println "smaug's health: " ((comp :health deref) smaug))
  (println "bilbo's health: " ((comp :health deref) bilbo))
  (println "Historical battle:")
  (dosync
    (alter smaug assoc :health 500)
    (alter bilbo assoc :health 100))
  (wait-futures 1
                (play bilbo attack smaug)
                (play smaug attack bilbo)
                (play gandalf heal bilbo))
  (map (comp #(select-keys % [:name :health :mana]) deref) [smaug bilbo gandalf])
)
