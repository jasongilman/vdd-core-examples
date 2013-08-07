(ns boolean-logic-simplifiers.core
  (:require [clojure.zip :as z]
            [boolean-logic-simplifiers.factory :as f]))

(defn- and? [c] (= (:type c) :and))
(defn- or? [c] (= (:type c) :or))

(defn and-simplify 
  "TODO"
  [condition]
    (cond
      (and? condition)
      (let [subconditions (map and-simplify (:conditions condition))
            {and-children true others false} (group-by #(= :and (:type %)) subconditions)
            from-and-children (flatten (map :conditions and-children))]
        (assoc condition :conditions (concat others from-and-children)))
      
      (or? condition)
      (update-in condition [:conditions] #(map and-simplify %))
      
      :else 
      condition))

(defn simplify 
  "TODO"
  [condition]
  (and-simplify condition))

