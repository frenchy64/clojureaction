#!/usr/bin/env bb
(ns clojureaction.action
  (:require [clj-yaml.core :as yaml]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.java.shell :as sh]))

(spit "current-sha" (:out (sh/sh "git" "rev-parse" "--verify" "HEAD")))

(def this-repo "frenchy64/clojureaction")
(def this-sha (slurp "current-sha"))
(def setup-clojure "DeLaGuardo/setup-clojure@13.0")
(def actions-checkout "actions/checkout@v4")
(def bb-version "1.12.195")

(defn indent [n s]
  (->> s
       str/split-lines
       (mapv #(str (apply str (repeat n " ")) %))
       (str/join "\n")))

(def inputs
  [{:edn {:description (str/join "\n" ["An edn map with the following keys:"
                                       ""
                                       "# YAML Example:"
                                       ""
                                       "  - uses: frenchy64/clojureaction@release"
                                       "    with:"
                                       "      edn: |-"
                                       (indent 8 (with-out-str
                                                   (pp/pprint
                                                     {:commands
                                                      '(cons "clj-kondo"
                                                             (for [java [11 21]
                                                                   clojure ["1.11" "1.12"]]
                                                               {:setup-java {:java-version java}
                                                                :name (format "Test (Clojure %s, Java %s)" clojure java)
                                                                :command (format "lein with-profile +%s test" clojure)}))
                                                      :download-deps "lein with-profile +test,+clj-kondo deps"
                                                      :target-duration [5 :minutes]
                                                      :java 21})))
                                       ""
                                       "# Supported keys:"
                                       "- :test  A command to run Clojure tests"
                                       "  Example: \"lein test\""
                                       "  Default: none"
                                       "- :download-deps  A command to download all Clojure dependencies."
                                       "  Example: \"lein deps\""
                                       "  Default: none"
                                       "- :lint  A command to lint Clojure sources."
                                       "  Example: \"clj-kondo\""
                                       "  Default: none"
                                       "- :setup-clojure"
                                       "- :java"
                                       "- :target-duration  Target duration for entire build."
                                       "  Example: [5 :minutes]"
                                       "- :ineffective-cache-detection  If true, fail the build if dependencies are still"
                                       "                                downloaded on a cache-hit."
                                       "  Default: true"])}}])

(def template
  {:name "clojureaction"
   :description "GitHub Action to build Clojure projects"
   :author "Ambrose Bonnaire-Sergeant"
   :on {:workflow_call ""}
   :inputs inputs
   ;:outputs {}
   :jobs [{:setup
           {:runs-on "ubuntu-latest"
            :steps [{:uses setup-clojure
                     :with {:bb bb-version}}
                    {:uses actions-checkout
                     :with {:repository this-repo
                            :ref this-sha}}]}}]})

(defn gen []
  (spit ".github/workflows/clojureaction.yml"
        (yaml/generate-string template {:dumper-options {:flow-style :block}})))

(defn -main [] (gen))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
