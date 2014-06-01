(ns new-reliquary.ring
  (:require [new-reliquary.core :refer [with-newrelic-transaction]]))

(defn wrap-newrelic-transaction [category handler]
  "Middleware to start newrelic transaction.

  If you want to add query parameters as new relic custom params, make sure that request contains hash map :query-params (not in the default ring setup).
  This can be achieved easily by using ring.middleware.params/wrap-params as in tests."
  (fn [request]
    (let [newrelic-transaction-name (:uri request)
          newrelic-custom-parameters (:query-params request)]
      (with-newrelic-transaction category newrelic-transaction-name newrelic-custom-parameters #(handler request)))))
