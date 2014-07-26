(ns room-escape.common
  (:gen-class))

(use '[clojure.string :as string])

(defn display [message & args]
  (println (str message (string/join args))))

(defn enclose [object-name]
  (str "["object-name"]"))

(defn parse-int
  ([s] (Integer/parseInt s))
  ([s default-int] (try
                     (Integer/parseInt s)
                     (catch java.lang.NumberFormatException ne default-int))))

(defn locate-by-id [id objects]
  (first (filter (comp #(= % id) #(:id %)) objects)))

(defn get-name [id objects]
  (:name (locate-by-id id objects)))

(defn locate-by-name [name objects]
  (first (filter (comp #(= % name) #(:name %)) objects)))

(defn locate-by-category [category-id objects]
  (filter (comp #(= % category-id) #(:category %)) objects))

(def visible (atom #{}))

(defn visible? [id]
  (contains? @visible id))

(defn set-visible [id]
  (swap! visible conj id))

(defn set-invisible [id]
  (swap! visible disj id))

(defn toggle-visible [id]
  (if (visible? id)
    (set-invisible id)
    (set-visible id)))
