(ns new-reliquary.test
  (:require [clojure.test :refer :all]
            [new-reliquary.core :as core]
            [new-reliquary.ring :refer [wrap-newrelic-transaction]]
            [ring.mock.request :refer [request]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.tools.trace :refer [trace]])
  (:import [com.newrelic.api.agent NewRelic Trace]))

(def path "/dogs")
(def url (str "http://test.fi" path))
(def category "my test app")

(def set-transaction-name-calls (atom []))
(def add-custom-parameter-calls (atom []))

(defn final-handler [request] {:body request})
(def app (wrap-params (wrap-newrelic-transaction category final-handler)))

(use-fixtures :each (fn [test]
                      (with-redefs [new-reliquary.core/set-transaction-name (fn [category name] (swap! set-transaction-name-calls conj [category name]))
                                    new-reliquary.core/add-custom-parameter (fn [key val] (swap! add-custom-parameter-calls conj [key val]))]
                        (reset! set-transaction-name-calls [])
                        (reset! add-custom-parameter-calls [])
                        (test))))

(deftest with-request-params
  (testing "tracks request"
    (let [req (request :get "http://test.fi/dogs?age=2&color=black")]
      (app req)
      (is (= @set-transaction-name-calls [[category "/dogs"]]))
      (is (= @add-custom-parameter-calls [["age" "2"] ["color" "black"]])))))

(deftest with-missing-first-request-param-value
  (testing "tracks request"
    (app (request :get "http://test.fi/dogs?age=2&color="))
    (is (= @add-custom-parameter-calls [["age" "2"] ["color" ""]]))
    (is (= @set-transaction-name-calls [[category "/dogs"]]))))

(deftest with-missing-last-request-param-value
  (testing "tracks request"
    (app (request :get "http://test.fi/dogs?age=2&color="))
    (is (= @add-custom-parameter-calls [["age" "2"] ["color" ""]]))
    (is (= @set-transaction-name-calls [[category "/dogs"]]))))

(deftest with-missing-last-request-param-key
  (testing "tracks request but ignores missing key"
    (app (request :get "http://test.fi/dogs?age=2&=black"))
    (is (= @add-custom-parameter-calls [["age" "2"]]))
    (is (= @set-transaction-name-calls [[category "/dogs"]]))))

(deftest without-query-params
  (testing "tracks request without custom parameters"
    (app (request :get "http://test.fi/dogs"))
    (is (= @add-custom-parameter-calls []))
    (is (= @set-transaction-name-calls [[category "/dogs"]]))))

(deftest without-query-params-hash-map-in-request
  (testing "tracks request without custom parameters"
    ((wrap-newrelic-transaction category final-handler) (request :get "http://test.fi/dogs"))
    (is (= @add-custom-parameter-calls []))
    (is (= @set-transaction-name-calls [[category "/dogs"]]))))

(deftest with-boolean-parameters
  (testing "starts transaction"
    (core/with-newrelic-transaction category "my transaction" {:huge-clojure-fan true} #())
    (is (= @set-transaction-name-calls [[category "my transaction"]]))
    (is (= @add-custom-parameter-calls [["huge-clojure-fan" "true"]]))))