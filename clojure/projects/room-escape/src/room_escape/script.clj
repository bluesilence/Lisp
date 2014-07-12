(ns room-escape.script
  (:gen-class))

(use '[room-escape.common :only [parse-int locate-by-id]])

(declare objects)

(def starting-message "
************************************************
  You were drunk last night.
  and you found yourself...locked!

  ...What the fuck?!

  Anyway, let's try to escape from here first!
************************************************")

(def rooms [{:id 1
             :category 0
             :name "starting room"
             :description {:default-check "The room is dark. Try look around."
                           :near-check "This is a strange room. Barely no furniture except a [table] and a [door]."}
             :items [2 3]
             }])

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
             :description {:default-check "It's a card with number 0428 on it."}
             :action [{:name "pick"
                       :description "Pick the card"
                       :function nil}]}
            {:id 5
             :category 2
             :name "password-panel"
             :description {:default-check "There are button 0~9 on the panel. The length of the password seems to be 4."}
             :state (atom [])
             :action [{:name "press"
                       :description "Press button 0 ~ 9 on the [password-panel]"
                       :function
                         #(let [button (parse-int % -1)]
                            (if (or (> button 9) (< button 0))
                              (println "Invalid button: " %)
                              (do
                                (swap! (:state (locate-by-id 5 objects)) conj button)
                                (println "You pressed button " button))))}]}])

(def starting-room (:id (first rooms)))

(def objects (vec (concat rooms spots items)))

(defn win? []
  (if-let [states (vec (rseq @(:state (locate-by-id 5 objects))))]
    (if (>= (count states) 4)
      (-> states (subvec 0 4) (rseq) (vec) (= [0 4 2 8]))
      false)
    false))

(def win-message "
************************************************
  Congratulations!
  The door is opened! You walked out...
  Still, you are trying to figure this out.
************************************************")
