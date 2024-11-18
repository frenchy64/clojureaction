#!/usr/bin/env bb
(ns clojureaction.action
  (:require [clj-yaml.core :as yaml]
            [clojure.pprint :as pp]
            [clojure.string :as str]))

(def this-repo "frenchy64/clojureaction")

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
                                                     {:test {:command "lein test"}
                                                      :deps "lein deps"
                                                      :lint "clj-kondo"
                                                      :target-duration [5 :minutes]
                                                      :java 21})))
                                       ""
                                       "# Supported keys:"
                                       "- :test  A command to run Clojure tests"
                                       "  Example: \"lein test\""
                                       "  Default: none"
                                       "- :deps  A command to download all Clojure dependencies."
                                       "  Example: \"lein deps\""
                                       "  Default: none"
                                       "- :lint  A command to lint Clojure sources."
                                       "  Example: \"clj-kondo\""
                                       "  Default: none"
                                       "- :setup-clojure"
                                       "- :java"
                                       "- :target-duration  Target duration for entire build."
                                       "  Example: [5 :minutes]"
                                       ])}}])

(def template
  {:name "clojureaction"
   :description "GitHub Action to build Clojure projects"
   :author "Ambrose Bonnaire-Sergeant"
   :branding {:color "blue"
              :icon "type"}
   :inputs inputs
   :runs {:using "composite"
          :steps [{:uses "DeLaGuardo/setup-clojure@13.0"
                   :with {:bb "1.12.195"}}]}})

(defn gen []
  (spit "action.yml" (yaml/generate-string template {:dumper-options {:flow-style :block}})))

(defn -main [] (gen))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
