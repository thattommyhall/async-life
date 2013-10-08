(ns async-life.core
  (:use [compojure.core]
        [hiccup.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [header]]
            [clojure.data.json :as json]
            [clojure.string :as string]))

(def fork-me "<a href=\"https://github.com/thattommyhall/async-life/blob/master/src/cljs/async_life/core.cljs\" target=\"_gh\"\"><img style=\"position: absolute; top: 0; right: 0; border: 0; z-index: 3;\" src=\"https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png\" alt=\"Fork me on GitHub\" ></a>")

(defn page-for [env]
  (html [:head {:title "Async Life"}
         [:link {:rel "stylesheet" :href "css/style.css"}]
         ]

        [:body {:onload "async_life.core.init();"}
         fork-me
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
