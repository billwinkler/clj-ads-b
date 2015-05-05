(ns com.lemondronor.ads-b-test
  (:require [clojure.pprint :as pprint]
            [clojure.test :refer :all]
            [com.lemondronor.ads-b :as ads-b]))


(deftest beast-parsing
  (testing "Mode A/C"
    (is (= {:type :mode-ac, :payload "7700"}
           (ads-b/parse-beast-str "*7700;"))))
  (testing "Mode S short"
    (is (= {:type :mode-s-short, :payload "02E99619FACDAE"}
           (ads-b/parse-beast-str "*02E99619FACDAE;"))))
  (testing "Mode S long"
    (is (= {:type :mode-s-long, :payload "8D3C5EE69901BD9540078D37335F"}
           (ads-b/parse-beast-str "*8D3C5EE69901BD9540078D37335F;"))))
  (testing "Mode A/C with timestamp"
    (is (= {:type :mode-ac, :timestamp [1459 593960052], :payload "7700"}
           (ads-b/parse-beast-str "@016CE3671C747700;"))))
  (testing "Mode S short with timestamp"
    (is (= {:type :mode-s-short,
            :timestamp [1459 593960052],
            :payload "23FFE7AB7BFCAB"}
           (ads-b/parse-beast-str "@016CE3671C7423FFE7AB7BFCAB;"))))
  (testing "Mode S long with timestamp"
    (is (= {:type :mode-s-long,
            :timestamp [1459 593959592],
            :payload "A800199A8BB80030A8000628F400"}
           (ads-b/parse-beast-str
            "@016CE3671AA8A800199A8BB80030A8000628F400;")))))


(deftest decode-airborne-position-test
  (testing "Decoding airborne position"
    (is (= {:type :airborne-position
            :horizontal-containment-radius-limit 185.2,
            :capabilities 0,
            :nic-supplement-b false,
            :surveillance-status 0,
            :alt 10972.800000000001,
            :baro-alt? true,
            :icao "c0ffee",
            :nic-supplement-a false,
            :cpr-lon 113957,
            :cpr-lat 92249,
            :time-flag false,
            :nic 8,
            :surveillance-status-desc "No condition information",
            :format-type-code 11,
            :cpr-format :odd,
            :downlink-format 17}
         (ads-b/decode-hex "8dc0ffee58b986d0b3bd25000000")))))


(deftest decode-emergency-or-priority-test
  (testing "Emergency or Priority"
    (is (= {:type :extended-squitter-aircraft-status
            :icao "3cbdce",
            :downlink-format 17,
            :capabilities 4,
            :format-type-code 28
            :subtype 1
            :emergency-state-code 0
            :emergency-state-text "no emergency"
            :mode-a-code [7 0 7 3]}
           (ads-b/decode-hex "8d3cbdcee1199400000000000000")))))


(deftest decode-identification-test
  (testing "Identification"
    (is (= {:type :identification
            :icao "3c1ff8",
            :downlink-format 18,
            :capabilities 4,
            :format-type-code 2,
            :emitter-category 0,
            :callsign "        "
            :category-description "No ADS-B Emitter Category Information"}
           (ads-b/decode-hex "903c1ff810820820820820000000")))))


(deftest decode-operational-status-test
  (testing "Operational status"
    (is (= {:position-nac 10.0,
            :capabilities 4,
            :system-design-assurance? 2,
            :barometric-alt-integrity-code true,
            :supports-target-change-report? false,
            :icao "3cbdce",
            :supports-air-referenced-vel? true,
            :nic-supplement-a false,
            :has-active-ident-switch? false,
            :type :aircraft-operational-status,
            :horizontal-reference-dir :true-north,
            :has-tcas-ra? false,
            :uses-single-antenna? false,
            :format-type-code 31,
            :source-integrity-level 3,
            :geometric-vert-accuracy -1,
            :has-uat-in? false,
            :supports-target-state-report? true,
            :version 2,
            :has-1090es-in? false,
            :has-tcas? true,
            :downlink-format 17}
           (ads-b/decode-hex "8d3cbdcef8230002004ab8000000")))))


(deftest decode-surface-position-test
  (testing "Surface position"
    (is (= {:type :surface-position
            :horizontal-containment-radius-limit 25.0,
            :capabilities 4,
            :icao "3c432e",
            :time-flag false,
            :baro-alt? true,
            :ground-speed-resolution 0.257222,
            :nic-supplement 0,
            :nic 10,
            :format-type-code 6,
            :ground-speed 4.372774,
            :downlink-format 18}
           (ads-b/decode-hex "903c432e31aad1686b3806000000")))))


(deftest decode-velocity-over-ground-test
  (testing "Decoding velocity over ground"
    (is (= {:type :airborne-velocity
            :capabilities 0,
            :e-w-spd -195.48872,
            :n-s-spd -104.94657600000001,
            :vertical-spd 0.0,
            :icao "48cb15",
            :supersonic? false,
            :nac 2,
            :ifr? false,
            :geo-minus-baro -182.88,
            :format-type-code 19,
            :ground-speed 221.87749651860187,
            :change-intent? false,
            :barometric-vertical-spd? false,
            :downlink-format 17,
            :heading 61.77122381863042}
           (ads-b/decode-hex "8d48cb1599117d19a00499000000")))))
