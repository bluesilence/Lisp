(ns room-escape.core
  (:gen-class))

(use '[clojure.string :as string])
(use '[room-escape.script :as script])
(use '[room-escape util script])

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
