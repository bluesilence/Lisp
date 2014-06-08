(ns xiami-crawler.config
  (:gen-class))

(def starting-url "http://www.xiami.com")
(def qualified-url-pattern 
  (re-pattern "^http:\\/\\/\\w+\\.xiami\\.com\\.*"))
