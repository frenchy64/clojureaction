#!/usr/bin/env bb
(ns clojureaction.eval-config
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [cheshire.core :as json]
            [babashka.fs :as fs]
            [clj-commons.digest :as digest]))

(def output-name "out")

(def default-tool-cache-paths
  {:bb ["~/.m2/repository" "~/.deps.clj" "~/.gitlibs"]
   :cli ["~/.m2/repository" "~/.gitlibs"]
   :lein ["~/.m2/repository"]})

(defn expand [conf]
  (cond-> conf
    (:download-deps conf)
    (update :download-deps (fn [{:keys [command cache-tool cache-path] :as m}]
                             (prn "cache-tool" cache-tool)
                             (let [cache-path (into (if (string? cache-path)
                                                      #{cache-path}
                                                      (set cache-path))
                                                    (mapcat #(default-tool-cache-paths (keyword %)))
                                                    (cond-> cache-tool
                                                      ((some-fn string? ident?) cache-tool) vector))
                                   files (into [] (mapcat #(fs/glob "." (fs/expand-home %) {:follow-links true}))
                                               cache-path)]
                               (assoc m
                                      :cache-path (sort cache-path)
                                      :key (str "clojure-deps-" (digest/sha-256 (apply str (map digest/sha-256 cache-path))))
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
                           :cache-tool [:bb :cli :lein]}
           :target-duration [5 :minutes]
           :java 21}))
  (gen (pr-str {:download-deps {:command "./script/deps"
                                :cache-path ["deps.edn"]}}))
  (gen (pr-str {:download-deps {:command "./script/deps"
                                :cache-tool :cli}}))
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
