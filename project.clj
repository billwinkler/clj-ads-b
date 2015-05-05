(defproject com.lemondronor/ads-b "0.1.1-SNAPSHOT"
  :description "Clojure code for parsing ADS-B transponder messages."
  :url "https://github.com/wiseman/clj-ads-b"
  :license {:name "GNU General Public License Version 3"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  :deploy-repositories [["releases" :clojars]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :dependencies [[org.apache.avro/avro "1.7.6"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :profiles {:dev {:source-paths ["examples"]}})
