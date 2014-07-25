(ns room-escape.script
  (:gen-class))

(use '[room-escape.common :only [parse-int locate-by-id locate-by-name set-visible display]])

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
             :opened (atom false)
             :items [5]}])

(def items [{:id 4
             :category 2
             :name "card"
             :pickable true
             :description {:default-check "It's a card with number 0428 on it."
                           :near-check "Perhaps you can [press] the numbers at the [password-panel]?"}}
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
                              (let [states (:state (locate-by-id 5 objects))]
                                (swap! states conj button)
                                (println "You pressed button " button)
                                (let [inputs (vec (rseq @states))]
                                  (if (>= (count inputs) 4)
                                    (if (-> inputs (subvec 0 4) (rseq) (vec) (= [0 4 2 8]))
                                    (do 
                                      (set-visible 6)
                                      (display "The bottom of the [password-panel] opened. A [key] fell down to the floor."))))))))}]}

            {:id 6
             :category 2
             :name "key"
             :description {:default-check "It's a key which fell from under the [password-panel]."
                          :near-check "Maybe it can open the [door]?"}
             :on-use #(let [target (locate-by-name % objects)]
                        (if (= (:name target) "door")
                          (do
                            (swap! (:opened target) not)
                            true)
                          false))
             :pickable true}])

(def starting-room (:id (first rooms)))

(def objects (vec (concat rooms spots items)))

(defn win? []
  @(:opened (locate-by-id 3 objects)))

(def win-message "
************************************************
  Congratulations!
  The door is opened! You walked out...
  Still, you are trying to figure this out.
************************************************")
