(ns clojureaction.action
  (:require [clj-yaml.core :as yaml]))

(def template
  {:name "clojureaction"
   :description "GitHub Action to build Clojure projects"
   :author "Ambrose Bonnaire-Sergeant"
   :branding {:color "blue"
              :icon "type"}
   :inputs {:setup-java {:required false
                         :type "string"}}
   :runs {:using "composite"
          :steps {:uses "DeLaGuardo/setup-clojure@13.0"
                  :with {:bb "1.12.195"}}}})

(defn gen []
  (spit "action.yml" (yaml/generate-string template {:dumper-options {:flow-style :block}})))

(defn -main [] (gen))
