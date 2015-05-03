(ns com.lemondronor.ads-b
  "Decodes Mode-S messages."
  (:import (org.opensky.libadsb Decoder tools)
           (org.opensky.libadsb.msgs AirbornePositionMsg AirspeedHeadingMsg
                                     IdentificationMsg SurfacePositionMsg
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


(extend-type AirbornePositionMsg
  IDictable
  (as-dict [msg]
    (letfn [(add-alt [d]
              (assoc-when (.hasAltitude msg)
                d
                :is-baro-alt (.isBarometricAltitude msg)
                :alt (.getAltitude msg)))
            (add-pos [d]
              (assoc-when (.hasPosition msg)
                d
                :cpr-lat (.getCPREncodedLatitude msg)
                :cpr-lon (.getCPREncodedLongitude msg)
                :cpr-format (if (.isOddFormat msg) :odd :even)))]
      (->
       {:icao (tools/toHexString (.getIcao24 msg))
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
       {:icao (tools/toHexString (.getIcao24 msg))
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
       {:icao (tools/toHexString (.getIcao24 msg))
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


(extend-type IdentificationMsg
  IDictable
  (as-dict [msg]
    {:icao (tools/toHexString (.getIcao24 msg))
     :downlink-format (.getDownlinkFormat msg)
     :capabilities (.getCapabilities msg)
     :format-type-code (.getFormatTypeCode msg)
     :emitter-category (.getEmitterCategory msg)
     :category-description (.getCategoryDescription msg)
     :callsign (String. (.getIdentity msg))}))


(extend-type SurfacePositionMsg
  IDictable
  (as-dict [msg]
    (letfn [(add-pos [d]
              (assoc-when (.hasPosition msg)
                d :heading (.getHeading msg)))
            (add-gnd-spd [d]
              (assoc-when (.hasGroundSpeed msg)
                d :1))]
      (->
       {:icao (tools/toHexString (.getIcao24 msg))
        :downlink-format (.getDownlinkFormat msg)
        :capabilities (.getCapabilities msg)
        :format-type-code (.getFormatTypeCode msg)
        :nic-supplement (.getNICSupplement msg)
        :horizontal-containment-radius-limit (.getHorizontalContainmentRadiusLimit msg)
        :nic (.getNavigationIntegrityCategory msg)}))))


(defn decode-hex
  "Decodes a Mode-S message in hex string format."
  [hex-str]
  (-> hex-str
      (Decoder/genericDecoder)
      as-dict))
