(ns async-life.core
  (:use [domina :only [by-id set-text!]])
  (:require [cljs.core.async :as async
             :refer [<! >! >!! chan put! timeout alts!]]
            )
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def line-colour "#cdcdcd")
(def padding 0)
(def alive "#666")
(def dead "#eee")
(def width (atom nil))
(def height (atom nil))
(def cell-size 30)
(def canvas (.getElementById js/document "world"))
(def context (.getContext canvas "2d"))

(defn fill_sq [x y colour]
  (set! (.-fillStyle context) colour)
  (set! (.-strokeStyle context) line-colour)
  (.fillRect context
             (+ (* x cell-size) padding)
             (+ (* y cell-size) padding)
             cell-size
             cell-size)
  (.strokeRect context
               (+ (* x cell-size) padding)
               (+ (* y cell-size) padding)
               cell-size
               cell-size))

(defn drawer []
  (let [c (chan 1000)]
    (go (loop []
          (let [[x y colour] (<! c)]
            (<! (timeout 1))
            (fill_sq x y colour)
            (recur))))
    c))

(def draw (drawer))



(defn log [msg]
  (.log js/console msg))

(defn cell [[x y]]
  (let [new-neighbor (chan)
        input (chan 20)
        neighbors #{}
        initial-state (rand-nth [:dead :alive])
        ]
    (if (= initial-state :alive)
      (go (>! draw [x y alive]))
      )
    (go (loop [neighbor-count 0
               state initial-state
               neighbors neighbors]
          (let [[val chan] (alts! [new-neighbor input])]
            (cond (= chan new-neighbor)
                  (do (if (= state :alive)
                        (<! (timeout 300))
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
                    (if draw?
                      (do (doseq [n neighbors]
                            (<! (timeout 1))
                            (>! n delta))
                          (>! draw [x y colour])
                          ))
                    (recur neighbor-count new-state neighbors))))))
    [input new-neighbor]))

(defn neighbors [[x y] grid]
  (filter (comp not nil?)
          (for [dx [-1 0 1]
                dy (if (zero? dx)
                     [-1 1]
                     [-1 0 1])]
            (let [x' (+ dx x)
                  y' (+ dy y)]
              (get grid [x' y'])))))

(defn draw-loop []
  (let [[input1 new-neighbor1] (cell [3 3])
        [input2 new-neighbor2] (cell [4 4])
        n1 (chan 20)
        xys (for [x (range @width)
                  y (range @height)]
              [x y])
        cells (zipmap xys
                      (map cell xys))
        ]
    (doseq [[xy [input _]] cells]
      (go (doseq [[_ nn] (neighbors xy cells)]
            (<! (timeout 1))
            (>! nn input))))))

(defn resized []
  (set! (.-width canvas) (.-innerWidth js/window))
  (set! (.-height canvas) (.-innerHeight js/window))
  (reset! width (/ (.-width canvas) cell-size))
  (reset! height (/ (.-height canvas) cell-size))
  (doseq [y (range @height)
          x (range @width)]
    (fill_sq x y dead))
  (draw-loop))

(set! (.-onresize js/window) resized)

(defn ^:export init []
  (resized)
  (go (<! (timeout 90000))
      (.reload js/location)))
