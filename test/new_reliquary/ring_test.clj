(ns new-reliquary.ring-test
  (:require [clojure.test :refer :all]
            [new-reliquary.ring :as ring]
            [ring.mock.request :refer [request]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.tools.trace :refer [trace]]))


(defn build-app [status content-type]
  (-> (fn [req] {:body    req
                 :headers {"Content-Type" content-type}
                 :status  status})
      (ring/wrap-newrelic-transaction)
      (wrap-params)))

(def set-request-response-calls (atom []))
(def set-transaction-name-calls (atom []))
(def add-custom-parameter-calls (atom []))
(def ignore-transaction-calls   (atom 0))

(def content-type "application/json")

(def app (build-app 200 content-type))

(defn get-newrelic-request-urls []
  (map #(.getRequestURI (first %)) @set-request-response-calls))

(defn get-newrelic-response-statuses []
  (map #(.getStatus (second %)) @set-request-response-calls))

(defn get-newrelic-response-content-types []
  (map #(.getContentType (second %)) @set-request-response-calls))

(use-fixtures :each (fn [test]
                      (with-redefs [new-reliquary.core/set-request-response (fn [req res] (swap! set-request-response-calls conj [req res]))
                                    new-reliquary.core/set-transaction-name (fn [category name] (swap! set-transaction-name-calls conj [category name]))
                                    new-reliquary.core/add-custom-parameter (fn [key val] (swap! add-custom-parameter-calls conj [key val]))
                                    new-reliquary.core/ignore-transaction   (fn [] (swap! ignore-transaction-calls inc))]
                        (reset! set-request-response-calls [])
                        (reset! set-transaction-name-calls [])
                        (reset! add-custom-parameter-calls [])
                        (reset! ignore-transaction-calls   0)
                        (test))))

(deftest with-request-params
  (testing "tracks request"
    (app (request :get "http://test.fi/dogs?age=2&color=black"))
    (is (= (get-newrelic-request-urls) ["/dogs"]))
    (is (= @add-custom-parameter-calls [["age" "2"] ["color" "black"]]))
    (is (= (get-newrelic-response-content-types) [content-type]))
    (is (= (get-newrelic-response-statuses) [200]))))

(deftest with-missing-first-request-param-value
  (testing "tracks request"
    (app (request :get "http://test.fi/dogs?age=2&color="))
    (is (= @add-custom-parameter-calls [["age" "2"] ["color" ""]]))
    (is (= (get-newrelic-request-urls) ["/dogs"]))
    (is (= (get-newrelic-response-content-types) [content-type]))
    (is (= (get-newrelic-response-statuses) [200]))))

(deftest with-missing-last-request-param-value
  (testing "tracks request"
    (app (request :get "http://test.fi/dogs?age=2&color="))
    (is (= @add-custom-parameter-calls [["age" "2"] ["color" ""]]))
    (is (= (get-newrelic-request-urls) ["/dogs"]))
    (is (= (get-newrelic-response-content-types) [content-type]))
    (is (= (get-newrelic-response-statuses) [200]))))

(deftest with-missing-last-request-param-key
  (testing "tracks request but ignores missing key"
    (app (request :get "http://test.fi/dogs?age=2&=black"))
    (is (= @add-custom-parameter-calls [["age" "2"]]))
    (is (= (get-newrelic-request-urls) ["/dogs"]))
    (is (= (get-newrelic-response-content-types) [content-type]))
    (is (= (get-newrelic-response-statuses) [200]))))

(deftest without-query-params
  (testing "tracks request without custom parameters"
    (app (request :get "http://test.fi/dogs"))
    (is (= @add-custom-parameter-calls []))
    (is (= (get-newrelic-request-urls) ["/dogs"]))
    (is (= (get-newrelic-response-content-types) [content-type]))
    (is (= (get-newrelic-response-statuses) [200]))))

(deftest with-custom-status-and-content-type
  (testing "tracks response status and content type"
    (let [app (build-app 404 "text/xml")]
      (app (request :get "http://test.fi/dogs"))
      (is (= (get-newrelic-response-content-types) ["text/xml"]))
      (is (= (get-newrelic-response-statuses) [404])))))

(deftest with-compojure-route-information
  (testing "picks the parametrized compojure url from route info if available"
    (let [compojure-request (-> (request :get "http://test.fi/dogs/kultainen-noutaja")
                                (assoc :compojure/route [:any "/dogs/:id"]))]
      (app compojure-request)
      (is (= (get-newrelic-request-urls) ["/dogs/:id"])))))

(deftest with-method-in-transaction-name
  (let [app (-> (fn [_] {:body "OK"
                         :headers {"Content-Type" "text/html"}
                         :status 200})
                (ring/wrap-newrelic-transaction :add-method-to-transaction-name true)
                (wrap-params))]
    (testing "adds the HTTP method to request URI"
      (app (request :get "http://test.fi/dogs"))
      (is (= (get-newrelic-request-urls) ["/GET /dogs"]))
      (is (= (get-newrelic-response-statuses) [200]))
      (is (= (get-newrelic-response-content-types) ["text/html"])))))
