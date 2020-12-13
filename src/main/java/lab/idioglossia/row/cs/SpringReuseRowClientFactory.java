package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.*;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.ws.SpringRowWebsocketSession;
import lab.idioglossia.row.server.service.ProtocolService;
import lab.idioglossia.row.server.ws.SpringRowServerWebsocket;
import lombok.SneakyThrows;
import org.springframework.lang.Nullable;

public class SpringReuseRowClientFactory implements RowClientFactory<SpringRowWebsocketSession> {
    private RowClientConfig rowClientConfig;
    private RowHttpClient rowHttpClient;
    private final ReusedClientRegistry reusedClientRegistry;

    public SpringReuseRowClientFactory(RowClientConfig rowClientConfig, @Nullable RowHttpClient rowHttpClient, ReusedClientRegistry reusedClientRegistry) {
        this.rowClientConfig = rowClientConfig;
        this.rowHttpClient = rowHttpClient;
        this.reusedClientRegistry = reusedClientRegistry;
    }

    @SneakyThrows
    public synchronized RowClient getRowClient(String address){
        throw new IllegalAccessException("Reusable ROW Client Factory doesnt work with address input");
    }

    public RowClient getRowClient(RowClientConfig rowClientConfig){
        if(rowHttpClient != null) {
            return getRowClient(rowClientConfig, this.rowHttpClient);
        }else {
            return new SpringReuseRowWebsocketClient(reusedClientRegistry, rowClientConfig);
        }
    }

    public RowClient getRowClient(RowClientConfig rowClientConfig, RowHttpClient rowHttpClient){
        return new HttpFallbackRowClientDecorator(new SpringReuseRowWebsocketClient(reusedClientRegistry, rowClientConfig), rowHttpClient);
    }

    public RowClient getRowClient(RowClientConfig rowClientConfig, SpringRowServerWebsocket rowWebsocketSession){
        SpringReuseRowWebsocketClient springReuseRowWebsocketClient = new SpringReuseRowWebsocketClient(reusedClientRegistry, rowClientConfig);
        springReuseRowWebsocketClient.reuse(rowWebsocketSession);
        return springReuseRowWebsocketClient;
    }

    public RowClientConfig getRowClientConfig(){
        RowClientConfig clone = RowClientConfigHelper.clone(this.rowClientConfig);
        //todo: remove this line on newer version from client-starter
        clone.setRowMessageHandlerProvider(this.rowClientConfig.getRowMessageHandlerProvider());
        return clone;
    }

}
