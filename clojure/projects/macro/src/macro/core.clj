(ns macro.core
  (:gen-class))

(require '(clojure [string :as string]
                   [walk :as walk]))

(defmacro reverse-it
  [form]
  (walk/postwalk #(if (symbol? %)
                    (symbol (string/reverse (name %)))
                    %)
                 form))

(defn -main
  "Clojure Programming Ch 5: Macro"
  [& args]
  (println "Hello, Macro!")
  (println "Reverse it:")
  (reverse-it
    (qesod [gra (egnar 5)]
           (nltnirp (cni gra))))
  (println "Expand-1:")
  (println (macroexpand-1 '(reverse-it
                    (qesod [gra (egnar 5)]
                      (nltnirp (cni gra))))))
  (println "Expand:")
  (println (macroexpand '(reverse-it
                  (qesod [gra (egnar 5)]
                    (nltnirp (cni gra))))))
  (println "Expand-all:")
  (walk/macroexpand-all '(reverse-it
                           (qesod [gra (egnar 5)]
                             (nltnirp (cni gra))))))
