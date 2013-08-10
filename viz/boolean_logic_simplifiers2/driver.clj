(ns boolean-logic-simplifiers2.driver
  (:require [taoensso.timbre :as timbre
              :refer (debug info warn error)]
            [clojure.edn]
            [boolean-logic-simplifiers.core :as simplifiers]
            [boolean-logic-simplifiers.factory :as factory]
            [vdd-core.core :as vdd]
            [vdd-core.capture-global :as capture]))

(comment
  ; You need to run this code after resetting the repl
  (require 'boolean-logic-simplifiers2.driver)
  (enable-viz)
)

(defmulti cond->d3 
  "Multimethod to convert a condition into a d3 node."
  (fn [condition] (:type condition)))

(defn make-group-d3-node [{id :id conditions :conditions type :type}]
  {:name (format "%s %s" id (name type) )
   :_id id 
   :type type
   :children (map cond->d3 conditions)})

(defmethod cond->d3 :and
  [c]
  (make-group-d3-node c))

(defmethod cond->d3 :or
  [c]
  (make-group-d3-node c))

(defmethod cond->d3 :eq
  [{v1 :value1 v2 :value2 id :id}]
  {:name (format "%s %s=%s" id v1 v2) 
   :_id id 
   :type :eq})


(defn- test-simplifiers
  [logic-str]
  (debug "Data received:" logic-str)
  (let [root-cond (factory/string->condition logic-str)]
    (capture/reset-captured!)
    (doall (simplifiers/simplify root-cond))
    (vdd/data->viz {:root (cond->d3 root-cond)
                    :changes (capture/captured)})))

(comment
  (simplifiers/simplify 
    (factory/string->condition "(and 
                               (and 
                               (= :a 1)
                               (= :b 2))
                               (and 
                               (= :x 5)
                               (= :z 7)))
                               "))
  
  (capture/captured)
  (test-simplifiers "(and (= :x 1) (= :z 2))")
  
  
  )

(defn enable-viz 
  []
  (vdd/set-viz-request-callback! test-simplifiers))

