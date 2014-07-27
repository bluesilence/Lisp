(defproject room-escape "0.1.0-SNAPSHOT"
  :description "ONLINE now! Play it right now by:
                telnet avalonOfSunny.cloudapp.net 8080"
  :url "telnet avalonOfSunny.cloudapp.net 8080"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [server-socket "1.0.0"]]
  :main room-escape.core
  :aot [room-escape.common room-escape.script room-escape.util]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
