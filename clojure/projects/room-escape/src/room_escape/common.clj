(ns room-escape.common
  (:gen-class))

(defn parse-int
  ([s] (Integer/parseInt s))
  ([s default-int] (try
                     (Integer/parseInt s)
                     (catch java.lang.NumberFormatException ne default-int))))

(defn locate-by-id [id objects]
  (first (filter (comp #(= % id) #(:id %)) objects)))

(defn locate-by-name [name objects]
  (first (filter (comp #(= % name) #(:name %)) objects)))

(defn locate-by-category [category-id objects]
  (filter (comp #(= % category-id) #(:category %)) objects))
