(ns room-escape.util
  (:gen-class))

(use '[clojure.string :as string :only [join]])
(use '(room-escape script common))

(def categories ["room"
                 "spot"
                 "item"])

(defn construct-player [player-id]
  (let [initial-context
        {:player-objects (objects player-id) 
	 :current-status (atom {:room -1
                           :spot -1
                           :items #{}})
         :visible (atom #{})
         :win (atom false)
         :continue (atom true)}]
  (swap! players assoc player-id initial-context)))

(defn room? [player-id object-id]
  (let [category-id (:category (locate-by-id player-id object-id))]
    (= "room" (nth categories category-id))))

(defn spot? [player-id object-id]
  (let [category-id (:category (locate-by-id player-id object-id))]
    (= "spot" (nth categories category-id))))

(defn item? [player-id object-id]
  (let [category-id (:category (locate-by-id player-id object-id))]
    (= "item" (nth categories category-id))))

(defn describe
  [player-id object-id & args]
  (if-let [object (locate-by-id player-id object-id)]
    (let [key-str (string/join args)]
      (if (empty? key-str)
        (display (:default-check (:description object)))
        (display (get (:description object) (keyword key-str)))))
    (display "No object with id " object-id " found.")))

(defn in-status? [player-id object-id]
  (let [status @(:current-status (get @players player-id))]
    (or (= (:room status) object-id)
        (= (:spot status) object-id)
        ((set (:items status)) object-id))))

(defn update-status [player-id key value]
  (let [status (:current-status (get @players player-id))]
  (swap! status assoc key value)))

(defn add-item-by-id [player-id item-id]
  (let [items (:items @(:current-status (get @players player-id)))]
    (update-status player-id :items (conj items item-id))))

(defn add-item-by-name [player-id item-name]
  (if-let [item-id (:id (locate-by-name player-id item-name))]
    (if-not (in-status? player-id item-id)
      (do 
        (add-item-by-id player-id item-id)
        (display "You picked the " (enclose item-name) "."))
      (display "You have already picked the " (enclose item-name) "."))))

(defn update-room [player-id room-id]
  (update-status player-id :room room-id))

(defn get-current-spot-id [player-id]
  (:spot @(:current-status (get @players player-id))))

(defn get-current-room [player-id]
  (when-let [id (:room @(:current-status (get @players player-id)))]
    (:name (locate-by-id player-id id))))

(defn update-spot [player-id spot-id]
  (when-let [old-spot (locate-by-id player-id (get-current-spot-id player-id))]
    (doseq [item (:items old-spot)]
      (set-invisible player-id item)))
  (if-let [new-spot (locate-by-id player-id spot-id)]
    (do
      (doseq [item (:items new-spot)]
        (set-visible player-id item))
      (update-status player-id :spot spot-id))
    (display "Spot " spot-id " doesn't exist.")))

(defn get-location [player-id]
  (display "Your current location:")
  (when-let [room (get-current-room player-id)]
    (display "Room: " (enclose room)))
  (when-let [spot (:name (locate-by-id player-id (get-current-spot-id player-id)))]
    (display "Spot: " (enclose spot))))

(defn check [player-id object-name]
  (if (nil? object-name)
    (display "Object name cannot be nil.")
    (if-let [object (locate-by-name player-id object-name)]
      (let [id (:id object)]
        (if (in-status? player-id id) ; If object is in status, set its items are visible
          (do
              (doseq [item (:items object)]
                (set-visible player-id item))
              (describe player-id id 'near-check))
          (if (visible? player-id id)
            (describe player-id id)
            (display "You didn't see any " (enclose object-name) " around here."))))
      (display "There is no " (enclose object-name) " around here."))))

(defn pick [player-id item-name]
  (if (nil? item-name)
    (display "Item name cannot be nil.")
    (if-let [object (locate-by-name player-id item-name)]
      (let [id (:id object)]
        (if (visible? player-id id)
          (if (:pickable object)
            (add-item-by-name player-id item-name)
            (display (enclose item-name) " cannot be picked."))
          (display "You didn't see any " (enclose item-name) " around here.")))
      (display "There is no " (enclose item-name) " around here."))))

(defn show-items [player-id]
  (let [items (:items @(:current-status (get @players player-id)))
        items-count (count items)]
    (if (> items-count 0)
      (do
        (display "You have " items-count " items:")
        (doseq [item-id items]
          (display (enclose (get-name player-id item-id)) ":")
          (describe player-id item-id)))
      (display "You don't have any item by now."))))

(defn goto [player-id spot-name]
  (if (empty? spot-name)
    (display "Spot name cannot be empty.")
    (if-let [spot (locate-by-name player-id spot-name)]
      (let [id (:id spot)]
        (if (visible? player-id id)
          (if (spot? player-id id)
            (do (display "You went to the " (enclose spot-name) " to take a closer look.")
                (update-spot player-id id)
                (describe player-id id 'near-check))
            (display (enclose spot-name) " is not somewhere to go to."))
          (display "You didn't see any " (enclose spot-name) " around here.")))
      (display "There is no " (enclose spot-name) " around here."))))

(defn look-around [player-id]
  (display "You looked around.")
  (check player-id (get-current-room player-id)))

(defn use-item [player-id item-name target-name]
  (let [item (locate-by-name player-id item-name)
        target (locate-by-name player-id target-name)]
    (let [item-id (:id item)
          target-id (:id target)]
      (if (not (visible? player-id item-id))
        (display "You didn't see any " (enclose item-name) " around here.")
        (if (not (visible? player-id target-id))
          (display "You didn't see any " (enclose target-name) " around here.")
          (if (not (in-status? player-id item-id))
            (display "You don't have the " (enclose item-name) " at hand.")
            (if-let [use-function (:on-use item)]
              (do
                (if (use-function player-id target-name)
                  (display "You used the " (enclose item-name) " at the " (enclose target-name) ".")
                  (display "The " (enclose item-name) " cannot be used on the " (enclose target-name) ".")))
              (display (enclose item-name) " cannot be used."))))))))

(defn quit [player-id]
  (let [continue-context (:continue (get @players player-id))]
    (swap! continue-context not)))

(defn continue? [player-id]
  (let [continue-context (:continue (get @players player-id))]
    (= @continue-context true)))

(defn get-visible-objects [player-id]
  (let [objects (:player-objects (get @players player-id))]
    (filter (comp (partial visible? player-id) :id) objects)))

(defn get-object-actions [player-id]
  (let [object-actions (filter (comp not nil?) (map :action (get-visible-objects player-id)))
        results (transient [])]
    (doseq [act object-actions]
      (doseq [a act]
        (conj! results a)))
    (persistent! results)))

(declare get-action-list)

(defn help [player-id]
  (display "")
  (display "Pick one " (enclose "action") " below:")
  (display "----------------------------")
  (doseq [action (get-action-list player-id)]
    (display (enclose (:name action)) " " (:description action))
    (display "  Usage: " (:usage action))))

(defn get-action-list [player-id]
  (conj (get-object-actions player-id)
                  {:name "help" :function help :description "Get help information" :usage "help"},
                  {:name "show-items" :function show-items :description "Show your items" :usage "show-items"},
                  {:name "get-location" :function get-location :description "Get current location" :usage "get-location"},
                  {:name "look-around" :function look-around :description "Look around the room" :usage "look-around"},
                  {:name "check" :function check :description "Check room/spot/item" :usage (str "check " (enclose "object") ". Eg. check door")},
                  {:name "pick" :function pick :description (str "Pick an item. So you can take a near " (enclose "check") " at the item, or " (enclose "use") " the item.") :usage (str "pick " (enclose "item") ". Eg. pick card")},
                  {:name "use" :function use-item :description "Use one of your items at a target object." :usage (str "use " (enclose "item") " " (enclose "target-object") ". Eg. use key door")}
                  {:name "goto" :function goto :description "Goto spot. So you can take a close look at the spot." :usage (str "goto " (enclose "spot") ". Eg. goto door")},
                  {:name "quit" :function quit :description "Quit the game" :usage "quit"}))
