(defproject vdd-core-examples "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.3"]
                                  ; Added vdd-core as a development dependency
                                  [vdd-core "0.1.0"]]
                   ; Add viz as a source path to load visualization drivers
                   :source-paths ["viz"]}})
