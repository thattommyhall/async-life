(ns async-life.core
  (:use [compojure.core]
        [hiccup.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [header]]
            [clojure.data.json :as json]
            [clojure.string :as string]))

(defn page-for [env]
  (html [:head {:title "Async Life"}
         [:link {:rel "stylesheet" :href "css/style.css"}]
         ]

        [:body {:onload "async_life.core.init();"}
         [:div
          [:canvas#world {:width 400 :height 400} ]
          [:script {:src (str "js/" env ".js")}]]]))

(defroutes site-routes
  (GET "/" []
       (page-for "prod"))
  (GET "/dev" []
       (page-for "dev"))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app (handler/site site-routes))
