(defproject yleisradio/new-reliquary "0.1.3"
  :description "Clojure newrelic java api wrapper"
  :url "https://github.com/Yleisradio/new-reliquary"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.newrelic.agent.java/newrelic-api "3.10.0"]]
  :profiles { :dev { :dependencies [[ring/ring-core "1.3.1"]
                                    [ring-mock "0.1.5"]
                                    [org.clojure/tools.trace "0.7.8"]]}}
  :plugins [[lein-ancient "0.5.5"]])
