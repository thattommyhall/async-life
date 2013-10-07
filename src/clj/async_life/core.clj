(ns async-life.core
  (:use [compojure.core]
        [hiccup.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [header]]
            [clojure.data.json :as json]
            [clojure.string :as string]))

(defroutes site-routes
  (GET "/" []
       (html [:head {:title "Async Life"}
              [:link {:rel "stylesheet" :href "css/style.css"}]
              [:script {:src "js/dev.js"}]]
             [:body {:onload "async_life.core.init();"}
              [:div
               [:canvas#world {:width 400 :height 400} ]]]))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app (handler/site site-routes))
