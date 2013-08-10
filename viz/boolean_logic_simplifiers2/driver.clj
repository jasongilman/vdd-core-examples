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

(defmulti cond->node 
  "Multimethod to convert a condition into a single node. Does not handle adding child nodes"
  (fn [condition depth] (:type condition)))

(defmethod cond->node :and
  [_ depth]
  {:title "&" :depth depth :type :and})

(defmethod cond->node :or
  [_ depth]
  {:title "||" :depth depth :type :or})

(defmethod cond->node :eq
  [{v1 :value1 v2 :value2} depth]
  {:title (format "%s = %s" v1 v2) :depth depth :type :eq})

(defn- add-node 
  "TODO"
  [nodes-and-links new-node parent-index]
  (let [nodes-and-links (update-in nodes-and-links [:nodes] conj new-node)
        new-node-index (-> nodes-and-links :nodes count dec)
        link {:source parent-index :target new-node-index}]
    (update-in nodes-and-links [:links] conj link)))

(defmulti add-condition-child-nodes-and-links
  "TODO"
  (fn [condition nodes-and-links cond-index depth] (:type condition)))

(defn add-group-condition-links
  "TODO"
  [{conditions :conditions} nodes-and-links cond-index depth]
  (let [depth (inc depth)]
    (reduce (fn [nodes-and-links condition]
              (let [new-node (cond->node condition depth)
                    nodes-and-links (add-node nodes-and-links new-node cond-index)
                    ; TODO remove duplication of determining this here and in add-node
                    new-node-index (-> nodes-and-links :nodes count dec)]
                (add-condition-child-nodes-and-links condition nodes-and-links new-node-index depth)))
            nodes-and-links
            conditions)))

(defmethod add-condition-child-nodes-and-links :and
  [condition nodes-and-links cond-index depth]
  (add-group-condition-links condition nodes-and-links cond-index depth))

(defmethod add-condition-child-nodes-and-links :or
  [condition nodes-and-links cond-index depth]
  (add-group-condition-links condition nodes-and-links cond-index depth))

(defmethod add-condition-child-nodes-and-links :eq
  [condition nodes-and-links cond-index depth]
  ; No need to add any links. Added by parent
  nodes-and-links)

(defn condition->d3-nodes-and-links 
  "TODO"
  [condition]
  (let [node (cond->node condition 1)
        nodes-and-links {:nodes [node] :links []}]
    (add-condition-child-nodes-and-links condition nodes-and-links 0 1)))

(defn- test-simplifiers
  [logic-str]
  (debug "Data received:" logic-str)
  (let [root-cond (factory/string->condition logic-str)
        simplified (simplifiers/simplify root-cond)
        ; TODO get captured here then prepend root-cond and append simplified
        conditions [root-cond simplified]
        conditions (map condition->d3-nodes-and-links conditions)]
    (vdd/data->viz conditions)))

(comment
  (factory/string->condition "(and (= :x 1) (= :z 2))")
  (test-simplifiers "(and (= :x 1) (= :z 2))")
  )

(defn enable-viz 
  []
  (vdd/set-viz-request-callback! test-simplifiers))

