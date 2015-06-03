Bloom Server
============

Bloom Filter Server - by btnguyen2k.

Project home: [https://github.com/btnguyen2k/bloom-server](https://github.com/btnguyen2k/bloom-server).

## Overview ##

Bloom Server provides REST and Thrift APIs to access centralized [bloom filter](http://en.wikipedia.org/wiki/Bloom_filter) data.


## Release-notes ##

Latest release: `0.1.0`.

See [RELEASE-NOTES.md](RELEASE-NOTES.md).


## APIs ##

* `put(item[, bloomName])`: put an item to a bloom filter.
* `mightContain(item[, bloomname])`: test if an item has been put to a bloom filter.
* `initBloom(bloomName, force, expectedNumItems, expectedFpp)`: create and initialize a bloom filter.

### REST APIs ###

* `GET /put/<item>[/bloomName]`
  - Put an item to a bloom filter.
  - `item (String)`: the item to be put to the bloom filter
  - `bloomName (String)`: (optional) name of the bloom filter to put the item to
  - Output: JSON `{"s":500,"v":false,"m":"Error message: exception occurred at server side"}`
  - Output: JSON `{"s":404,"v":false,"m":"Error message: the specified bloom filter does not exist"}`
  - Output: JSON `{"s":200,"v":true,"m":"Successful: bloom filter data is updated"}`
  - Output: JSON `{"s":200,"v":false,"m":"Successful: bloom filter has not changed, the item might have been put to the bloom filter before"}`

* `GET /mightContain/<item>[/bloomName]`
  - Test if an item has been put to a bloom filter.
  - `item (String)`: the item to be tested
  - `bloomName (String)`: (optional) name of the bloom filter to test
  - Output: JSON `{"s":500,"v":false,"m":"Error message: exception occurred at server side"}`
  - Output: JSON `{"s":404,"v":false,"m":"Error message: the specified bloom filter does not exist"}`
  - Output: JSON `{"s":200,"v":false,"m":"Successful: the item has definitely not put to the bloom filter"}`
  - Output: JSON `{"s":200,"v":true,"m":"Successful: the item might has been put to the bloom filter"}`
  
* `POST /initBloom`
  - Creates and Initializes a new bloom filter.
  - Input: (JSON) `{"bloom_name":"(string) name of the bloom filter to create/init", "num_items":(int) number of expected items, "expected_fpp": (double, in range [0,1]) expected false-positive-probability, "force": (boolean) set to true to force override existing bloom filter, false to ignore if the bloom filter already existed, "secret": "(string) secret key to authorize the request"}`
  - Output: JSON `{"s":500,"v":false,"m":"Error message: exception occurred at server side"}`
  - Output: JSON `{"s":403,"v":false,"m":"Error message: unauthorized request (wrong secret code)"}`
  - Output: JSON `{"s":403,"v":false,"m":"Error message: unauthorized request (wrong secret code)"}`
  - Output: JSON `{"s":200,"v":true,"m":"Successful: new bloom filter has been created and initialized"}`
  - Output: JSON `{"s":200,"v":false,"m":"Successful: the bloom filter has already existed"}`

### Thrift APIs ###

See file [bloomserver.thrift](thrift/bloomserver.thrift).

### Thrift-over-HTTP APIs ###

Thrift APIs, but over HTTP(s)! URL: `http://server:host/thrift`

### Clients ###

- Java: [https://github.com/btnguyen2k/bloom-jclient](https://github.com/btnguyen2k/bloom-jclient).


## Installation ##

Note: Java 7+ is required!

### Install from binary ###

- Download binary package from [project release site](https://github.com/btnguyen2k/bloom-server/releases).
- Unzip the binary package and copy it to your favourite location, e.g. `/usr/local/bloom-server`.


### Install from source ###

- Download application's source, either cloning github project or download the source package from [project release site](https://github.com/btnguyen2k/bloom-server/releases).
- Build [Play! Framework](https://www.playframework.com): `play dist`.
- The built binary package is available at `target/universal/bloom-server-<version>.zip`. You may copy it to your favourite location, e.g. `/usr/local/bloom-server`.


## Start/Stop Bloom-Server ##

### Linux ###

Start server with default options:
> `/usr/local/bloom-server/conf/server-production.sh start`

Start server with 1024M memory limit, REST & Thrift-over-HTTP APIs on port 18080, Thrift APIs on port 19090
> `/usr/local/bloom-server/conf/server-production.sh start -m 1024 -p 18080 -t 19090`
Set thrift port to 0 (`-t 0`) to disable Thrift APIs.

Default port for REST & Thrift-over-HTTP requests is `8080` and `9090` is the default port for Thrift requests (both port numbers are configurable).

Stop server:
> `/usr/local/bloom-server/conf/server-production.sh stop`


## Configurations ##

See file(s) `conf/application.conf` and `conf/spring/beans.xml`.


## License ##

See [LICENSE.txt](LICENSE.txt) for details. Copyright (c) 2015 btnguyen2k.

Third party libraries are distributed under their own license(s).
