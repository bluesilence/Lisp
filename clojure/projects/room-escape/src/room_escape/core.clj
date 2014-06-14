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

(defn display [message & args]
  (println (str message (string/join args))))

(def categories ["room"
                 "spot"
                 "item"])

(def rooms [{:id 1
             :category 0
             :name "starting room"
             :description {:default-check "The room is dark. Try look around."
                           :near-check "This is a strange room. Barely no furniture except a [table] and a [door]."}
             :items [2 3]
             }])

(def starting-room (:id (first rooms)))

(def spots [{:id 2
             :category 1
             :name "table"
             :description {:default-check "This is a small square table. It seems that there is something on the table..."
                           :near-check "There is a [card] on the table."}
             :items [4]}
            {:id 3
             :category 1
             :name "door"
             :description{:default-check "The door is locked."
                          :near-check "There is a [password-panel] beside the door. Maybe the password is written somewhere..."}
             :items [5]}])

(def items [{:id 4
             :category 2
             :name "card"
             :description {:default-check "It's a card with number 0428 on it."}}
            {:id 5
             :category 2
             :name "password-panel"
             :description {:default-check "There are button 0~9 on the panel. The length of the password seems to be 4."}
             :state "Locked"}])

(def objects (vec (concat rooms spots items)))

; To-Do: use macro to generate these locate functions
(defn locate-by-id [id]
  (first (filter (comp #(= % id) #(:id %)) objects)))

(defn locate-by-name [name]
  (first (filter (comp #(= % name) #(:name %)) objects)))

(defn locate-by-category [category-id]
  (filter (comp #(= % category-id) #(:category %)) objects))

(defn room? [id]
  (let [category-id (:category (locate-by-id id))]
    (= "room" (nth categories category-id))))

(defn spot? [id]
  (let [category-id (:category (locate-by-id id))]
    (= "spot" (nth categories category-id))))

(defn item? [id]
  (let [category-id (:category (locate-by-id id))]
    (= "item" (nth categories category-id))))

(def visible (atom #{}))

(defn visible? [id]
  (contains? @visible id))

(defn set-visible [id]
  (swap! visible conj id))

(defn set-invisible [id]
  (swap! visible disj id))

(defn toggle-visible [id]
  (if (visible? id)
    (set-invisible id)
    (set-visible id)))

(defn describe
  [id & args]
  (if-let [object (locate-by-id id)]
    (let [key-str (string/join args)]
      (if (empty? key-str)
        (display (:default-check (:description object)))
        (display (get (:description object) (keyword key-str)))))
    (display "No object with id " id " found.")))

(def current-status (atom {:room -1
                           :spot -1
                           :items []}))

(defn in-status [id]
  (let [status @current-status]
    (or (= (:room status) id)
        (= (:spot status) id)
        (contains? (:items status) id))))

(defn update-status [key value]
  (swap! current-status assoc key value))

(defn update-room [room-id]
  (update-status :room room-id))

(defn get-current-spot-id []
  (:spot @current-status))

(defn get-current-room []
  (when-let [id (:room @current-status)]
    (:name (locate-by-id id))))

(defn update-spot [spot-id]
  (when-let [old-spot (locate-by-id (get-current-spot-id))]
    (doseq [item (:items old-spot)]
      (set-invisible item)))
  (if-let [new-spot (locate-by-id spot-id)]
    (do
      (doseq [item (:items new-spot)]
        (set-visible item))
      (update-status :spot spot-id))
    (display "Spot " spot-id " doesn't exist.")))

(defn get-location []
  (display "Your current location:")
  (when-let [room (get-current-room)]
    (display "Room: [" room "]"))
  (when-let [spot (:name (locate-by-id (get-current-spot-id)))]
    (display "Spot: [" spot "]")))

(defn check [object-name]
  (if (nil? object-name)
    (display "Object name cannot be nil.")
    (if-let [object (locate-by-name object-name)]
      (let [id (:id object)]
        (if (visible? id)
          (if (in-status id) ; If object is in status, set its items are visible
            (do
                (doseq [item (:items object)]
                  (set-visible item))
                (describe id 'near-check))
            (describe id))
          (display "You didn't see any [" object-name "] around here.")))
      (display "There is no [" object-name "] around here."))))

(defn goto [spot-name]
  (if (empty? spot-name)
    (display "Spot name cannot be empty.")
    (if-let [spot (locate-by-name spot-name)]
      (let [id (:id spot)]
        (if (visible? id)
          (if (spot? id)
            (do (display "You went to the [" spot-name "] to take a closer look.")
                (update-spot id)
                (describe id 'near-check))
            (display "[" spot-name "] is not somewhere to go to."))
          (display "You didn't see any [" spot-name "] around here.")))
      (display "There is no [" spot-name "] around here."))))

(defn look-around []
  (display "You looked around.")
  (check (get-current-room)))

(def continue (atom true))
(defn quit []
  (swap! continue not)
  (display "Bye~!"))

(defn continue? []
  (= @continue true))

(declare action-list)

(defn help []
  (display "Pick one [action] below:")
  (doseq [action action-list]
    (display (str "[" (:name action) "] " (:description action)))))

(def action-list [{:name "help" :function help :description "Get help information"},
                  {:name "get-location" :function get-location :description "Get current location"},
                  {:name "look-around" :function look-around :description "Look around the room"},
                  {:name "check" :function check :description "Check room/spot/item"},
                  {:name "goto" :function goto :description "Goto spot"},
                  {:name "quit" :function quit :description "Quit the game"}])

(defn execute [command & args]
  (let [action (->> action-list (filter (comp #(= (:name %) (str command)))) first :function)
        args-str (string/join args)]
    (if (nil? action)
      (do 
        (display "Unsupported command: " command)
        (help))
      (if (string/blank? args-str)
          (action)
          (action args-str)))))

(defn initialize []
  (display starting-message)
  (update-room starting-room)
  (set-visible starting-room)
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
    (do (when-not (= command-str "")
      (let [command-vector (string/split command-str #" ")]
        (execute (first command-vector) (string/join (rest command-vector)))))
      (when (continue?)
        (display-prompt)
        (recur (str (read-line)))))))
