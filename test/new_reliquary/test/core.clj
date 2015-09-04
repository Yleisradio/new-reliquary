(ns new-reliquary.test.core
  (:require [clojure.test :refer :all]
            [new-reliquary.core :as core]
            [ring.mock.request :refer [request]]
            [ring.middleware.params :refer [wrap-params]]
            [clojure.tools.trace :refer [trace]]))


(def set-transaction-name-calls (atom []))
(def add-custom-parameter-calls (atom []))

(defn empty-handler [])

(use-fixtures :each (fn [test]
                      (with-redefs [new-reliquary.core/set-transaction-name (fn [category name] (swap! set-transaction-name-calls conj [category name]))
                                    new-reliquary.core/add-custom-parameter (fn [key val] (swap! add-custom-parameter-calls conj [key val]))]
                        (reset! set-transaction-name-calls [])
                        (reset! add-custom-parameter-calls [])
                        (test))))


(deftest with-name-and-custom-params
  (testing "tracks transaction"
    (core/with-newrelic-transaction "api" "tsers" {:foo "bar"} empty-handler)
    (is (= @add-custom-parameter-calls [[":foo" "bar"]]))
    (is (= @set-transaction-name-calls [["api" "tsers"]]))))

(deftest with-name-and-without-custom-params
  (testing "tracks transaction"
    (core/with-newrelic-transaction "api" "tsers" empty-handler)
    (is (= @add-custom-parameter-calls []))
    (is (= @set-transaction-name-calls [["api" "tsers"]]))))

(deftest without-name-or-custom-params
  (testing "tracks transaction without name/category info"
    (core/with-newrelic-transaction empty-handler)
    (is (= @add-custom-parameter-calls []))
    (is (= @set-transaction-name-calls []))))


