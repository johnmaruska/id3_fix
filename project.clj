(defproject id3-fix "0.1.0-SNAPSHOT"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [claudio "0.1.3"]]
  :main ^:skip-aot id3-fix.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
