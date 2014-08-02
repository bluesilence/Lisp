(ns test-macro.core
  (:gen-class))

(defn read-files [path]
  (seq (. (clojure.java.io/file path) listFiles)))

(def rooms-path "./rooms/")

(defn file? [file]
  (. file isFile))

(defn get-absolute-path [file]
  (. file getAbsolutePath))

(defmacro merge-rooms [rooms]
  `(vec ~rooms))

(defn eval-room [room]
  (let [evaled-room (transient {})]
    (doseq [key (keys room)]
      (println key " -> " (eval (room key)))
      (assoc! evaled-room key (eval (room key))))
    (persistent! evaled-room)))

(defn consume-rooms [path]
  (let [room-files (filter file? (read-files path))
        absolute-paths (map get-absolute-path room-files)
        room-contents (map slurp absolute-paths)]
    (let [raw-rooms (map read-string (merge-rooms room-contents))]
      (vec (map eval-room raw-rooms)))))
          ;(assoc room property (read-string (room property))))))))
    ;(eval (read-string (str "(vec " room-contents ")")))))
      ;(eval (read-string "(fn [] (into {} {:name \"test\"}))")))
;(def f1 (eval (read-string "(fn [x y] (* x y))")))

(def rooms (consume-rooms rooms-path))

(defn -main
  [& args]
  rooms)
