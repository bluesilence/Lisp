(ns xiami-crawler.util
  (:gen-class))

(defn read-proxies []
  (try
    (let [line (slurp "src/xiami_crawler/proxy_list.txt")
          proxy-port (re-seq (re-pattern "(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)") line)]
        (map rest proxy-port))
    (catch java.io.FileNotFoundException fe nil)))

(def proxies (read-proxies))

(defn pick-proxy-randomly []
  (rand-nth proxies))
