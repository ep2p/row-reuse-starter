package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.RowClient;
import lab.idioglossia.row.client.RowMessageHandlerProvider;
import lab.idioglossia.row.client.http.RowHttpClientHolder;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.tyrus.RowMessageHandler;
import lab.idioglossia.row.client.ws.WebsocketSession;
import lab.idioglossia.row.client.ws.handler.PipelineFactory;
import lab.idioglossia.row.server.config.properties.HandlerProperties;
import lab.idioglossia.row.server.config.properties.WebSocketProperties;
import lab.idioglossia.row.server.repository.RowSessionRegistry;
import lab.idioglossia.row.server.repository.SubscriptionRegistry;
import lab.idioglossia.row.server.service.ProtocolService;
import lab.idioglossia.row.server.ws.RowWsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.socket.WebSocketHandler;

import javax.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(CSProperties.class)
@ConditionalOnProperty(value = "row.enable", havingValue = "true")
public class RowClientServerConfiguration {
    private final ProtocolService protocolService;
    private final RowSessionRegistry rowSessionRegistry;
    private final WebSocketProperties webSocketProperties;
    private final RowWsListener rowWsListener;
    private final SubscriptionRegistry subscriptionRegistry;
    private final HandlerProperties handlerProperties;

    @Autowired
    public RowClientServerConfiguration(ProtocolService protocolService, RowSessionRegistry rowSessionRegistry, WebSocketProperties webSocketProperties, RowWsListener rowWsListener, SubscriptionRegistry subscriptionRegistry, HandlerProperties handlerProperties) {
        this.protocolService = protocolService;
        this.rowSessionRegistry = rowSessionRegistry;
        this.webSocketProperties = webSocketProperties;
        this.rowWsListener = rowWsListener;
        this.subscriptionRegistry = subscriptionRegistry;
        this.handlerProperties = handlerProperties;
    }

    @Bean("reusedClientRegistry")
    public ReusedClientRegistry reusedClientRegistry(){
        return new ReusedClientRegistry();
    }

    @Bean("rowWebSocketHandler")
    @DependsOn("reusedClientRegistry")
    public WebSocketHandler rowWebSocketHandler(ReusedClientRegistry reusedClientRegistry){
        return new ReusableRowWebSocketHandler(rowSessionRegistry, webSocketProperties, rowWsListener, subscriptionRegistry, protocolService, handlerProperties.isTrackHeartbeats(), reusedClientRegistry);
    }

    @Bean("clientToServerWebsocketPort")
    @ConditionalOnMissingBean(ClientToServerWebsocketPort.class)
    public ClientToServerWebsocketPort clientToServerWebsocketPort(){
        return new ClientToServerWebsocketPort.Default();
    }

    @Bean("rowMessageHandlerProvider")
    @DependsOn("clientToServerWebsocketPort")
    public RowMessageHandlerProvider rowMessageHandlerProvider(ClientToServerWebsocketPort clientToServerWebsocketPort){
        return new RowMessageHandlerProvider() {
            @Override
            public RowMessageHandler provide(RowClientConfig rowClientConfig, RowClient rowClient) {
                return new ReusableRowMessageHandler(PipelineFactory.getPipeline(rowClientConfig), rowClientConfig.getConnectionRepository(), rowClientConfig.getRowTransportListener(), rowClient, protocolService, clientToServerWebsocketPort);
            }
        };
    }


    @Bean("reuseRowClientFactory")
    @DependsOn({"rowClientConfig", "rowHttpClientHolder", "reusedClientRegistry"})
    public SpringReuseRowClientFactory reuseRowClientFactory(RowClientConfig<WebsocketSession> rowClientConfig, RowHttpClientHolder rowHttpClientHolder, ReusedClientRegistry reusedClientRegistry){
        return new SpringReuseRowClientFactory(rowClientConfig, rowHttpClientHolder.getRowHttpClient(), reusedClientRegistry);
    }

}
