(ns xiami-crawler.logger
  (:gen-class))

(defn print-logger
  [writer]
  #(binding [*out* writer]
     (println %)))

(defn file-logger
  [file]
  #(with-open [f (clojure.java.io/writer file :append false)]
     ((print-logger f) %)))

(def today (format "%1$tY-%1$tm-%1$te"(java.util.Date.)))

(defn file-logger-with-date
  [file]
  (let [file-name (str file "_" today ".log")]
    #(with-open [f (clojure.java.io/writer file-name :append true)]
       ((print-logger f) %))))

(defn multi-logger
  [& logger-fns]
  #(doseq [f logger-fns]
     (f %)))

(defn timestamped-logger
  [logger]
  #(logger (format "[%1$tY-%1$tm-%1$te %1$tH:%1$tM:%1$tS] %2$s"(java.util.Date.) %)))

(def writer (java.io.StringWriter.))

(def retained-logger (print-logger writer))

(defn -main
  "A few examples on the usage of the loggers."
  [& args]
  (println "---------Welcome to use my customized loggers---------")
  (println "1)Test print-logger:")
  (def *out*-logger (print-logger *out*))
  (*out*-logger "Hello Sunny!")

  (println "2)Test retained-logger:")
  (def writer (java.io.StringWriter.))
  (def retained-logger (print-logger writer))
  (retained-logger "Hello again, Sunny!")
  ;Execute in REPL: (str writer)

  (println "3)Test file-logger:")
  (def log->file (file-logger "test.log"))
  (log->file "See you again, Sunny!")

  (println "4)Test multi-logger:")
  (def log (multi-logger
              (print-logger *out*)
              (file-logger "test.log")))
  (log "You are so hard-working, Sunny.")

  (println "5)Test timestamped-logger:")
  (def log-timestamped (timestamped-logger
                         (multi-logger
                           (print-logger *out*)
                           (file-logger "test.log"))))
  (log-timestamped "Well done! Have a nice sleep now.")

  (println "---------Goodbye----------")
)
