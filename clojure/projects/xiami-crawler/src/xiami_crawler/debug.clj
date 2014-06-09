(ns xiami-crawler.debug
  (:gen-class))

(require '[net.cgrand.enlive-html :as enlive])
(use '[clojure.string :only (lower-case)])

(require '[xiami-crawler.logger :as logger])

(def log->file (logger/file-logger "logs/page.log"))

(defn slurp-single-page [url]
  (let [html (enlive/html-resource (java.io.StringReader. (slurp url)))]
    (log->file html)))
