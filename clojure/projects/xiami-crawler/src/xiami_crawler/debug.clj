(ns xiami-crawler.debug
  (:gen-class))

(require '[clj-http.client :as client])
(require '[net.cgrand.enlive-html :as enlive])
(use '[clojure.string :only (lower-case)])

(require '[xiami-crawler.logger :as logger])

(def log-slurp (logger/file-logger "logs/slurp-page.log"))
(def log-get (logger/file-logger "logs/get-page.log"))

(defn slurp-single-page [url]
  (let [html (enlive/html-resource (java.io.StringReader. (slurp url)))]
    (log-slurp html)
    (println html)))

(defn test-slurp []
  (slurp-single-page "http://www.xiami.com/album/1"))

(defn get-single-page [url]
  (let [html (enlive/html-resource (java.io.StringReader. (:body (client/get url))))]
    (log-get html)
    (println html)))

(defn test-get []
  (get-single-page "http://www.xiami.com/album/1"))

(defn match [html pattern]
  (re-seq pattern html))
