(ns boolean-logic-simplifiers.factory
  (:refer-clojure :exclude [and or =]))

(comment
  ; This namespace allows easily creating conditions for testing. Can be called like the following
(or 
  (and 
    (= :x 5) 
    (= :z 7))
  (and 
    (= :a 1) 
    (= :b 2)))

)

(defn and [& parts]
  {:type :and :conditions parts})

(defn or [& parts]
  {:type :or :conditions parts})

(defn = [v1 v2]
  {:type :eq :value1 v1 :value2 v2})

(defn string->condition [s]
  (binding [*ns* (find-ns 'boolean-logic-simplifiers.factory)]
    (load-string s)))