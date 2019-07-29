(defproject yleisradio/new-reliquary "1.1.0"
  :description "Clojure newrelic java api wrapper"
  :url "https://github.com/Yleisradio/new-reliquary"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.newrelic.agent.java/newrelic-api "5.3.0"]]
  :profiles { :dev { :dependencies [[ring/ring-core "1.7.1"]
                                    [ring-mock "0.1.5"]
                                    [org.clojure/tools.trace "0.7.10"]]}}
  :plugins [[lein-ancient "0.6.15"]]
  :signing {:gpg-key "D9A17928"})
