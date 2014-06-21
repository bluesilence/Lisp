(ns room-escape.core
  (:gen-class))

(use '[clojure.string :as string])
(use '[room-escape.script :as script])

(defn display [message & args]
  (println (str message (string/join args))))

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

(defn get-visible-objects []
  (filter (comp visible? :id) objects))

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
  (swap! continue not))

(defn continue? []
  (= @continue true))

(defn get-object-actions []
  (let [object-actions (filter (comp not nil?) (map :action (get-visible-objects)))
        results (transient [])]
    (doseq [act object-actions]
      (doseq [a act]
        (conj! results a)))
    (persistent! results)))

(declare get-action-list)

(defn help []
  (display "Pick one [action] below:")
  (doseq [action (get-action-list)]
    (display (str "[" (:name action) "] " (:description action)))))

(defn get-action-list []
  (conj (get-object-actions)
                  {:name "help" :function help :description "Get help information"},
                  {:name "get-location" :function get-location :description "Get current location"},
                  {:name "look-around" :function look-around :description "Look around the room"},
                  {:name "check" :function check :description "Check room/spot/item"},
                  {:name "goto" :function goto :description "Goto spot"},
                  {:name "quit" :function quit :description "Quit the game"}))

(defn execute [command & args]
  (println "Command: " command)
  (println "Args: " args)
  (let [action (->> (get-action-list) (filter (comp #(= (:name %) (str command)))) first :function)
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
      (if (win?)
        (do
          (swap! continue not)
          (display win-message))
        (when (continue?)
          (display-prompt)
          (recur (str (read-line)))))))
  (display "Goodbye~!"))
