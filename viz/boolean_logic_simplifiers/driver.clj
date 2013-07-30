(ns boolean-logic-simplifiers.driver
  (:require [taoensso.timbre :as timbre
              :refer (debug info warn error)]
            [boolean-logic-simplifiers.core :as simplifiers]
            [vdd-core.core :as vdd]
            [vdd-core.capture-global :as capture]))



(defn- test-simplifiers
  [logic-str]
  (debug "Data received:" logic-str))


(defn enable-viz 
  []
  (vdd/set-viz-request-callback! test-simplifiers))