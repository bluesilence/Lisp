(ns xiami-crawler.core
  (:gen-class))

(require '[net.cgrand.enlive-html :as enlive])
(use '[clojure.string :only (lower-case)])
(import '(java.net URL MalformedURLException))
(import '(java.util.concurrent LinkedBlockingQueue))

(require '[xiami-crawler.config :as config])
(require '[xiami-crawler.logger :as logger])

(def url-queue (LinkedBlockingQueue.))
(def crawled-urls (atom #{}))
(def word-freqs (atom {}))
(def song-infos (atom []))

(def log (logger/multi-logger
            (logger/print-logger *out*)
            (logger/file-logger "logs/crawler.log")))

(def record
  (logger/file-logger (str "logs/songs.log")))

(defn- songs-from
  [html]
  (println "-------------------Songs-from--------------------")
  (println config/song-id-name-pattern)
  (log html)
  (when-let [songs (re-seq config/song-id-name-pattern html)]
    (println "-------------------After find--------------------")
    (doseq [song-info songs]
      (println "Got song info: " song-info)
      (println "Song name: " (peek song-info))
      (println "Song id: " (peek (pop song-info))))))

(defn- links-from
  [base-url html]
  (remove nil? (for [link (enlive/select html [:a])]
                 (when-let [href (-> link :attrs :href)]
                   (try
                     (when (re-find config/qualified-url-pattern href)
                       (URL. href))
                     ; ignore bad URLs
                     (catch MalformedURLException e))))))

(defn add-album-url [id]
  (.put url-queue (URL. (str config/album-url-path id)))
  (log url-queue))

(declare get-url)
(def agents (set (repeatedly 25 #(agent {::t #'get-url :queue url-queue}))))

(declare run process handle-results)

(defn ^::blocking get-url
  [{:keys [^LinkedBlockingQueue queue] :as state}]
  (println "------------------------get-url--------------------------")
  (let [url (.take queue)]
    (println "URL got:" url)
    (try
      (if (@crawled-urls url)
           state
        {:url url
         :content (slurp url)
         ::t #'process})
      (catch Exception e
        ;; skip any URL we failed to load
        state)
      (finally (run *agent*)))))

(defn process
  [{:keys [url content]}]
  (println "------------------------process--------------------------")
  (try
    (let [html (enlive/html-resource (java.io.StringReader. content))]
      {::t #'handle-results
       :url url
       :songs (songs-from html)
       })
    (finally (run *agent*))))

(defn ^::blocking handle-results
  [{:keys [url songs]}]
  (println "------------------------handle-results--------------------------")
  (try
    (swap! crawled-urls conj url)
    (doseq [song songs]
      (swap! song-infos conj song))
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
  (swap! word-freqs empty)
  (doseq [album-id config/album-ids]
    (.add url-queue (str config/album-url-path album-id)))
  (run)
  (Thread/sleep crawling-time)
  (pause)
  (Thread/sleep 10000)	;Wait till all agents terminate
  (println "Crawled url count: " (count @crawled-urls))
  (println "Url in queue: " (count url-queue))
  (println "Songs got: " (count @song-infos))
  (doseq [song @song-infos]
    (record (:id song))))

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
