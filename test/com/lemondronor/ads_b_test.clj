(ns com.lemondronor.ads-b-test
  (:require [clojure.pprint :as pprint]
            [clojure.test :refer :all]
            [com.lemondronor.ads-b :as ads-b]))


(deftest decode-airborne-position-test
  (testing "Decoding airborne position"
    (is (= {:horizontal-containment-radius-limit 185.2,
            :capabilities 0,
            :nic-supplement-b false,
            :surveillance-status 0,
            :alt 10972.800000000001,
            :is-baro-alt true,
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


(deftest decode-velocity-over-ground-test
  (testing "Decoding velocity over ground"
    (is (= {:capabilities 0,
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


(deftest decode-identification-test
  (testing "Identification"
    (is (= {:icao "3c1ff8",
            :downlink-format 18,
            :capabilities 4,
            :format-type-code 2,
            :emitter-category 0,
            :callsign "        "
            :category-description "No ADS-B Emitter Category Information"}
           (ads-b/decode-hex "903c1ff810820820820820000000")))))
