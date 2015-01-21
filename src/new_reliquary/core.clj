(ns new-reliquary.core
  (:require [clojure.string :refer [split]])
  (:import [com.newrelic.api.agent NewRelic Trace]))

(definterface NewRelicTraceable (callWithTrace [category name params callback]))

(defn set-transaction-name [category name]
  (NewRelic/setTransactionName category name))

(defn add-custom-parameter [key val]
  (NewRelic/addCustomParameter key val))

(defn ignore-transaction []
  (NewRelic/ignoreTransaction))

(deftype NewRelicTracer []
  NewRelicTraceable (^{Trace {:dispatcher true}}
                      callWithTrace [this category transaction-name query-params callback]
                      (set-transaction-name category transaction-name)
                      (doseq [[key val] (sort-by key query-params)]
                        (when (> (count (name key)) 0)
                          (add-custom-parameter (name key) (str val))))
                      (callback)))

(def tracer (NewRelicTracer.))

(defn with-newrelic-transaction
  ([category transaction-name custom-parameters callback]
    (try
      (.callWithTrace tracer category transaction-name custom-parameters callback)
      (catch Throwable e
        (ignore-transaction)
        (throw e))))

  ([category transaction-name callback]
    (with-newrelic-transaction category transaction-name {} callback)))


(defn notice-error [error]
  (NewRelic/noticeError error))