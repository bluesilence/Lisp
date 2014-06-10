(ns room-escape.core
  (:gen-class))

(def starting-message "You were drunk last night...
                      and you found yourself...locked!
                      Now, try to escape from here!")

(def starting-room "Starting room")
(def current-status (atom {:room {}}))

(def rooms [{:id 1
             :name "Starting room"
             :visible 1
             :description "This is a strange room. Barely no furniture except for a table and a door."
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

(defn check [object-name]
  (if-let [object (locate-by-name object-name)]
    (if (= (:visible object) 1)
      (println (:description object))
      (println "You didn't see any " object-name " around here."))
    (println "There is no " object-name " around here.")))

(defn look-around []
  (check (:name (:room @current-status))))

(def action-list [{:name "look-around" :function look-around :description "Look around the room"},
                  {:name "check" :function check :description "Check room/spot/item"}])

(defn help []
  (println "-----------Available actions------------")
  (doseq [action action-list]
    (println "Name: " (:name action))
    (println "Description: " (:description action))
    (println)))

(defn execute [command]
  ; To-Do: split command-str by ' '
  (doseq [action action-list]
    (when (= (:name action) command)
      (:function action))))

(defn initialize []
  (println starting-message)
  (swap! current-status assoc :room (locate-by-name starting-room))
  (help))

(defn -main
  "A text only game to escape from a locked room."
  [& args]
  (initialize)
  (loop [command-str ""]
    (if (= command-str "quit")
      (println "Bye~!")
      (do (when-not (= command-str "")
            (execute command-str))
          (println "Your action: ")
          (recur (read))))))
