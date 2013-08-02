(ns boolean-logic-simplifiers.driver
  (:require [taoensso.timbre :as timbre
              :refer (debug info warn error)]
            [clojure.edn]
            [boolean-logic-simplifiers.core :as simplifiers]
            [vdd-core.core :as vdd]
            [vdd-core.capture-global :as capture]))

(comment
  ; You need to run this code after resetting the repl
  (require 'boolean-logic-simplifiers.driver)
  (boolean-logic-simplifiers.driver/enable-viz)
)

(comment 
(def sample [:and 
             [:eq "x" 5]
             [:eq "z" 7]])
(def sample 
  [:or
   [:and 
    [:eq "a" 1]
    [:eq "b" 2]]
   [:and 
    [:eq "x" 5]
    [:eq "z" 7]]])

(first sample)
(rest sample)

(cond->node sample)
(condition->d3-nodes-and-links sample)
)

(defmulti cond->node 
  "TODO"
  (fn [condition] (first condition)))

(defmethod cond->node :and
  [condition]
  {:title "and"})

(defmethod cond->node :or
  [condition]
  {:title "or"})

(defmethod cond->node :eq
  [[_ n1 n2]]
  {:title (format "%s = %s" n1 n2)})

(defn- add-node 
  "TODO"
  [nodes-and-links condition parent-index]
  (let [new-node (cond->node condition)
        nodes-and-links (update-in nodes-and-links [:nodes] conj new-node)
        new-node-index (-> nodes-and-links :nodes count dec)
        link {:source parent-index :target new-node-index}]
    (update-in nodes-and-links [:links] conj link)))

(defmulti add-condition-child-nodes-and-links
  "TODO"
  (fn [condition nodes-and-links cond-index] (first condition)))

(defn add-group-condition-links
  "TODO"
  [[_ & conditions] nodes-and-links cond-index]
  (reduce (fn [nodes-and-links condition]
            (let [nodes-and-links (add-node nodes-and-links condition cond-index)
                  ; TODO remove duplication of determining this here and in add-node
                  new-node-index (-> nodes-and-links :nodes count dec)]
              (add-condition-child-nodes-and-links condition nodes-and-links new-node-index)))
          nodes-and-links
          conditions))

(defmethod add-condition-child-nodes-and-links :and
  [condition nodes-and-links cond-index]
  (add-group-condition-links condition nodes-and-links cond-index))

(defmethod add-condition-child-nodes-and-links :or
  [condition nodes-and-links cond-index]
  (add-group-condition-links condition nodes-and-links cond-index))

(defmethod add-condition-child-nodes-and-links :eq
  [condition nodes-and-links cond-index]
  ; No need to add any links. Added by parent
  nodes-and-links)



(defn condition->d3-nodes-and-links 
  "TODO"
  [condition]
  (let [node (cond->node condition)
        nodes-and-links {:nodes [node] :links []}]
    (add-condition-child-nodes-and-links condition nodes-and-links 0)))

(comment 

  ; previous try
  
  

  (defmulti condition->d3-nodes-and-links
    "TODO"
    (fn [condition nodes-and-links] (first condition)))

  (defmethod condition->d3-nodes-and-links :and
    [[type & conditions] nodes-and-links]
    (let [and-node {:title "and"}
          nodes-and-links (add-node nodes-and-links and-node)
          and-node-index (-> nodes-and-links :nodes count dec)]
      (reduce (fn [nodes-and-links condition]))
     ))

  (defmethod condition->d3-nodes-and-links :eq
    [[_ & parts] nodes-and-links]
    (add-node {:title (apply format "%s=%s" parts)}))

) ; end of comment

(defn- test-simplifiers
  [logic-str]
  (debug "Data received:" logic-str)
  (let [root-cond (clojure.edn/read-string logic-str)
        simplified (simplifiers/simplify root-cond)
        ; TODO get captured here then prepend root-cond and append simplified
        conditions [root-cond simplified]
        conditions (map condition->d3-nodes-and-links conditions)]
    (vdd/data->viz conditions)))

(defn enable-viz 
  []
  (vdd/set-viz-request-callback! test-simplifiers))

