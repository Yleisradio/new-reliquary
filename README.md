# New-reliquary

Clojure wrapper for New Relic


[![Build Status](https://travis-ci.org/Yleisradio/new-reliquary.svg)](https://travis-ci.org/Yleisradio/new-reliquary)
[![Dependencies Status](http://jarkeeper.com/Yleisradio/new-reliquary/status.png)](http://jarkeeper.com/Yleisradio/new-reliquary)

------------------------------------
## Installation

Add this to your Leiningen project.clj `:dependencies`:

    [yleisradio/new-reliquary "0.1.5"]

Jar is available in Clojars.

--------------------------------------
## New Relic Transactions

Basic NewRelic transaction tracing utilities located in `new-reliquary.core`

### API

#### `with-newrelic-transaction`

```clojure 
(defn with-newrelic-transaction
  ([category transaction-name custom-params callback] ...)
  ([category transaction-name callback]               ...)
  ([callback]                                         ...))
```

Creates a transaction with optional transaction name and custom params.
If transaction name is not passed, then `set-transaction-name` should 
be used inside the transaction.

#### `set-transaction-name [category name]`

Sets name to the transaction. Must be used if transaction is created
with `with-newrelic-transaction callback`. See http://newrelic.github.io/java-agent-api/javadoc/com/newrelic/api/agent/NewRelic.html#setTransactionName(java.lang.String,%20java.lang.String)

#### `notice-error [error]`

See http://newrelic.github.io/java-agent-api/javadoc/com/newrelic/api/agent/NewRelic.html#noticeError(java.lang.String)

#### `ignore-transaction`

See http://newrelic.github.io/java-agent-api/javadoc/com/newrelic/api/agent/NewRelic.html#ignoreTransaction()

#### `add-custom-param [name value]`

Adds new custom parameter to the transaction. Must be called inside the
transaction. See: http://newrelic.github.io/java-agent-api/javadoc/com/newrelic/api/agent/NewRelic.html#addCustomParameter(java.lang.String,%20java.lang.String)


### Examples

```clojure 
(:require [new-reliquary.core :as [newrelic]])

(defn update-facebook-likes [] ...)

(newrelic/with-newrelic-transaction
    "My custom category"
    "Facebook data updating"
    {:user "jk" :huge-clojure-fan true}
    update-facebook-likes)
    
(newrelic/with-newrelic-transaction 
  (fn [] (newrelic/set-transaction-name "backend" "poller") ...)
```


## Ring middleware

Middleware to start NewRelic web transaction. Located in `new-reliquary.ring`

If you want to add query parameters as new relic custom params, make sure that 
request contains hash map `:query-params` (not in the default ring setup).
This can be achieved easily by using `ring.middleware.params/wrap-params`.

### API

#### `wrap-newrelic-transaction [next-ring-request-handler]`

### Examples

```clojure
(ns new-reliquary-example.main
  (:require [new-reliquary.ring :refer [wrap-newrelic-transaction]]
            [ring.middleware.params :refer [wrap-params]]))

(defn request-handler [request] {:body "Hello world"})
(def app (-> request-handler
            (wrap-newrelic-transaction)
            (wrap-params)))
```


## License

Distributed under the Eclipse Public License either version 1.0 or any later version.
