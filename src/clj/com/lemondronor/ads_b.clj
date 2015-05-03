(ns com.lemondronor.ads-b
  "Decodes Mode-S messages."
  (:import (org.opensky.libadsb Decoder tools)
           (org.opensky.libadsb.exceptions MissingInformationException)
           (org.opensky.libadsb.msgs
            AirbornePositionMsg AirspeedHeadingMsg EmergencyOrPriorityStatusMsg
            IdentificationMsg OperationalStatusMsg SurfacePositionMsg
            VelocityOverGroundMsg)))

(set! *warn-on-reflection* true)


(defprotocol IDictable
  (as-dict [msg]))


(defn hexify [s]
  (apply str
         (map #(format "%02x" (int %)) s)))


(defmacro assoc-when [test m & args]
  `(let [m# ~m]
     (if ~test (assoc m# ~@args) m#)))




(defn assoc-when-exists% [m k v-fn]
  (try
    (assoc m k (v-fn))
    (catch MissingInformationException e
      m)))


(defmacro assoc-when-exists [m & args]
  `(-> ~m
       ~@(for [[k v] (partition 2 args)]
           `(assoc-when-exists% ~k (fn [] ~v)))))


(extend-type AirbornePositionMsg
  IDictable
  (as-dict [msg]
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
  IDictable
  (as-dict [msg]
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
  IDictable
  (as-dict [msg]
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
  IDictable
  (as-dict [msg]
    {:type :identification
     :icao (tools/toHexString (.getIcao24 msg))
     :downlink-format (.getDownlinkFormat msg)
     :capabilities (.getCapabilities msg)
     :format-type-code (.getFormatTypeCode msg)
     :emitter-category (.getEmitterCategory msg)
     :category-description (.getCategoryDescription msg)
     :callsign (String. (.getIdentity msg))}))


(extend-type OperationalStatusMsg
  IDictable
  (as-dict [msg]
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
  IDictable
  (as-dict [msg]
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
  IDictable
  (as-dict [msg]
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
      as-dict))
