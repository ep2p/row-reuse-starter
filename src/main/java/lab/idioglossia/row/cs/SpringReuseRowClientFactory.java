package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.*;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.ws.SpringRowWebsocketClient;
import lab.idioglossia.row.client.ws.SpringRowWebsocketSession;
import lombok.SneakyThrows;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

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
        Assert.notNull(rowClientConfig.getAddress(), "Address cant be null");
        if(rowHttpClient != null) {
            return getRowClient(rowClientConfig, this.rowHttpClient);
        }else {
            return new SpringReuseRowWebsocketClient(reusedClientRegistry, rowClientConfig);
        }
    }

    public RowClient getRowClient(RowClientConfig rowClientConfig, RowHttpClient rowHttpClient){
        return new HttpFallbackRowClientDecorator(new SpringReuseRowWebsocketClient(reusedClientRegistry, rowClientConfig), rowHttpClient);
    }

    public RowClientConfig getRowClientConfig(){
        return RowClientConfigHelper.clone(this.rowClientConfig);
    }

}
