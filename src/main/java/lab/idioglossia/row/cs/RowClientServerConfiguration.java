package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.RowClientFactory;
import lab.idioglossia.row.client.http.RowHttpClientHolder;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.ws.WebsocketSession;
import lab.idioglossia.row.server.config.properties.WebSocketProperties;
import lab.idioglossia.row.server.config.RowConfiguration;
import lab.idioglossia.row.server.config.properties.HandlerProperties;
import lab.idioglossia.row.server.filter.RowFilterChain;
import lab.idioglossia.row.server.repository.RowSessionRegistry;
import lab.idioglossia.row.server.repository.SubscriptionRegistry;
import lab.idioglossia.row.server.service.ProtocolService;
import lab.idioglossia.row.server.ws.RowWebSocketHandler;
import lab.idioglossia.row.server.ws.RowWsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.web.socket.WebSocketHandler;

@Configuration
@AutoConfigureAfter(RowConfiguration.class)
@EnableConfigurationProperties(CSProperties.class)
@ConditionalOnProperty(value = "row.enable", havingValue = "true")
public class RowClientServerConfiguration {
    private final CSProperties csProperties;
    private final WebSocketProperties webSocketProperties;
    private final HandlerProperties handlerProperties;
    private final RowSessionRegistry rowSessionRegistry;
    private final ProtocolService protocolService;
    private final RowWsListener rowWsListener;
    private final SubscriptionRegistry subscriptionRegistry;

    @Autowired
    public RowClientServerConfiguration(CSProperties csProperties, WebSocketProperties webSocketProperties, HandlerProperties handlerProperties, RowSessionRegistry rowSessionRegistry, ProtocolService protocolService, RowWsListener rowWsListener, SubscriptionRegistry subscriptionRegistry) {
        this.csProperties = csProperties;
        this.webSocketProperties = webSocketProperties;
        this.handlerProperties = handlerProperties;
        this.rowSessionRegistry = rowSessionRegistry;
        this.protocolService = protocolService;
        this.rowWsListener = rowWsListener;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    @Bean
    public ReusedClientRegistry reusedClientRegistry(){
        return new ReusedClientRegistry();
    }

    @Bean("rowWebSocketHandler")
    @Primary
    @DependsOn({"reusedClientRegistry"})
    public WebSocketHandler rowWebSocketHandler(ReusedClientRegistry reusedClientRegistry){
        if(!csProperties.isReuse())
            return new RowWebSocketHandler(rowSessionRegistry, webSocketProperties, rowWsListener, subscriptionRegistry, protocolService, handlerProperties.isTrackHeartbeats());
        else
            return new ReusableRowWebSocketHandler(rowSessionRegistry, webSocketProperties, rowWsListener, subscriptionRegistry, protocolService, handlerProperties.isTrackHeartbeats(), reusedClientRegistry);
    }

    @Bean("rowClientFactory")
    @Primary
    @DependsOn({"rowClientConfig", "rowHttpClientHolder", "reusedClientRegistry"})
    public RowClientFactory rowClientFactory(RowClientConfig<WebsocketSession> rowClientConfig, RowHttpClientHolder rowHttpClientHolder, ReusedClientRegistry reusedClientRegistry){
        return new SpringReuseRowClientFactory(rowClientConfig, rowHttpClientHolder.getRowHttpClient(), reusedClientRegistry);
    }

}
