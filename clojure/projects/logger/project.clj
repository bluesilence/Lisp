(defproject logger "0.1.0-SNAPSHOT"
  :description "Build a log system using composite high-order functions"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :main ^:skip-aot logger.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
