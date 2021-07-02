package io.ep2p.row.resuse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReusedClientRegistry {
    private final Map<String, SpringReuseRowWebsocketClient> registryMap = new ConcurrentHashMap<>();

    public void register(String id, SpringReuseRowWebsocketClient springReuseRowWebsocketClient){
        registryMap.putIfAbsent(id, springReuseRowWebsocketClient);
    }

    public SpringReuseRowWebsocketClient get(String id){
        return registryMap.get(id);
    }

    public void unregister(String id){
        registryMap.remove(id);
    }
}
