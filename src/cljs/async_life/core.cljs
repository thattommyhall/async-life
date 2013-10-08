(ns async-life.core
  (:use [domina :only [by-id set-text!]])
  (:require [cljs.core.async :as async
             :refer [<! >! >!! chan put! timeout]]
            )
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def line-colour "#cdcdcd")

(def padding 0)

(def alive "#666")
(def dead "#eee")

(def context (atom nil))
(def width (atom nil))
(def height (atom nil))
(def cell-size (atom 50))
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

(defn cell [x y]
  (let [new-neighbor (chan)
        input (chan 20)
        neighbors #{}
        initial-state (rand-nth [:dead :alive])
        ]
    (go (loop [neighbor-count 0
               state initial-state
               neighbors neighbors]
          (let [[val chan] (alts! [new-neighbor input])]
            (cond (= chan new-neighbor)
                  (do (if (= state :alive)
                        (>! val 1))
                      (recur neighbor-count state (conj neighbors val)))

                  :else
                  (let [neighbor-count (+ val neighbor-count)
                        new-state (if (or (= neighbor-count 3)
                                          (and (= neighbor-count 2) (= state :alive)))
                                    :alive
                                    :dead)
                        draw? (not= new-state state)
                        delta (if (= new-state :alive) 1 (- 1))
                        colour (if (= new-state :alive) alive dead)
                        ]
                    (log (str x " " y " " neighbor-count))
                    (if draw?
                      (do (doseq [n neighbors]
                            (>! n delta))
                          (fill_sq x y colour)))
                    (recur neighbor-count new-state neighbors))))))
    [input new-neighbor]))

(defn draw-loop []
  (let [[input1 new-neighbor1] (cell 3 3)
        [input2 new-neighbor2] (cell 4 4)
        ]
    (go (doseq [n [1 1 1 1 -1 -1 -1 -1]]
          (<! (timeout 500))
          ;; (log n)
          (>! input1 n)
          (>! input2 n)))

    ;; (go (loop []
    ;;       (<! out)
    ;;       (recur)))
    ;; (go (loop []
    ;;       (<! out2))
    ;;       (recur))

    )
    )


  ;; (defn draw-loop []
  ;;   (let [queue (chan)]
  ;;     (go
  ;;      (loop []
  ;;          (<! (timeout 1))
  ;;          (>! queue [(Math/floor (rand @width)) (Math/floor (rand @height))])
  ;;          (recur)))


  ;;     (go (loop []
  ;;           (draw! (<! queue))
  ;;           (recur)))))


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

    (draw-loop)
    )
