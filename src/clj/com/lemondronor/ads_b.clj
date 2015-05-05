(ns com.lemondronor.ads-b
  "Decodes Mode-S messages. Thin wrapper around java-libadsb."
  (:require [clojure.math.numeric-tower :as math])
  (:import (org.opensky.libadsb Decoder tools)
           (org.opensky.libadsb.exceptions MissingInformationException)
           (org.opensky.libadsb.msgs
            AirbornePositionMsg AirspeedHeadingMsg EmergencyOrPriorityStatusMsg
            IdentificationMsg ModeSReply OperationalStatusMsg SurfacePositionMsg
            VelocityOverGroundMsg)))

(set! *warn-on-reflection* true)


(defmacro assoc-when
  "If test is true, assocs key/values onto m, otherwise returns m."
  [test m & args]
  `(let [m# ~m]
     (if ~test (assoc m# ~@args) m#)))


(defn assoc-when-exists%
  [m k v-fn]
  (try
    (assoc m k (v-fn))
    (catch MissingInformationException e
      m)))


(defmacro assoc-when-exists [m & args]
  `(-> ~m
       ~@(for [[k v] (partition 2 args)]
           `(assoc-when-exists% ~k (fn [] ~v)))))



;; Kinda wishing I had CLOS-style method combination here so I could
;; merge the results of as-map for each applicable type.

(defprotocol IConvertableToMap
  (as-map [msg]))


(extend-type ModeSReply
  IConvertableToMap
  (as-map [msg]
    {:type :mode-s-reply
     :icao (tools/toHexString (.getIcao24 msg))
     :downlink-format (.getDownlinkFormat msg)
     :capabilities (.getCapabilities msg)
     :payload (vec (.getPayload msg))}))


(extend-type AirbornePositionMsg
  IConvertableToMap
  (as-map [msg]
    (letfn [(add-alt [d]
              (assoc-when (.hasAltitude msg)
                d
                :baro-alt? (.isBarometricAltitude msg)
                :alt (.getAltitude msg)))
            (add-pos [d]
              (assoc-when (.hasPosition msg)
                d
                :cpr-lat (.getCPREncodedLatitude msg)
                :cpr-lon (.getCPREncodedLongitude msg)
                :cpr-format (if (.isOddFormat msg) :odd :even)))]
      (->
       {:type :airborne-position
        :icao (tools/toHexString (.getIcao24 msg))
        :downlink-format (.getDownlinkFormat msg)
        :capabilities (.getCapabilities msg)
        :format-type-code (.getFormatTypeCode msg)
        :nic-supplement-a (.getNICSupplementA msg)
        :horizontal-containment-radius-limit (.getHorizontalContainmentRadiusLimit msg)
        :nic (.getNavigationIntegrityCategory msg)
        :surveillance-status (.getSurveillanceStatus msg)
        :surveillance-status-desc (.getSurveillanceStatusDescription msg)
        :nic-supplement-b (.getNICSupplementB msg)
        :time-flag (.getTimeFlag msg)}
       add-alt
       add-pos))))


(extend-type AirspeedHeadingMsg
  IConvertableToMap
  (as-map [msg]
    (letfn [(add-hdg [d]
              (assoc-when (.hasHeadingInfo msg)
                d :heading (.getHeading msg)))
            (add-spd [d]
              (assoc-when (.hasAirspeedInfo msg)
                d :airspeed (.getAirspeed msg)))
            (add-vert-spd [d]
              (assoc-when (.hasVerticalRateInfo msg)
                d :vertical-spd (.getVerticalRate msg)))
            (add-geo-minus-baro [d]
              (assoc-when (.hasGeoMinusBaroInfo msg)
                d :geo-minus-baro (.getGeoMinusBaro msg)))]
      (->
       {:type :airborne-velocity
        :icao (tools/toHexString (.getIcao24 msg))
        :downlink-format (.getDownlinkFormat msg)
        :capabilities (.getCapabilities msg)
        :format-type-code (.getFormatTypeCode msg)
        :supersonic? (.isSupersonic msg)
        :change-intent? (.hasChangeIntent msg)
        :ifr? (.hasIFRCapability msg)
        :nac (.getNavigationAccuracyCategory msg)
        :barometric-vertical-spd? (.isBarometricVerticalSpeed msg)}
       add-hdg
       add-spd
       add-vert-spd
       add-geo-minus-baro))))


(extend-type EmergencyOrPriorityStatusMsg
  IConvertableToMap
  (as-map [msg]
    {:type :extended-squitter-aircraft-status
     :icao (tools/toHexString (.getIcao24 msg))
     :downlink-format (.getDownlinkFormat msg)
     :capabilities (.getCapabilities msg)
     :format-type-code (.getFormatTypeCode msg)
     :subtype (.getSubtype msg)
     :emergency-state-code (.getEmergencyStateCode msg)
     :emergency-state-text (.getEmergencyStateText msg)
     :mode-a-code (vec (.getModeACode msg))}))


(extend-type IdentificationMsg
  IConvertableToMap
  (as-map [msg]
    {:type :identification
     :icao (tools/toHexString (.getIcao24 msg))
     :downlink-format (.getDownlinkFormat msg)
     :capabilities (.getCapabilities msg)
     :format-type-code (.getFormatTypeCode msg)
     :emitter-category (.getEmitterCategory msg)
     :category-description (.getCategoryDescription msg)
     :callsign (String. (.getIdentity msg))}))


(extend-type OperationalStatusMsg
  IConvertableToMap
  (as-map [msg]
    (-> {:type :aircraft-operational-status
         :icao (tools/toHexString (.getIcao24 msg))
         :downlink-format (.getDownlinkFormat msg)
         :capabilities (.getCapabilities msg)
         :format-type-code (.getFormatTypeCode msg)
         :version (.getVersion msg)
         :nic-supplement-a (.getNICSupplementA msg)
         :position-nac (.getPositionNAC msg)
         :geometric-vert-accuracy (.getGeometricVerticalAccuracy msg)
         :source-integrity-level (.getSourceIntegrityLevel msg)}
        (assoc-when-exists
         :has-tcas? (.hasOperationalTCAS msg)
         :has-1090es-in? (.has1090ESIn msg)
         :supports-air-referenced-vel? (.supportsAirReferencedVelocity msg)
         :has-low-tx-power? (.hasLowTxPower msg)
         :supports-target-state-report? (.supportsTargetStateReport msg)
         :supports-target-change-report? (.supportsTargetChangeReport msg)
         :has-uat-in? (.hasUATIn msg)
         :nac-v (.getNACV msg)
         :nic-supplement-c (.getNICSupplementC msg)
         :has-tcas-ra? (.hasTCASResolutionAdvisory msg)
         :has-active-ident-switch? (.hasActiveIDENTSwitch msg)
         :uses-single-antenna? (.usesSingleAntenna msg)
         :system-design-assurance? (.getSystemDesignAssurance msg)
         :gps-antenna-offset (.getGPSAntennaOffset msg)
         :airplane-length (.getAirplaneLength msg)
         :airplane-width (.getAirplaneWidth msg)
         :barometric-alt-integrity-code (.getBarometricAltitudeIntegrityCode msg)
         :track-heading-info (.getTrackHeadingInfo msg)
         :horizontal-reference-dir (if (.getHorizontalReferenceDirection msg)
                                     :magnetic-north
                                     :true-north)))))


(extend-type SurfacePositionMsg
  IConvertableToMap
  (as-map [msg]
    (letfn [(add-pos [d]
              (assoc-when (.hasPosition msg)
                d
                :cpr-lat (.getCPREncodedLatitude msg)
                :cpr-lon (.getCPREncodedLongitude msg)
                :cpr-format (if (.isOddFormat msg) :odd :even)))
            (add-hdg [d]
              (assoc-when (.hasValidHeading msg)
                          d (:heading (.getHeading msg))))
            (add-gnd-spd [d]
              (assoc-when (.hasGroundSpeed msg)
                d
                :ground-speed (.getGroundSpeed msg)
                :ground-speed-resolution (.getGroundSpeedResolution msg)))]
      (->
       {:type :surface-position
        :icao (tools/toHexString (.getIcao24 msg))
        :downlink-format (.getDownlinkFormat msg)
        :capabilities (.getCapabilities msg)
        :format-type-code (.getFormatTypeCode msg)
        :nic-supplement (.getNICSupplement msg)
        :horizontal-containment-radius-limit (.getHorizontalContainmentRadiusLimit msg)
        :nic (.getNavigationIntegrityCategory msg)
        :time-flag (.isTime_flag msg)
        :baro-alt? (.isBarometricAltitude msg)}
       add-gnd-spd))))


(extend-type VelocityOverGroundMsg
  IConvertableToMap
  (as-map [msg]
    (letfn [(add-vel [d]
              (assoc-when (.hasVelocityInfo msg)
                d
                :heading (.getHeading msg)
                :ground-speed (.getVelocity msg)
                :e-w-spd (.getEastToWestVelocity msg)
                :n-s-spd (.getNorthToSouthVelocity msg)))
            (add-vert-spd [d]
              (assoc-when (.hasVerticalRateInfo msg)
                d :vertical-spd (.getVerticalRate msg)))
            (add-geo-minus-baro [d]
              (assoc-when (.hasGeoMinusBaroInfo msg)
                d :geo-minus-baro (.getGeoMinusBaro msg)))]
      (->
       {:type :airborne-velocity
        :icao (tools/toHexString (.getIcao24 msg))
        :downlink-format (.getDownlinkFormat msg)
        :capabilities (.getCapabilities msg)
        :format-type-code (.getFormatTypeCode msg)
        :supersonic? (.isSupersonic msg)
        :change-intent? (.hasChangeIntent msg)
        :ifr? (.hasIFRCapability msg)
        :nac (.getNavigationAccuracyCategory msg)
        :barometric-vertical-spd? (.isBarometricVerticalSpeed msg)}
       add-vel
       add-vert-spd
       add-geo-minus-baro))))


(defn decode-hex
  "Decodes a Mode-S message in hex string format."
  [hex-str]
  (-> hex-str
      (Decoder/genericDecoder)
      as-map))



(defn read-hex-str [^String s]
  (let [n (count s)
        ^ints b (int-array (/ n 2))]
    (doseq [i (range 0 n 2)]
      (println i)
      (aset b
            (/ i 2)
            (int (+ (bit-shift-left (Character/digit (.charAt s i) 16) 4)
                    (Character/digit (.charAt s (inc i)) 16)))))
    b))


(defn parse-mode-s-beast-timestamp [hex-str]
  (let [^ints t (read-hex-str hex-str)
        nanosec (bit-or (bit-shift-left (bit-and (aget t 2) 0x3f) 24)
                        (bit-shift-left (aget t 3) 16)
                        (bit-shift-left (aget t 4) 8)
                        (aget t 5))
        daysec (bit-or (bit-shift-left (aget t 0) 10)
                       (bit-shift-left (aget t 1) 2)
                       (bit-shift-right (aget t 2) 6))]
    [daysec nanosec]))


(defn cpr-mod-function [a b]
  (let [res (mod a b)]
    (if (neg? res)
      (+ res b)
      res)))


(def cpr-nl-table
  [[10.47047130 59]
   [14.82817437 58]
   [18.18626357 57]
   [21.02939493 56]
   [23.54504487 55]
   [25.82924707 54]
   [27.93898710 53]
   [29.91135686 52]
   [31.77209708 51]
   [33.53993436 50]
   [35.22899598 49]
   [36.85025108 48]
   [38.41241892 47]
   [39.92256684 46]
   [41.38651832 45]
   [42.80914012 44]
   [44.19454951 43]
   [45.54626723 42]
   [46.86733252 41]
   [48.16039128 40]
   [49.42776439 39]
   [50.67150166 38]
   [51.89342469 37]
   [53.09516153 36]
   [54.27817472 35]
   [55.44378444 34]
   [56.59318756 33]
   [57.72747354 32]
   [58.84763776 31]
   [59.95459277 30]
   [61.04917774 29]
   [62.13216659 28]
   [63.20427479 27]
   [64.26616523 26]
   [65.31845310 25]
   [66.36171008 24]
   [67.39646774 23]
   [68.42322022 22]
   [69.44242631 21]
   [70.45451075 20]
   [71.45986473 19]
   [72.45884545 18]
   [73.45177442 17]
   [74.43893416 16]
   [75.42056257 15]
   [76.39684391 14]
   [77.36789461 13]
   [78.33374083 12]
   [79.29428225 11]
   [80.24923213 10]
   [81.19801349 9]
   [82.13956981 8]
   [83.07199445 7]
   [83.99173563 6]
   [84.89166191 5]
   [85.75541621 4]
   [86.53536998 3]
   [87.00000000 2]])


(defn cpr-nl-function [lat]
  (if (neg? lat)
    (cpr-nl-function (- lat))
    (loop [v cpr-nl-table]
      (if (seq v)
        (let [[nl-lat nl] (first v)]
          (if (< lat nl-lat)
            nl
            (recur (rest v))))
        1))))


(defn cpr-n-function [lat fflag]
  (let [nl (- (cpr-nl-function lat)
              (if (= fflag :odd) 1 0))]
    (if (< nl 1)
      1
      nl)))


(defn cpr-dlon-function [lat fflag surface?]
  (/ (if surface? 90.0 360.0)
     (cpr-n-function lat fflag)))


(defn sort-even-odd [m1 m2]
  (let [g (group-by :cpr-format [m1 m2])]
    (assert (and (= (count (:even g)) 1)
                 (= (count (:odd g)) 1))
            (str "Need one message with each :cpr-format, :odd and :even: "
                 m1 " " m2))
    [(first (:even g)) (first (:odd g))]))


;; (defn decode-cpr [m1 m2 opts]
;;   (let [[m0 m1] (sort-even-odd m1 m2)
;;         mult (if (= (:type m0) :surface-position)
;;                90.0
;;                360.0)
;;         air-dlat0 (/ mult 60.0)
;;         air-dlat1 (/ mult 59.0)
;;         lat0 (:cpr-lat m0)
;;         lat1 (:cpr-lat m1)
;;         lon0 (:cpr-lon m0)
;;         lon1 (:cpr-lon m1)
;;         ;; Compute altitude index j
;;         j (int (math/floor (+ (/ (- (* 59 lat0) (* 60 lat1)) 131072) 0.5)))
;;         rlat0 (* air-dlat0 (+ (cpr-mod-function j 60) (/ lat0 131072)))
;;         rlat1 (* air-dlat1 (+ (cpr-mod-function j 59) (/ lat1 131072)))
;;         [surface-rlat surface-rlon] (if (= (:type m0) :surface-position)

