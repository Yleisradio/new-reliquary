(ns new-reliquary.ring
  (:require [new-reliquary.core :as newrelic])
  (:import (com.newrelic.api.agent Response HeaderType Request)))

; see http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
(def status-code-mapping
  {100 "Continue"
   101 "Switching Protocols"
   200 "OK"
   201 "Created"
   202 "Accepted"
   203 "Non-Authoritative Information"
   204 "No Content"
   205 "Reset Content"
   206 "Partial Content"
   300 "Multiple Choices"
   301 "Moved Permanently"
   302 "Found"
   303 "See Other"
   304 "Not Modified"
   305 "Use Proxy"
   307 "Temporary Redirect"
   400 "Bad Request"
   401 "Unauthorized"
   402 "Payment Required"
   403 "Forbidden"
   404 "Not Found"
   405 "Method Not Allowed"
   406 "Not Acceptable"
   407 "Proxy Authentication Required"
   408 "Request Timeout"
   409 "Conflict"
   410 "Gone"
   411 "Length Required"
   412 "Precondition Failed"
   413 "Request Entity Too Large"
   414 "Request-URI Too Long"
   415 "Unsupported Media Type"
   416 "Requested Range Not Satisfiable"
   417 "Expectation Failed"
   500 "Internal Server Error"
   501 "Not Implemented"
   502 "Bad Gateway"
   503 "Service Unavailable"
   504 "Gateway Timeout"
   505 "HTTP Version Not Supported"})

(defn- resolve-uri [req]
  (or (get-in req [:compojure/route 1])
      (:uri req)))

(defn- resolve-query-params [req]
  (->> (:query-params req)
       (map (fn [[key val]] [(str key) (str val)]))
       (remove (comp empty? first))
       (into {})))

(defn- resolve-headers [req]
  (->> (:headers req)
       (map (fn [[key val]] [(str key) (str val)]))
       (into {})))

(defn- resolve-content-type [res]
  (->> (:headers res)
       (filter (fn [[key _]] (= "content-type" (.toLowerCase (str key)))))
       (first)
       (second)))

(deftype NewRelicRingWrapperRequest [req] Request
  (getRequestURI [_] (resolve-uri req))
  (getRemoteUser [_] nil)
  (getParameterNames [_] (keys (resolve-query-params req)))
  (getParameterValues [_ name]
    (let [value (get (resolve-query-params req) name)]
      (if (nil? value) nil (into-array [(str value)]))))
  (getCookieValue [_ _] nil)
  (getAttribute [_ _] nil)
  (getHeader [_ name] (get (resolve-headers req) name))
  (getHeaderType [_] HeaderType/HTTP))

(deftype NewRelicRingWrapperResponse [res] Response
  (getContentType [_] (resolve-content-type res))
  (getStatusMessage [this] (get status-code-mapping (.getStatus this)))
  (getStatus [_] (get res :status 200)))

(defn- add-query-params [params]
  (doseq [[key value] (sort-by key (seq params))]
    (newrelic/add-custom-parameter key value)))

(defn- web-transaction [request-hander request]
  (fn []
    (let [newrelic-req (NewRelicRingWrapperRequest. request)
          response     (request-hander request)
          newrelic-res (NewRelicRingWrapperResponse. response)]
      (add-query-params (resolve-query-params request))
      (newrelic/set-request-response newrelic-req newrelic-res)
      response)))

(defn wrap-newrelic-transaction [handler]
  (fn [request] (newrelic/with-newrelic-transaction (web-transaction handler request))))
