(ns xiami-crawler.config
  (:gen-class))

(def starting-url "http://www.xiami.com/album/1")

; Only crawl xiami's pages
(def qualified-url-pattern 
  (re-pattern "^http:\\/\\/\\w+\\.xiami\\.com\\.*"))

; Album url root path
(def album-url-path "http://www.xiami.com/album/")

; Parse album url
(def album-url-pattern
  (re-pattern "^http:\\/\\/www\\.xiami\\.com\\/album\\/(\\d+)"))

; Album ids to be crawled
(def album-ids (range 1 (inc 100)))

; Parse song url
(def song-url-pattern
  (re-pattern "^http:\\/\\/www\\.xiami\\.com\\/song\\/(\\d+)"))

; Parse song id and name
(def song-id-name-pattern
  (re-pattern ":href \\/song\\/(\\d+)}, :content \\(([^\\)}]+)\\)"))

; Parse song id
(def song-id-pattern
  (re-pattern "^var\\ssong_id = \\'(\\d+)\\'"))

; Parse song name from url
(def song-name-pattern
  (re-pattern "<a ((href=\"http:\\/\\/www\\.xiami\\.com\\/song\\/\\d+\"\\s+title=\".*\")|(title=\".*\"\\s+href=\"http:\\/\\/www\\.xiami\\.com\\/song\\/\\d+\"))\\s*>([a-zA-Z0-9 ]+)<\\/a>"))
