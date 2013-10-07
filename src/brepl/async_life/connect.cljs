(ns enlil.repl
  (:require [clojure.browser.repl :as repl]))

(defn ^:export start []
  (repl/connect "http://localhost:9000/repl"))

