(ns room-escape.util
  (:gen-class))

(use '[clojure.string :as string])
(use '(room-escape script common))

(def current-status (atom {:room -1
                           :spot -1
                           :items #{}}))

(def categories ["room"
                 "spot"
                 "item"])

(defn room? [id]
  (let [category-id (:category (locate-by-id id objects))]
    (= "room" (nth categories category-id))))

(defn spot? [id]
  (let [category-id (:category (locate-by-id id objects))]
    (= "spot" (nth categories category-id))))

(defn item? [id]
  (let [category-id (:category (locate-by-id id objects))]
    (= "item" (nth categories category-id))))

(defn describe
  [id & args]
  (if-let [object (locate-by-id id objects)]
    (let [key-str (string/join args)]
      (if (empty? key-str)
        (display (:default-check (:description object)))
        (display (get (:description object) (keyword key-str)))))
    (display "No object with id " id " found.")))

(defn in-status? [id]
  (let [status @current-status]
    (or (= (:room status) id)
        (= (:spot status) id)
        ((set (:items status)) id))))

(defn update-status [key value]
  (swap! current-status assoc key value))

(defn add-item-by-id [item-id]
  (let [items (:items @current-status)]
    (update-status :items (conj items item-id))))

(defn add-item-by-name [item-name]
  (if-let [item-id (:id (locate-by-name item-name objects))]
    (if-not (in-status? item-id)
      (do 
        (add-item-by-id item-id)
        (display "You picked the " (enclose item-name) "."))
      (display "You have already picked the " (enclose item-name) "."))))

(defn update-room [room-id]
  (update-status :room room-id))

(defn get-current-spot-id []
  (:spot @current-status))

(defn get-current-room []
  (when-let [id (:room @current-status)]
    (:name (locate-by-id id objects))))

(defn update-spot [spot-id]
  (when-let [old-spot (locate-by-id (get-current-spot-id) objects)]
    (doseq [item (:items old-spot)]
      (set-invisible item)))
  (if-let [new-spot (locate-by-id spot-id objects)]
    (do
      (doseq [item (:items new-spot)]
        (set-visible item))
      (update-status :spot spot-id))
    (display "Spot " spot-id " doesn't exist.")))

(defn get-location []
  (display "Your current location:")
  (when-let [room (get-current-room)]
    (display "Room: " (enclose room)))
  (when-let [spot (:name (locate-by-id (get-current-spot-id) objects))]
    (display "Spot: " (enclose spot))))

(defn check [object-name]
  (if (nil? object-name)
    (display "Object name cannot be nil.")
    (if-let [object (locate-by-name object-name objects)]
      (let [id (:id object)]
        (if (in-status? id) ; If object is in status, set its items are visible
          (do
              (doseq [item (:items object)]
                (set-visible item))
              (describe id 'near-check))
          (if (visible? id)
            (describe id)
            (display "You didn't see any " (enclose object-name) " around here."))))
      (display "There is no " (enclose object-name) " around here."))))

(defn pick [item-name]
  (if (nil? item-name)
    (display "Item name cannot be nil.")
    (if-let [object (locate-by-name item-name objects)]
      (let [id (:id object)]
        (if (visible? id)
          (if (:pickable object)
            (add-item-by-name item-name)
            (display (enclose item-name) " cannot be picked."))
          (display "You didn't see any " (enclose item-name) " around here.")))
      (display "There is no " (enclose item-name) " around here."))))

(defn show-items []
  (let [items (:items @current-status)
        items-count (count items)]
    (if (> items-count 0)
      (do
        (display "You have " items-count " items:")
        (doseq [item-id items]
          (display (enclose (get-name item-id objects)) ":")
          (describe item-id)))
      (display "You don't have any item by now."))))

(defn goto [spot-name]
  (if (empty? spot-name)
    (display "Spot name cannot be empty.")
    (if-let [spot (locate-by-name spot-name objects)]
      (let [id (:id spot)]
        (if (visible? id)
          (if (spot? id)
            (do (display "You went to the " (enclose spot-name) " to take a closer look.")
                (update-spot id)
                (describe id 'near-check))
            (display (enclose spot-name) " is not somewhere to go to."))
          (display "You didn't see any " (enclose spot-name) " around here.")))
      (display "There is no " (enclose spot-name) " around here."))))

(defn look-around []
  (display "You looked around.")
  (check (get-current-room)))

(defn use-item [item-name target-name]
  (let [item (locate-by-name item-name objects)
        target (locate-by-name target-name objects)]
    (let [item-id (:id item)
          target-id (:id target)]
      (if (not (visible? item-id))
        (display "You didn't see any " (enclose item-name) " around here.")
        (if (not (visible? target-id))
          (display "You didn't see any " (enclose target-name) " around here.")
          (if (not (in-status? item-id))
            (display "You don't have the " (enclose item-name) " at hand.")
            (if-let [use-function (:on-use item)]
              (do
                (if (use-function target-name)
                  (display "You used the " (enclose item-name) " at the " (enclose target-name) ".")
                  (display "The " (enclose item-name) " cannot be used on the " (enclose target-name) ".")))
              (display (enclose item-name) " cannot be used."))))))))

(def continue (atom true))
(defn quit []
  (swap! continue not))

(defn continue? []
  (= @continue true))

(defn get-visible-objects []
  (filter (comp visible? :id) objects))

(defn get-object-actions []
  (let [object-actions (filter (comp not nil?) (map :action (get-visible-objects)))
        results (transient [])]
    (doseq [act object-actions]
      (doseq [a act]
        (conj! results a)))
    (persistent! results)))

(declare get-action-list)

(defn help []
  (display "")
  (display "Pick one " (enclose "action") " below:")
  (display "----------------------------")
  (doseq [action (get-action-list)]
    (display (enclose (:name action)) " " (:description action))
    (display "  Usage: " (:usage action))))

(defn get-action-list []
  (conj (get-object-actions)
                  {:name "help" :function help :description "Get help information" :usage "help"},
                  {:name "show-items" :function show-items :description "Show your items" :usage "show-items"},
                  {:name "get-location" :function get-location :description "Get current location" :usage "get-location"},
                  {:name "look-around" :function look-around :description "Look around the room" :usage "look-around"},
                  {:name "check" :function check :description "Check room/spot/item" :usage (str "check " (enclose "object") ". Eg. check door")},
                  {:name "pick" :function pick :description (str "Pick an item. So you can take a near " (enclose "check") " at the item, or " (enclose "use") " the item.") :usage (str "pick " (enclose "item") ". Eg. pick card")},
                  {:name "use" :function use-item :description "Use one of your items at a target object." :usage (str "use " (enclose "item") " " (enclose "target-object") ". Eg. use key door")}
                  {:name "goto" :function goto :description "Goto spot. So you can take a close look at the spot." :usage (str "goto " (enclose "spot") ". Eg. goto door")},
                  {:name "quit" :function quit :description "Quit the game" :usage "quit"}))
