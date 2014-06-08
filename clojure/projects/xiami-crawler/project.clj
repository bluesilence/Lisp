(defproject xiami-crawler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [enlive/enlive "1.0.0"]]
  :main xiami-crawler.core
  :aot [xiami-crawler.config xiami-crawler.logger]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
