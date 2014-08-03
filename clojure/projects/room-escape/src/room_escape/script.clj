(ns room-escape.script
  (:gen-class))

(use '[clojure.string :as string :only [join] :exclude [reverse]])
(use '[room-escape.common :only [parse-int locate-by-id locate-by-name set-visible visible? set-win display enclose highlight]])

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
      (vec (sort-by :name (map eval-room raw-rooms))))))

(def starting-rooms (consume-rooms rooms-path))
