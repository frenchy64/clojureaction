#!/usr/bin/env bb
(ns clojureaction.download-deps
  (:require [babashka.process :as proc]))

(defn -main [cmd]
  (prn cmd)
  (proc/shell cmd))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
