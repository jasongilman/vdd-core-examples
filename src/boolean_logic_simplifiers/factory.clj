(ns boolean-logic-simplifiers.factory
  (:require [clojure.zip :as z]
            [boolean-logic-simplifiers.factory-dsl])
  (:import [java.io StringWriter]))

(defn cond-zipper 
  "Returns a clojure zipper object that can traverse over a condition"
  [cond]
  (z/zipper 
    :conditions
    :conditions
    (fn [existing new-children]
      (assoc existing :conditions new-children))
    cond))

(defn- assign-ids
  "Assigns unique ids to the condition and it's children."
  [cond]
  (let [zipped (cond-zipper cond)]
    (loop [zipped zipped next-id 0]
      (if (z/end? zipped)
        (z/root zipped)
        (let [next-node (z/replace zipped (-> zipped
                                                 z/node
                                                 (assoc :id next-id)))]
          (recur (z/next next-node) (inc next-id)))))))

(defn string->condition [s]
  (binding [*ns* (find-ns 'boolean-logic-simplifiers.factory-dsl)]
    (assign-ids (load-string s))))

(defmulti cond->clojure-calls 
  "Multimethod to convert a condition into a string."
  (fn [condition] (:type condition)))

(defn condition->string [c]
  (let [w (StringWriter.)]
    (clojure.pprint/pprint (cond->clojure-calls c) w)
    (.toString w)))

(defmethod cond->clojure-calls :and
  [{conditions :conditions}]
  (list 'and (for [c conditions]
              (cond->clojure-calls c))))

(defmethod cond->clojure-calls :or
  [{conditions :conditions}]
  (list 'or (for [c conditions]
              (cond->clojure-calls c))))

(defmethod cond->clojure-calls :eq
  [{v1 :value1 v2 :value2}]
  (list '= v1 v2))
