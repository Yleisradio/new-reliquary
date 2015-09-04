(ns new-reliquary.core
  (:import [com.newrelic.api.agent NewRelic Trace]))

(definterface INewRelicTracable
  (trace []))

(deftype NewRelicTracer [callback] INewRelicTracable
  (^{Trace {:dispatcher true}} trace [_]
    (callback)))

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

(defn- named-transaction [category name custom-params callback]
  (fn []
    (set-transaction-name category name)
    (doseq [[key value] (seq custom-params)]
      (add-custom-parameter (str key) (str value)))
    (callback)))

(defn with-newrelic-transaction
  ([category transaction-name custom-params callback]
   (.trace (NewRelicTracer. (named-transaction category transaction-name custom-params callback))))
  ([category transaction-name callback]
    (with-newrelic-transaction category transaction-name {} callback))
  ([callback]
    (.trace (NewRelicTracer. callback))))

