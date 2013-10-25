(ns async-life.core
  (:require [cljs.core.async :as async
             :refer [<! >! chan timeout alts!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def line-colour "#cdcdcd")
(def alive "#666")
(def dead "#eee")
(def width (atom nil))
(def height (atom nil))
(def cell-size (atom nil))
(def canvas (.getElementById js/document "world"))
(def context (.getContext canvas "2d"))
(def prob 0.5)

(defn fill_sq [x y colour]
  (let [cell-size @cell-size]
    (set! (.-fillStyle context) colour)
    (set! (.-strokeStyle context) line-colour)
    (.fillRect context
               (* x cell-size)
               (* y cell-size)
               cell-size
               cell-size)
    (.strokeRect context
                 (* x cell-size)
                 (* y cell-size)
                 cell-size
                 cell-size)))

(def draw
  (let [c (chan)]
    (go (loop []
          (let [[x y colour] (<! c)]
            (<! (timeout 10))
            (fill_sq x y colour)
            (recur))))
    c))

(defn cell [[x y]]
  (let [new-neighbor (chan)
        input (chan)
        neighbors #{}
        initial-state (if (< (rand) prob) :alive :dead)]
    (if (= initial-state :alive)
      (fill_sq x y alive))
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
                        colour (if (= new-state :alive) alive dead)]
                    (if draw?
                      (do (doseq [n neighbors]
                            (>! n delta))
                          (>! draw [x y colour])))
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
  (let [xys (for [x (range @width)
                  y (range @height)]
              [x y])
        cells (zipmap xys (map cell xys))]
    (doseq [[xy [input _]] cells]
      (go
       (doseq [[_ nn] (neighbors xy cells)]
         (>! nn input))))))

(defn ^:export init []
  (set! (.-width canvas) (.-clientWidth canvas))
  (set! (.-height canvas) (.-clientHeight canvas))
  (reset! width 75)
  (reset! cell-size (/ (.-clientWidth canvas) @width))
  (reset! height (/ (.-clientHeight canvas) @cell-size))
  (doseq [y (range @height)
          x (range @width)]
    (fill_sq x y dead))
  (draw-loop))
