(ns boolean-logic-simplifiers.test.core
  (:require [boolean-logic-simplifiers.factory :as f])
  (:use [boolean-logic-simplifiers.core]
        [clojure.test]))

(defn v 
  "Short helper to create a value condition."
  [v]
  (f/= :v v))

; None of these conditions should be changed by the and simplifier
(deftest test-and-simplifier-none-required
  (are [c] (= c (and-simplify c))
    (v 1)
    (f/or (v 1) (v 2))
    (f/and (v 1) (v 2))
    (f/and (v 1) (f/or (v 2) (v 3)))))

(deftest test-and-simplifier-single-level
  (is (= (f/and 
           (v 1) 
           (v 2) 
           (v 3))
         (and-simplify 
           (f/and 
             (v 1) 
             (f/and
               (v 2)
               (v 3)))))))

(deftest test-and-simplifier-many-nested
  (is (= (f/and 
           (v 1) 
           (v 2) 
           (v 3)
           (v 4))
         (and-simplify 
           (f/and 
             (v 1) 
             (f/and 
               (v 2) 
               (f/and 
                 (v 3) 
                 (v 4))))))))
