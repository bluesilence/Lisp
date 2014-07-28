(ns room-escape.core
  (:gen-class))

(import '[java.io BufferedReader InputStreamReader OutputStreamWriter])
(use 'server.socket)

(use '[clojure.string :as string :only [split]])
(use '[room-escape.script :as script])
(use '[room-escape common util script])

; macro doesn't help...it generates at compiling time
; (defmacro call-action [action & args]
;  (println "Action: " action)
;  (println "Args: " args)
;  `(~action ~@args))

(defn execute [player-id command & args]
  ;(println "Command: " command)
  ;(println "Args: " args)
  (let [action-info (->> (get-action-list player-id) (filter (comp #(= (:name %) (str command)))) first)
        action (:function action-info)
        args-list (first args)]
    (if (nil? action)
      (do 
        (display "Unsupported command: " command)
        (help player-id))
      (let [args-num (count args-list)] ; at most 2 args are supported
        (try
          (cond (= 0 args-num)
                   (action player-id)
                (= 1 args-num)
                   (action player-id (first args-list))
                (= 2 args-num)
                   (action player-id (first args-list) (second args-list)))
          (catch Exception e
            (.printStackTrace e)
            (display "[!]Incorrect usage.")
            (display "  Usage: " (:usage action-info))))))))

(defn initialize [player-id]
  (construct-player player-id)
  (Thread/sleep 2000)
  (let [starting-room (get-starting-room-by-player player-id)
        starting-room-id (:id starting-room)]
    (display (:starting-message starting-room))
    (update-room player-id starting-room-id)
    (set-visible player-id starting-room-id))
  (Thread/sleep 5000)
  (help player-id))

(def player-count (atom 0))

(defn game-server []
  (display "Opening game server...")
  (letfn [(echo [in out]
            (swap! player-count inc)
            (println "Player " @player-count " connected.")
            (binding [*in* (BufferedReader. (InputStreamReader. in))
                      *out* (OutputStreamWriter. out)]
              (let [current-player @player-count]
                (display welcome-message)
                (initialize current-player)
                (display-prompt)
                (loop [command-str (str (read-line))]
                  (do (when-not (= command-str "")
                        (let [command-vector (string/split command-str #" ")]
                          (execute current-player (first command-vector) (rest command-vector))))
                  (if (win? current-player)
                    (do
                      (swap! (:continue (get @players current-player)) not)
                      (display (:win-message (get-starting-room-by-player current-player))))
                    (when (continue? current-player)
                      (display-prompt)
                      (recur (str (read-line)))))))
             (display "Goodbye~!"))))]
    (create-server 8080 echo)))

(defn -main
  "A text only game to escape from a locked room."
  [& args]
  (if (= args true)
    (swap! is-windows not))
  (game-server))
