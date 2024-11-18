#!/usr/bin/env bb
(ns clojureaction.eval-config
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [cheshire.core :as json]
            [babashka.fs :as fs]
            [clj-commons.digest :as digest]))

(def output-name "out")

(def cache-paths
  {:bb ["~/.m2/repository" "~/.deps.clj" "~/.gitlibs"]
   :cli ["~/.m2/repository" "~/.gitlibs"]
   :lein ["~/.m2/repository"]})

(defn expand [conf]
  (cond-> conf
    (:download-deps conf)
    (update :download-deps (fn [{:keys [command cache-tools cache-paths] :as m}]
                             (assert ())
                             (let [cache-paths (into (set cache-paths)
                                                     (mapcat #(cache-paths (keyword %)))
                                                     cache-tools)
                                   files (into [] (mapcat #(fs/glob "." (fs/expand-home %) {:follow-links true}))
                                               cache-paths)]
                               (assoc m
                                      :cache-paths (sort cache-paths)
                                      :key (str "clojure-deps-" (digest/sha-256 (apply str (map digest/sha-256 cache-paths))))
                                      ))))))

(defn gen [code]
  (let [id (str (random-uuid))
        conf (-> code read-string eval expand (json/encode {:pretty true}))]
    (println (format "Setting '%s' to:" output-name))
    (println conf)
    (str output-name "<<" id "\n"
         conf "\n" id "\n")))

(comment
  (gen (pr-str
         '{:commands
           (into ["clj-kondo"]
                 (for [java [11 21]
                       clojure ["1.11" "1.12"]]
                   {:setup-java {:java-version java}
                    :name (format "Test (Clojure %s, Java %s)" clojure java)
                    :command (format "lein with-profile +%s test" clojure)}))
           :download-deps {:command "lein with-profile +test,+clj-kondo deps"
                           :cache-tools [:bb :cli :lein]}
           :target-duration [5 :minutes]
           :java 21}))
  (gen (pr-str {:download-deps {:command "./script/deps"
                                :cache-paths ["deps.edn"]}}))
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
