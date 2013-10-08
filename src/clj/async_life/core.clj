(ns async-life.core
  (:use [compojure.core]
        [hiccup.core])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :refer [header]]
            [clojure.data.json :as json]
            [clojure.string :as string]))

(def fork-me "<a href=\"https://github.com/thattommyhall/async-life/blob/master/src/cljs/async_life/core.cljs\" target=\"_gh\"\"><img style=\"position: absolute; top: 0; right: 0; border: 0; z-index: 3;\" src=\"https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png\" alt=\"Fork me on GitHub\" ></a>")

(def tweet-this "<a href=\"https://twitter.com/share\" style=\"width: 136px; height: 28px;z-index: 100;position: absolute;left: 50%;\" class=\"twitter-share-button\" data-text=\"Async Game Of Live in Clojurescript core.async, no datastructures, everything is a channel\" data-via=\"thattommyhall\" data-size=\"large\">Tweet</a>
<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');</script>")

(def ga "<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-871424-4', 'thattommyhall.com');
  ga('send', 'pageview');

</script>")

(defn page-for [env]
  (html [:head {:title "Async Life"}
         [:link {:rel "stylesheet" :href "css/style.css"}]
         ga
         ]

        [:body {:onload "async_life.core.init();"}
         ;; tweet-this
         fork-me
         [:div

          [:canvas#world {:width 400 :height 400} ]
          [:script {:src (str "js/" env ".js")}]]
         ]))


(defroutes site-routes
  (GET "/" []
       (page-for "prod"))
  (GET "/dev" []
       (page-for "dev"))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app (handler/site site-routes))
