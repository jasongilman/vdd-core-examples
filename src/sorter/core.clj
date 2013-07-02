(ns sorter.core
  (:require [vdd-core.core :as vdd])
  (:use [vdd-core.capture-global :only [captured
                                        capture!
                                        reset-captured!]]))

(comment
  ; Commands to send to the repl to test this
  (require '[vdd-core.core :as vdd])
  ; Start and save the viz 
  (def viz-server (vdd/start-viz))
  ; Shut it down
  (vdd/stop-viz viz-server)
  
  (use 'sorter.core)
  (viz-qsort (shuffle (range 20)))
  
  (use 'clj-wamp.server)
  (use 'vdd-core.internal.websocket)
  (send-event! (evt-url "vizdata") 5)
  
  (use 'vdd-core.capture-global)
  (captured)
)

; Forward declare a data munger for visualization data
(declare qsort qsort-list-combiner)


(defn viz-qsort 
  "Runs the quicksort implementation capturing data as it runs. Then munges the
  data for visualization and sends it to the quicksort visualization."
  [data]
  ; Reset the global captured state
  (reset-captured!)
  ; Run quicksort. Visualization data will be captured
  (doall (qsort data))
  (let [c (captured)
        combined (qsort-list-combiner c)
        ; Prepend the unsorted data
        combined (concat [{:items data}]
                         combined)]
    ; Send the data to be visualized
    ; TODO need to determine if we really need multiple channels
    (vdd/data->viz "vizdata" combined)))


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


; This is a little convoluted. The qsort algorithm in clojure is recursive so it only looks at a portion of the list
; at a time. I wanted the entire list to be displayed in the visualization so this keeps track of the results
; and appends or prepends the portions of the list that were missing in the recursed captured data.
; TODO We should simplify this and we can handle it in the visualization.
; We want to show only the active data at any particular time. We should be able to send the the pieces
; of the component data to visualize without having to squash everything together
(defn qsort-list-combiner
  "Combines togehter the captured maps from quicksort to make it easier to display"
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
          
          ; We only want to prepend the sorted items to the right side so we grabe the last left side
          ; which should be completely sorted.
          last-left (last lefts)
          last-left (or (:items last-left) [])
          
          ; Append the current pivot and right to the left recursed items
          lefts (map #(update-in % [:items] concat [curr-pivot] curr-right) lefts)
          
          ; Prepend the last left and pivot to the left recursed items
          rights (map #(update-in % [:items] (fn [v] (concat last-left [curr-pivot] v))) rights)]
      (concat
        [{:items (concat curr-left [curr-pivot] curr-right) :pivot curr-pivot}]
        lefts
        rights))
    []))