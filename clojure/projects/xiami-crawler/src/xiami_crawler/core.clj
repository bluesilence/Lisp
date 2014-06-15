(ns xiami-crawler.core
  (:gen-class))

(require '[net.cgrand.enlive-html :as enlive])
(use '[clojure.string :only (lower-case)])
(import '(java.net URL MalformedURLException))
(import '(java.util.concurrent LinkedBlockingQueue))

(require '[xiami-crawler.config :as config])
(require '[xiami-crawler.logger :as logger])

(defn parse-int
  ([s] (Integer/parseInt s))
  ([s default-int] (try
                     (Integer/parseInt s)
                     (catch java.lang.NumberFormatException ne default-int))))

(def url-queue (LinkedBlockingQueue.))
(def crawled-urls (atom #{}))
(def song-infos (atom #{}))
(def album-infos (atom #{}))
(def artist-infos (atom #{}))

(def log (logger/multi-logger
            (logger/print-logger *out*)
            (logger/file-logger-with-date "logs/crawler")))

(defn filter-nil [info]
  (filter (fn [x] (every? (comp not nil?) x)) info))

(def album-id (atom 0))

(defn get-next-album []
  (println "Get next album...")
  (swap! album-id inc)
  (println "Next Album id: " @album-id)
  @album-id)

(defn record-song [song-info]
  (let [[id name hot album-id] song-info]
  ((logger/file-logger-with-date "logs/songs") (str id ";" name ";" hot ";" album-id))))

(defn record-songs []
  (println "Songs got: " (count @song-infos))
  (println @song-infos)
  (let [ordered-songs (reverse (sort-by (comp parse-int #(nth % 2)) (filter-nil @song-infos)))]
    (doseq [song ordered-songs]
      (record-song song))))

(defn record-album [album-info]
  (let [[id name genre artist-id] album-info]
  ((logger/file-logger-with-date "logs/albums") (str id ";" name ";" genre ";" artist-id))))

(defn record-albums []
  (println "Albums got: " (count @album-infos))
  (println @album-infos)
  (doseq [album (filter-nil @album-infos)]
    (record-album album)))

(defn record-artist [artist-info]
  (let [[id name] artist-info]
  ((logger/file-logger-with-date "logs/artists") (str id ";" name))))

(defn record-artists []
  (println "Artists got: " (count @artist-infos))
  (println @artist-infos)
  (doseq [artist (filter-nil @artist-infos)]
    (record-artist artist)))

(defn record-album-page [album-id message]
  ((logger/file-logger (str "logs/album_" album-id ".log")) message))

(defn get-album-log [id]
  (slurp (str "logs/album_" id ".log")))

(defn- songs-from
  [html album-id]
  (println "-------------------Songs-from--------------------")
  (record-album-page album-id html) ; Force evaluate lazy-seq
  (when-let [page (get-album-log album-id)]
    (let [songs (re-seq config/song-id-name-pattern page)
          hotness (re-seq config/song-hot-pattern page)
          album-name (re-seq config/album-name-pattern page)
          genre (re-seq config/album-genre-pattern page)
          artist (re-seq config/album-artist-pattern page)]
      (let [song-id (map (comp peek pop) songs)
            song-name (map peek songs)
            song-hot (map peek hotness)
            album-name (peek (first album-name))
            album-genre (peek (first genre))
            artist-id (peek (pop (first artist)))
            artist-name (peek (first artist))]
        (let [song-list (map conj (map vector song-id song-name song-hot) (repeat (count song-id) album-id))
              album-info [album-id album-name album-genre artist-id]
              artist-info [artist-id artist-name]]
          {:song-list song-list
           :album-info album-info
           :artist-info artist-info})))))

(defn- links-from
  [base-url html]
  (remove nil? (for [link (enlive/select html [:a])]
                 (when-let [href (-> link :attrs :href)]
                   (try
                     (when (re-find config/qualified-url-pattern href)
                       (URL. href))
                     ; ignore bad URLs
                     (catch MalformedURLException e))))))

(declare get-url)
(def agents (set (repeatedly 25 #(agent {::t #'get-url :queue url-queue}))))

(declare run process handle-results)

(defn ^::blocking get-url
  [{:keys [^LinkedBlockingQueue queue] :as state}]
  (println "------------------------get-url--------------------------")
  (let [url (URL. (str config/album-url-path (get-next-album)))]
    (println "URL got:" url)
    (try
      (Thread/sleep config/sleep-interval)
      (if (@crawled-urls url)
           state
        {:url url
         :content (slurp url)
         :album @album-id
         ::t #'process})
      (catch Exception e
        ;; skip any URL we failed to load
        state)
      (finally (run *agent*)))))

(defn process
  [{:keys [url content album]}]
  (println "------------------------process--------------------------")
  (try
    (let [html (enlive/html-resource (java.io.StringReader. content))]
      {::t #'handle-results
       :url url
       :songs (songs-from html album)
       })
    (finally (run *agent*))))

(defn ^::blocking handle-results
  [{:keys [url songs]}]
  (println "------------------------handle-results--------------------------")
  (try
    (swap! crawled-urls conj url)
    (println "------------Songs------------")
    (let [songs (:song-list songs)]
      (doseq [song songs]
        (println song)
        (swap! song-infos conj song)))
    (println "------------Album------------")
    (println (:album-info songs))
    (swap! album-infos conj (:album-info songs))
    (println "------------Artist------------")
    (println (:artist-info songs))
    (swap! artist-infos conj (:artist-info songs))
    {::t #'get-url :queue url-queue}
    (finally (run *agent*))))

(defn paused? [agent] (::paused (meta agent)))

(defn run
  ([] (doseq [a agents] (run a)))
  ([a]
   (when (agents a)
     (send a (fn [{transition ::t :as state}]
               (when-not (paused? *agent*)
                 (let [dispatch-fn (if (-> transition meta ::blocking)
                                     send-off
                                     send)]
                   (dispatch-fn *agent* transition)))
               state)))))

(defn pause
  ([] (doseq [a agents] (pause a)))
  ([a] (alter-meta! a assoc ::paused true)))

(defn restart
  ([] (doseq [a agents] (restart a)))
  ([a]
   (alter-meta! a dissoc ::paused)
   (run a)))

(defn test-crawler
  "Resets all state associated with the crawler, adds the given URL to the url-queue, and runs the crawler for 60 seconds, returning a vector containing the number of URLs crawled, and the number of URLs accumulated through crawling that have yet to be visited."
  [agent-count starting-url crawling-time]
  (def agents (set (repeatedly agent-count
                               #(agent {::t #'get-url :queue url-queue}))))
  (.clear url-queue)
  (swap! crawled-urls empty)
  (run)
  (Thread/sleep crawling-time)
  (pause)
  (Thread/sleep 10000)	;Wait till all agents terminate
  (println "Crawled url count: " (count @crawled-urls))
  (record-songs)
  (record-albums)
  (record-artists))

(defn -main
  [& args]
  (println "Hello, Crawler of agents!")
  (println "Input number of agents:")
  (when-let [number (read)]
    (let [url config/starting-url]
      (println "Starting url:" url)
      (println "Input time to crawl(unit: s): ")
      (when-let [crawling-time (read)]
        (test-crawler number url (* 1000 crawling-time))))))
