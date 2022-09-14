(defproject com.lemondronor/ads-b "0.1.4-SNAPSHOT"
  :description "Clojure code for parsing ADS-B transponder messages."
  :url "https://github.com/billwinkler/clj-ads-b"
  :license {:name "GNU General Public License Version 3"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  :deploy-repositories [["releases" :clojars]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :dependencies [[org.apache.avro/avro "1.11.1"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/math.numeric-tower "0.0.5"]
                 [org.opensky-network/libadsb "3.4.0"]]
  :profiles {:dev {:source-paths ["examples"]}})
