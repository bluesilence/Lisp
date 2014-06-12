(ns room-escape.core
  (:gen-class))

(use '[clojure.string :as string])

(def starting-message "
************************************************
  You were drunk last night.
  and you found yourself...locked!

  ...What the fuck?!

  Anyway, let's try to escape from here first!
************************************************")

(def current-status (atom {:location []
                           :items []}))

(defn update-status [key value]
  (swap! current-status assoc key value))

(defn update-room [room-id]
  (update-status :location [room-id (second (:location @current-status))]))

(def rooms [{:id 1
             :name "starting room"
             :description "This is a strange room. Barely no furniture except a [ table ] and a [ door ]."
             :items [2 3]
             }])

(def starting-room (:id (first rooms)))

(def spots [{:id 2
             :name "table"
             :description "This is a small square table. It seems that there is something on the table..."
             :items [4]}
            {:id 3
             :name "door"
             :description "It is a door locked with a password panel. Maybe the password is written somewhere..."
             :items [5]}])

(def items [{:id 4
             :name "card"
             :description "It's a card with number 0428 on it."}
            {:id 5
             :name "password Panel"
             :description "There are button 0~9 on the panel. The length of the password seems to be 4."
             :state "Locked"}])

(def objects (vec (concat rooms spots items)))
(def visible (atom #{}))

(defn visible? [id]
  (contains? @visible id))

(defn toggle-visible [id]
  (if (visible? id)
    (swap! visible disj id)
    (swap! visible conj id)))

; To-Do: use macro to generate these locate functions
(defn locate-by-id [id]
  (first (filter (comp #(= % id) #(:id %)) objects)))

(defn locate-by-name [name]
  (first (filter (comp #(= % name) #(:name %)) objects)))

(defn get-current-room []
  (when-let [id (first (:location @current-status))]
    (:name (locate-by-id id))))

(defn display [description]
  (println description))

(defn check [object-name]
  (if (nil? object-name)
    (println "Object name cannot be nil.")
    (if-let [object (locate-by-name object-name)]
      (if (visible? (:id object))
        (do (display (:description object))
            (doseq [item (:items object)]
              (toggle-visible item)))
        (println "You didn't see any [" object-name "] around here."))
      (println "There is no [" object-name "] around here."))))

(defn look-around []
  (display "You looked around.")
  (check (get-current-room)))

(def action-list [{:name "look-around" :function look-around :description "Look around the room"},
                  {:name "check" :function check :description "Check room/spot/item"}])

(defn help []
  (display "Pick one [action] below:")
  (doseq [action action-list]
    (display (str "[" (:name action) "] " (:description action)))))

(defn execute [command & args]
  (let [action (->> action-list (filter (comp #(= (:name %) (str command)))) first :function)
        args-str (string/join args)]
    (if (nil? action)
      (println "Unsupported command: " command)
      (if (string/blank? args-str)
          (action)
          (action args-str)))))

(defn initialize []
  (println starting-message)
  (update-room starting-room)
  (toggle-visible starting-room)
  (help))

(defn display-prompt []
  (print ">>")
  (flush))

(defn -main
  "A text only game to escape from a locked room."
  [& args]
  (initialize)
  (display-prompt)
  (loop [command-str (str (read-line))]
    (if (= command-str "quit")
      (println "Bye~!")
      (do (when-not (= command-str "")
            (let [command-vector (string/split command-str #" ")]
              (execute (first command-vector) (string/join (rest command-vector)))))
          (display-prompt)
          (recur (str (read-line)))))))
