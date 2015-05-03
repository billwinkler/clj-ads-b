(ns com.lemondronor.ads-b
  "Decodes Mode-S messages."
  (:import (org.opensky.libadsb Decoder)))

(set! *warn-on-reflection* true)


(defn decode-hex
  "Decodes a Mode-S message in hex string format."
  [hex-str]
  (Decoder/genericDecoder hex-str))
