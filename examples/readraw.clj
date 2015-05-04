(ns readraw
  ""
  (:require [com.lemondronor.ads-b :as ads-b]))

(set! *warn-on-reflection* true)


(defn is-mode-a-c? [hex-str]
  (< (count hex-str) 6))


(defn -main [& args]
  (doseq [^String line (line-seq (java.io.BufferedReader. *in*))]
    (let [hex-str (if (.startsWith line "*")
                    (subs line 1 (- (count line) 1))
                    (subs line 13 (- (count line) 1)))]
      (pr hex-str "=>")
      (if (>= (count hex-str) 6)
        (println (ads-b/decode-hex hex-str))
        (println "Mode A/C")))))
