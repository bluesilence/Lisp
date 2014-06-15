(ns xiami-crawler.config
  (:gen-class))

; Sleep 1s between every slurp to prevent being banned
(def sleep-interval 5000)

; Start from album 1
(def starting-url "http://www.xiami.com/album/1")

; Only crawl xiami's pages
(def qualified-url-pattern 
  (re-pattern "^http:\\/\\/\\w+\\.xiami\\.com\\.*"))

; Album url root path
(def album-url-path "http://www.xiami.com/album/")

; Parse album url
(def album-url-pattern
  (re-pattern "^http:\\/\\/www\\.xiami\\.com\\/album\\/(\\d+)"))

; Parse album name
(def album-name-pattern
  (re-pattern "《(.+)》 专辑地址：http:\\/\\/www\\.xiami\\.com\\/album\\/\\d+"))

; Test text of album name
(def album-name-test
  "分享 Alex 的专辑《我 那一场恋爱》 专辑地址：http://www.xiami.com/album/1")

; Parse album genre
(def album-genre-pattern
  (re-pattern ":content \\(专辑类别：\\)}\\s+.+:content\\s+\\(([^\\)}]+)\\)}"))

; Test text of album genre
(def album-genre-test
  "{:tag :td, :attrs {:valign top, :class item}, :content (专辑类别：)}  
   {:tag :td, :attrs {:valign top}, :content (录音室专辑)}")

; Parse album artist
(def album-artist-pattern
  (re-pattern ":href \\/artist\\/(\\d+)}, :content \\(([^\\)}]+)\\)"))

; Test text of album artist
(def album-artist-test
  ":content ({:tag :a, :attrs {:title Alex, :href /artist/1}, :content (Alex)})})")

; Parse song url
(def song-url-pattern
  (re-pattern "^http:\\/\\/www\\.xiami\\.com\\/song\\/(\\d+)"))

; Parse song id and name
(def song-id-name-pattern
  (re-pattern ":href \\/song\\/(\\d+)}, :content \\(([^\\)}]+)\\)"))

; Parse song hot
(def song-hot-pattern
  (re-pattern ":class\\s+song_hot\\s*},\\s+:content\\s+\\((\\d+)\\)}"))

; Parse song id
(def song-id-pattern
  (re-pattern "^var\\ssong_id = \\'(\\d+)\\'"))

; Parse song name from url
(def song-name-pattern
  (re-pattern "<a ((href=\"http:\\/\\/www\\.xiami\\.com\\/song\\/\\d+\"\\s+title=\".*\")|(title=\".*\"\\s+href=\"http:\\/\\/www\\.xiami\\.com\\/song\\/\\d+\"))\\s*>([a-zA-Z0-9 ]+)<\\/a>"))
