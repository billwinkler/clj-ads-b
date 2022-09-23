(ns readraw
  ""
  (:require [com.lemondronor.ads-b :as ads-b])
  (:gen-class))

(set! *warn-on-reflection* true)


(defn is-mode-a-c? [hex-str]
  (< (count hex-str) 6))


(defn decode [s]
  (if (= (count s) 4)
    "Mode A/C"
    (ads-b/decode-hex s)))


(defn -main [& args]
  (doseq [^String line (line-seq (java.io.BufferedReader. *in*))]
    (pr line "=>")
    (-> line
        ads-b/parse-beast-str
        :payload
        decode
        println)))
