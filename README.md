# New-reliquary

Clojure wrapper for New Relic

------------------------------------
### Installation

Add this to your Leiningen project.clj :dependencies:

    [new-reliquary "0.1.0"]

Jar is available in Clojars.

--------------------------------------
### New Relic Transactions

#### Api

    (new-reliquary.core/with-newrelic-transaction
        transaction-category  ;You'll be able to select category from New Relic UI
                              ;[Applications -> Monitoring -> Transactions -> Type dropdown].
        transaction-name
        optional-custom-parameters ;helpful when you need to investigate errors or slow performance
        function-to-evaluate-in-transaction)

##### Example

    (defn update-facebook-likes [] ...)

    (with-newrelic-transaction
        "My custom category"
        "Facebook data updating"
        {:user "jk" :huge-clojure-fan true}
        update-facebook-likes)

--------------------------------------------
### Error handling

#### Api

    (new-reliquary.core/notice-error error)

    * error can be String or Throwable

#### Example

    (new-reliquary.core/notice-error "Couldn't connect to Faceboook.")

or

     (try
       (some-fn)
       (catch Throwable e
           (new-reliquary.core/notice-error e)))

--------------------------------------------

### Ring middleware

Middleware to start newrelic transaction.

If you want to add query parameters as new relic custom params, make sure that request contains hash map :query-params (not in the default ring setup).
This can be achieved easily by using ring.middleware.params/wrap-params.

#### Api

    (new-reliquary.ring/wrap-newrelic-transaction transaction-category next-ring-request-handler)

#### Example

    (ns new-reliquary-example.main
      (:require [new-reliquary.ring :refer [wrap-newrelic-transaction]]
                [ring.middleware.params :refer [wrap-params]]))

    (defn final-handler [request] {:body "Hello world"})
    (def app (wrap-params
                (wrap-newrelic-transaction "my transaction category" final-handler)))


## License

Distributed under the Eclipse Public License either version 1.0 or any later version.