/**
 * Thrift definition file for bloom-server.
 * Version: 0.1.0
 */

namespace java com.github.btnguyen2k.bloomserver.thrift

struct TBloomResponse {
    1: i32 status,
    2: bool value,
    3: string message
}

service TBloomService {
    /**
     * "Ping" the server. This method is to test if server is reachable.
     */
    oneway void ping(),
    
    /**
     * "Ping" the server. This method is to test if server is reachable.
     */
    bool ping2(),
    
    /**
     * Puts an item to a bloom filter specified by a name.
     * 
     * @param _bloomName
     * @param _item
     * @return
     */
    TBloomResponse put(1: string _bloomName, 2: string _item),
    
    /**
      * Tests if an item exists in a specified bloom filter.
      *
      * @param _bloomName
      * @param _item
      * @return
      */
    TBloomResponse mightContain(1: string _bloomName, 2: string _item),
    
    /**
     * Creates and Initializes a new bloom filter.
     *
     * @param _secret
     * @param _bloomName
     * @param _numItems
     * @param _expectedFpp
     * @param _force set to {@code true} to force overriding existing bloom filter
     * @param _counting (not supported yet)
     * @param _scaling (not supported yet)
     */
    TBloomResponse initBloom(1: string _secret, 2: string _bloomName, 3: i64 _numItems, 4: double _expectedFpp, 5: bool _force, 6: bool _counting, 7: bool _scaling),
}
