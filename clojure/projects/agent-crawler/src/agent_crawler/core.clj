(ns agent-crawler.core
  (:gen-class))

(require '[net.cgrand.enlive-html :as enlive])
(use '[clojure.string :only (lower-case)])
(import '(java.net URL MalformedURLException))
(import '(java.util.concurrent LinkedBlockingQueue))

(defn- links-from
  [base-url html]
  (remove nil? (for [link (enlive/select html [:a])]
                 (when-let [href (-> link :attrs :href)]
                   (try
                     (URL. href)
                     ; ignore bad URLs
                     (catch MalformedURLException e))))))

(defn- words-from
  [html]
  (let [chunks (-> html
                 (enlive/at [:script] nil)
                             (enlive/select [:body enlive/text-node]))]
    (->> chunks
      (mapcat (partial re-seq #"\w+"))
      (remove (partial re-matches #"\d+"))
      (map lower-case))))

(def url-queue (LinkedBlockingQueue.))
(def crawled-urls (atom #{}))
(def word-freqs (atom {}))

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
      (println "links: " (links-from url html))
      {::t #'handle-results
       :url url
       :links (links-from url html)
       :words (reduce (fn [m word]
                        (update-in m [word] (fnil inc 0)))
                      {}
                      (words-from html))})
    (finally (run *agent*))))

(defn ^::blocking handle-results
  [{:keys [url links words]}]
  (println "------------------------handle-results--------------------------")
  (println "words:" words)
  (try
    (swap! crawled-urls conj url)
    (doseq [url links]
      (.put url-queue url))
    (swap! word-freqs (partial merge-with +) words)

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
  [agent-count starting-url]
  (def agents (set (repeatedly agent-count
                               #(agent {::t #'get-url :queue url-queue}))))
  (.clear url-queue)
  (swap! crawled-urls empty)
  (swap! word-freqs empty)
  (.add url-queue starting-url)
  (run)
  (Thread/sleep 300000)
  (pause)
  (println "Crawled url count: " (count @crawled-urls))
  (println "Url in queue: " (count url-queue))
  (->> (sort-by val @word-freqs)
    reverse
    (take 10)))

(defn -main
  [& args]
  (println "Hello, Crawler of agents!")
  (println "Input number of agents:")
  (when-let [number (read)]
    (println "Input starting url: ")
    (when-let [url 
               (str (when-let [rawUrl (str (read))]
                (if (> 0 (.indexOf rawUrl "http://"))
                  (str "http://" rawUrl)
                  rawUrl)))]
      (println "Starting url:" url)
      (test-crawler number url))))
