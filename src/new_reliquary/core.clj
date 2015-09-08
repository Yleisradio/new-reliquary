(ns new-reliquary.core
  (:import [com.newrelic.api.agent NewRelic Trace]))

(definterface NewRelicTracable
  (trace [callback])
  (doTransaction [callback]))

(defn set-transaction-name [category name]
  (NewRelic/setTransactionName category name))

(defn set-request-response [req res]
  (NewRelic/setRequestAndResponse req res))

(defn add-custom-parameter [key val]
  (NewRelic/addCustomParameter key val))

(defn ignore-transaction []
  (NewRelic/ignoreTransaction))

(defn notice-error [error]
  (NewRelic/noticeError error))

(defn- wrap-with-named-transaction [category name custom-params callback]
  (fn []
    (set-transaction-name category name)
    (doseq [[key value] (seq custom-params)]
      (add-custom-parameter (str key) (str value)))
    (callback)))

(deftype NewRelicTracer [] NewRelicTracable
  (^{Trace {:dispatcher true}} trace [_ callback]
    (callback))
  (doTransaction [this callback]
    (try
      (.trace this callback)
      (catch Throwable e
        ; .trace() method already reports the error due to @Trace annotation => we don't want that
        ; NewRelic reports the error twice, thus ignore the outer ("global") transaction
        ; TODO: how to resolve nested transactions case?
        (ignore-transaction)
        (throw e)))))

(defn with-newrelic-transaction
  ([category transaction-name custom-params callback]
   (.doTransaction (NewRelicTracer.) (wrap-with-named-transaction category transaction-name custom-params callback)))
  ([category transaction-name callback]
    (with-newrelic-transaction category transaction-name {} callback))
  ([callback]
    (.doTransaction (NewRelicTracer.) callback)))

