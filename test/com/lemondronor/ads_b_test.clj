(ns com.lemondronor.ads-b-test
  (:require [clojure.test :refer :all]
            [com.lemondronor.ads-b :as ads-b]))

(deftest decode-hex-test
  (testing "Decoding hex string"
    (let [m (ads-b/decode-hex "8dc0ffee58b986d0b3bd25000000")]
      (println m))))
