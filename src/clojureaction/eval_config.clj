#!/usr/bin/env bb
(ns clojureaction.eval-config
  (:require [clojure.pprint :as pp]))

(def output-name "out")

(defn gen [code]
  (let [id (str (random-uuid))
        conf (-> code read-string eval pp/pprint with-out-str)]
    (println "Setting 'out' to:")
    (print conf)
    (flush)
    (str output-name "<<" id "\n"
         conf
         "\n" id "\n")))

(comment
  (gen "{:commands
       (cons \"clj-kondo\"
       (for [java [11 21]
       clojure [\"1.11\" \"1.12\"]]
       {:setup-java {:java-version java}
       :name (format \"Test (Clojure %s, Java %s)\" clojure java)
       :command (format \"lein with-profile +%s test\" clojure)}))
       :download-deps \"lein with-profile +test,+clj-kondo deps\"
       :target-duration [5 :minutes]
       :java 21}")
  )

(comment
  (gen "{}")
  )

(defn -main [code]
  (spit (System/getenv "GITHUB_OUTPUT")
        (gen code)
        :append true))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
