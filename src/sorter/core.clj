(ns sorter.core
  (:use [vdd-core.capture-global :only [capture!]]))

; An implementation of quicksort that captures intermediate data
(defn qsort [[pivot & xs]]
  (when pivot
    (let [smaller #(< % pivot)
          before-pivot (filter smaller xs)
          after-pivot (remove smaller xs)]
      (capture! {:left before-pivot 
                 :pivot pivot 
                 :right after-pivot})
      (lazy-cat (qsort before-pivot)
                [pivot]
                (qsort after-pivot)))))

; From http://rosettacode.org/wiki/Sorting_algorithms/Quicksort#Clojure
(defn qsort_original [[pivot & xs]]
  (when pivot
    (let [smaller #(< % pivot)]
      (lazy-cat (qsort (filter smaller xs))
                [pivot]
                (qsort (remove smaller xs))))))
