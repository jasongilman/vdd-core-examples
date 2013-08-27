(ns qsort.driver
  (:require [sorter.core :as sorter]
            [vdd-core.core :as vdd]
            [vdd-core.capture-global :as capture]))


(comment
  (require 'qsort.driver)
  (def server (vdd/start-viz))
  (vdd/stop-viz server)
  (viz-qsort (shuffle (range 20)))
  (viz-qsort [3 2 4 5 1])
)

; Forward declare a data munger for visualization data
(declare qsort qsort-list-combiner)

(defn viz-qsort 
  "Runs the quicksort implementation capturing data as it runs. Then munges the
  data for visualization and sends it to the quicksort visualization."
  [data]
  ; Reset the global captured state
  (capture/reset-captured!)
  ; Run quicksort. Visualization data will be captured
  (let [results (doall (sorter/qsort data))
        combined (qsort-list-combiner (capture/captured))
        ; Prepend the unsorted data
        combined (concat [{:items data :left [] :right []}]
                         combined
                         [{:items results :left [] :right []}])]
    ; Send the data to be visualized
    (vdd/data->viz combined)))

; This is a little convoluted. The qsort algorithm in clojure is recursive so it only looks at a portion of the list
; at a time. I wanted the entire list to be displayed in the visualization so this keeps track of the results
; and appends or prepends the portions of the list that were missing in the recursed captured data.
; We want to show only the active data at any particular time. Ideally, we should be able to send the the pieces
; of the component data to visualize without having to squash everything together
(defn qsort-list-combiner
  "Combines together the captured maps from quicksort to make it easier to display"
  [[top & others]]
  (if top
    (let [curr-left (:left top)
          curr-pivot (:pivot top)
          curr-right (:right top)
          
          ; Divide others into two lists. One is the iterations that were sorted
          ; The others are after the pivot 
          [lefts rights] (split-at (count curr-left) others)
          
          ; Recurse down into the others and fix them
          lefts (qsort-list-combiner lefts)
          rights (qsort-list-combiner rights)
          
          ; We only want to prepend the sorted items to the right side so we grab the last left side
          ; which should be completely sorted.
          last-left (last lefts)
          last-left (or (:items last-left) [])
          
          ; Append the current pivot and right to the left recursed items
          lefts (map #(update-in % [:items] concat [curr-pivot] curr-right) lefts)
          
          ; Prepend the last left and pivot to the left recursed items
          rights (map #(update-in % [:items] (fn [v] (concat last-left [curr-pivot] v))) rights)]
      (concat
        [(assoc top :items (concat curr-left [curr-pivot] curr-right))]
        lefts
        rights))
    []))