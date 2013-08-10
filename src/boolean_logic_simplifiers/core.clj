(ns boolean-logic-simplifiers.core
  (:require [vdd-core.capture-global :as vdd]
            [boolean-logic-simplifiers.factory :as f]))

(defn- and-condition? [c] (= (:type c) :and))
(defn- or-condition? [c] (= (:type c) :or))


(defn- capture-moves! 
  "Captures moves from of conditions to another condition"
  [from-condition to-condition conditions]
  (doseq [c conditions]
    (vdd/capture! {:type :node-move 
                   :from (:id from-condition) 
                   :to (:id to-condition) 
                   :id (:id c)})))

(defn- capture-remove! 
  "Captures removals of nodes from the tree"
  [from-condition condition]
  (vdd/capture! {:type :node-remove 
                 :from (:id from-condition)
                 :id (:id condition)}))

(defn and-simplify 
  "TODO"
  [condition]
    (cond
      (and-condition? condition)
      (let [subconditions (map and-simplify (:conditions condition))
            {and-children true others false} (group-by #(= :and (:type %)) subconditions)
            from-and-children (flatten (map (fn [and-child]
                                              ; Capture that we're moving children from and-child to condition
                                              (capture-moves! and-child condition (:conditions and-child))
                                              
                                              ; Capture that and-child is being removed
                                              (capture-remove! condition and-child)
                                              
                                              (:conditions and-child) )
                                            and-children))]
        (assoc condition :conditions (concat others from-and-children)))
      
      (or-condition? condition)
      (update-in condition [:conditions] #(map and-simplify %))
      
      :else 
      condition))



(defn simplify 
  "TODO"
  [condition]
  (and-simplify condition))

