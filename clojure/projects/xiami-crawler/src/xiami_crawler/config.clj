(ns xiami-crawler.config
  (:gen-class))

(def starting-url "http://www.xiami.com/song/1770642543")

; Only crawl xiami's pages
(def qualified-url-pattern 
  (re-pattern "^http:\\/\\/\\w+\\.xiami\\.com\\.*"))

; Parse song url
(def song-url-pattern
  (re-pattern "^http:\\/\\/www\\.xiami\\.com\\/song\\/(\\d+)"))

; Parse song id
(def song-id-pattern
  (re-pattern "^var\\ssong_id = \\'(\\d+)\\'"))

; Parse song name from url
(def song-name-pattern
  (re-pattern "<a ((href=\"http:\\/\\/www\\.xiami\\.com\\/song\\/\\d+\"\\s+title=\".*\")|(title=\".*\"\\s+href=\"http:\\/\\/www\\.xiami\\.com\\/song\\/\\d+\"))\\s*>([a-zA-Z0-9 ]+)<\\/a>"))
