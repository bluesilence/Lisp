(ns room-escape.core
  (:gen-class))

(def starting-message "
************************************************
  You were drunk last night.
  and you found yourself...locked!

  ...What the fuck?!

  Anyway, let's try to escape from here first!
************************************************")

(def starting-room "Starting room")
(def current-status (atom {:location []
                           :items []}))

(defn update-status [key value]
  (swap! current-status assoc key value))

(defn update-room [room-name]
  (update-status :location [room-name (second (:location @current-status))]))

(defn get-current-room []
  (first (:location @current-status)))

(def rooms [{:id 1
             :name "Starting room"
             :visible 1
             :description "This is a strange room. Barely no furniture except a table and a door."
             :spots [1 2]
             }])

(def spots [{:id 1
             :name "Table"
             :visible 1
             :description "This is a small square table. It seems that there is something on the table..."
             :items [1]}
            {:id 2
             :name "Door"
             :visible 1
             :description "It is a door locked with a password panel. Maybe the password is written somewhere..."
             :items [2]}])

(def items [{:id 1
             :name "Card"
             :visible 0
             :description "It's a card with number 0428 on it."}
            {:id 2
             :name "Password Panel"
             :visible 0
             :description "There are button 0~9 on the panel. The length of the password seems to be 4."
             :state "Locked"}])

(def objects (vec (concat rooms spots items)))

; To-Do: use macro to generate these locate functions
(defn locate-by-id [id]
  (doseq [object objects]
    (let [object-id (:id object)]
      (if (= object-id id)
        object
        nil))))

(defn locate-by-name [name]
  (first (filter (comp #(= % name) #(:name %)) objects)))

(defn display [description]
  (println ">>" description))

(defn check [object-name]
  (if (nil? object-name)
    (println "Object name cannot be nil.")
    (if-let [object (locate-by-name object-name)]
      (if (= (:visible object) 1)
        (display (:description object))
        (println "You didn't see any " object-name " around here."))
      (println "There is no " object-name " around here."))))

(defn look-around []
  (display "You looked around.")
  (check (get-current-room)))

(def action-list [{:name "look-around" :function look-around :description "Look around the room"},
                  {:name "check" :function check :description "Check room/spot/item"}])

(defn help []
  (display "Pick one [action] below:")
  (doseq [action action-list]
    (display (str "[" (:name action) "] " (:description action)))))

(defn execute [command]
  ; To-Do: split command-str by ' '
  (let [action (->> action-list (filter (comp #(= (:name %) (str command)))) first :function)]
    (if (nil? action)
      (println "Unsupported command: " command)
      (action))))

(defn initialize []
  (println starting-message)
  (update-room starting-room)
  (help))

(defn -main
  "A text only game to escape from a locked room."
  [& args]
  (initialize)
  (println "Your action: ")
  (loop [command-str (str (read))]
    (if (= command-str "quit")
      (println "Bye~!")
      (do (when-not (= command-str "")
            (execute command-str))
          (println "Your action: ")
          (recur (str (read)))))))
