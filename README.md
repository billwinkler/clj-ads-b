# ads-b

A Clojure wrapper for
[java-libadsb](https://github.com/openskynetwork/java-adsb), which
parses Mode S/ADS-B transponder messages. (See also:
[dump1090](https://github.com/MalcolmRobb/dump1090),
[gr-air-modes](https://github.com/bistromath/gr-air-modes), etc.)

## Caveats

java-libadsb only decodes a few types of Mode S messages, and no Mode
A/C messages.

Civil aviation uses Mode S messages with types DF0, DF4, DF5, DF11,
DF16, DF17, DF18, DF20, DF21 and DF24; java-libadsb only decodes some
subtypes of DF17 and DF18.


## Usage

```
[com.lemondronor/ads-b "0.1.4"]
```

```
(require '[com.lemondronor.ads-b :as ads-b])

(ads-b/decode-hex "8dc0ffee58b986d0b3bd25000000")
=>
{:icao "c0ffee",
 :type :airborne-position
 :downlink-format 17
 :format-type-code 11,
 :capabilities 0,
 :horizontal-containment-radius-limit 185.2,
 :nic-supplement-b false,
 :surveillance-status 0,
 :alt 10972.800000000001,
 :baro-alt? true,
 :nic-supplement-a false,
 :cpr-lon 113957,
 :cpr-lat 92249,
 :time-flag false,
 :nic 8,
 :surveillance-status-desc "No condition information",
 :cpr-format :odd,
}
```

There are 6 types of message:

```
:airborne-position
:airborne-velocity
:extended-squitter-aircraft-status
:identification
:operational-aircraft-status
:surface-position
```

Run examples/readraw from target directory (dump1090 executable in peer directory)

``` shell
../../dump1090/dump1090 --raw | java -jar readraw.jar
```

## License

Copyright © 2015 John Wiseman

Distributed under the GNU Generap Public License version 3.
