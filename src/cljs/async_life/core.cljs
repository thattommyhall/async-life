(ns async-life.core
  (:use [domina :only [by-id set-text!]])
  (:require [cljs.core.async :as async
             :refer [<! >! chan put! timeout]]
            )
  (:require-macros [cljs.core.async.macros :refer [go]]))


(def line-colour "#cdcdcd")

(def padding 0)

(def alive "#666")
(def dead "#eee")

(def context (atom nil))
(def width (atom nil))
(def height (atom nil))
(def cell-size (atom 10))
(def canvas (atom nil))

(defn fill_sq [x y colour]
  (let [c @context
        s @cell-size]
  (set! (.-fillStyle c) colour)
  (set! (.-strokeStyle c) line-colour)
  (.fillRect c
             (+ (* x s) padding)
             (+ (* y s) padding)
             s
             s)
  (.strokeRect c
               (+ (* x s) padding)
               (+ (* y s) padding)
               s
               s)))

(defn resized []
  (set! (.-width @canvas) (.-innerWidth js/window))
  (set! (.-height @canvas) (.-innerHeight js/window))
  (reset! width (/ (.-width @canvas) @cell-size))
  (reset! height (/ (.-height @canvas) @cell-size))
  )

(set! (.-onresize js/window) resized)

(defn log [msg]
  (.log js/console msg))

(defn draw! [[x y]]
  ;; (log (clj->js [x y]))
  (fill_sq x y (rand-nth [alive dead])))

(defn draw-loop []
  (let [queue (chan)]
    (go
     (loop []
         (<! (timeout 1))
         (>! queue [(Math/floor (rand @width)) (Math/floor (rand @height))])
         (recur)))


    (go (loop []
          (draw! (<! queue))
          (recur)))))


;; (defn glide []
;;   (let [draw (drawer)]
;;     (go (while true
;;           (<! (timeout 1000))
;;           (>! draw [50 50])))))


(defn ^:export init []
  (reset! canvas (by-id "world"))
  (reset! context (.getContext @canvas  "2d"))
  (resized)
  (doseq [y (range @height)
          x (range @width)]
    (fill_sq x y dead)
    )
  (draw! [1 1])
  (draw-loop)
  )
