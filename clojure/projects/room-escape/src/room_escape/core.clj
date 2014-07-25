(ns room-escape.core
  (:gen-class))

(use '[clojure.string :as string])
(use '[room-escape.script :as script])
(use '[room-escape common util script])

; macro doesn't help...it generates at compiling time
; (defmacro call-action [action & args]
;  (println "Action: " action)
;  (println "Args: " args)
;  `(~action ~@args))

(defn execute [command & args]
  (println "Command: " command)
  (println "Args: " args)
  (let [action (->> (get-action-list) (filter (comp #(= (:name %) (str command)))) first :function)
        args-list (first args)]
    (if (nil? action)
      (do 
        (display "Unsupported command: " command)
        (help))
      (let [args-num (count args-list)] ; at most 2 args are supported
        (cond (= 0 args-num)
                 (action)
              (= 1 args-num)
                 (action (first args-list))
              (= 2 args-num)
                 (action (first args-list) (second args-list)))))))

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
        (execute (first command-vector) (rest command-vector))))
      (if (win?)
        (do
          (swap! continue not)
          (display win-message))
        (when (continue?)
          (display-prompt)
          (recur (str (read-line)))))))
  (display "Goodbye~!"))
