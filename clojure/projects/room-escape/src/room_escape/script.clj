(ns room-escape.script
  (:gen-class))

(use '[clojure.string :as string :only [join] :exclude [reverse]])
(use '[room-escape.common :only [parse-int locate-by-id locate-by-name set-visible visible? set-win display]])

(declare objects)

(def starting-message "
************************************************
  You were drunk last night.
  and you found yourself...locked!

  ...What the fuck?!

  Anyway, let's try to escape from here first!
************************************************")

(defn- rooms [player-id]
  (vector {:id 1
             :category 0
             :name "starting room"
             :description {:default-check "The room is dark. Try look around."
                           :near-check "This is a strange room. Barely no furniture except a [table] and a [door]."}
             :items [2 3]
             }))

(defn- spots [player-id]
  (vector {:id 2
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
             :items [5]}))

(defn- items [player-id]
  (vector {:id 4
             :category 2
             :name "card"
             :pickable true
             :description {:default-check "It's a card with some words on it."
                           :near-check "
 'Oops, I failed it again.
  Only 1 step before game clear...
  But I stopped at 1024 T T

  Hey stranger!
  Can you help me get to the number?'
                                       
What is this...a riddle?"}}
            {:id 5
             :category 2
             :name "password-panel"
             :description {:default-check "There are button 0~9 on the panel. The length of the password seems to be 4. Maybe you can press the buttons..."}
             :state (atom "")
             :action [{:name "press"
                       :description "Press button 0 ~ 9 on the [password-panel]"
                       :usage "press [0~9]. Eg. press 0"
                       :function
                         #(let [player-id %1
                                button (parse-int %2 -1)]
                            (if (or (> button 9) (< button 0))
                              (display "Invalid button: " %2)
                              (let [state (:state (locate-by-id player-id 5))]
                                (swap! state (comp string/join reverse (partial concat %2) reverse))
			        (if (> (count @state) 4)
				  (swap! state subs 1 5))
                                (display "You pressed button " button)
                                (if (and (= @state "2048")
                                         (not (visible? player-id 6)))
                                  (do 
                                    (set-visible player-id 6)
                                    (display "The bottom of the [password-panel] opened. A [key] fell down to the floor."))))))}]}

            {:id 6
             :category 2
             :name "key"
             :description {:default-check "It's a key which fell from under the [password-panel]."
                          :near-check "Maybe it can open the [door]?"}
             :on-use #(let [player-id %1
                            object-id %2
                            target (locate-by-name player-id object-id)]
                        (if (= (:name target) "door")
                          (do
			    (set-win player-id)
		            true)
                          false))
             :pickable true}))

(def starting-room 1)

(defn objects [player-id]
  (vec (concat (rooms player-id) (spots player-id) (items player-id))))

(def win-message "
************************************************
  Congratulations!
  The door is opened! You walked out...
  Still, you are trying to figure this out.
************************************************")
