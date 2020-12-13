package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.RowClient;
import lab.idioglossia.row.client.callback.ResponseCallback;
import lab.idioglossia.row.client.callback.SubscriptionListener;
import lab.idioglossia.row.client.model.RowRequest;
import lab.idioglossia.row.client.tyrus.ConnectionRepository;
import lab.idioglossia.row.client.tyrus.RequestSender;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.tyrus.RowMessageHandler;
import lab.idioglossia.row.client.ws.SpringRowWebsocketSession;
import lab.idioglossia.row.server.ws.RowServerWebsocket;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class SpringReuseRowWebsocketClient implements RowClient {
    private RequestSender requestSender;
    private final ReusedClientRegistry reusedClientRegistry;
    @Getter
    private RowMessageHandler<SpringRowWebsocketSession> rowMessageHandler;
    @Getter
    private final RowClientConfig<SpringRowWebsocketSession> rowClientConfig;
    @Getter
    private SpringRowWebsocketSession springRowWebsocketSession;

    public SpringReuseRowWebsocketClient(ReusedClientRegistry reusedClientRegistry, RowClientConfig<SpringRowWebsocketSession> rowClientConfig) {
        this.reusedClientRegistry = reusedClientRegistry;
        this.rowClientConfig = rowClientConfig;
    }

    @Override
    public void sendRequest(RowRequest<?, ?> rowRequest, ResponseCallback<?> responseCallback) throws IOException {
        requestSender.sendRequest(rowRequest, responseCallback);
    }

    @Override
    public void subscribe(RowRequest<?, ?> rowRequest, ResponseCallback<?> responseCallback, SubscriptionListener<?> subscriptionListener) throws IOException {
        requestSender.sendSubscribe(rowRequest, responseCallback, subscriptionListener);
    }

    public synchronized void reuse(RowServerWebsocket<WebSocketSession> rowServerWebsocket){
        this.springRowWebsocketSession = new SpringRowWebsocketSession(rowClientConfig.getAttributes(), rowServerWebsocket.getUri(), rowClientConfig.getWebsocketConfig());
        WebSocketSession nativeSession = rowServerWebsocket.getNativeSession(WebSocketSession.class);
        this.springRowWebsocketSession.setNativeSession(nativeSession);
        reusedClientRegistry.register(rowServerWebsocket.getId(), this);
        rowClientConfig.getConnectionRepository().setConnection(this.springRowWebsocketSession);
        this.rowMessageHandler = rowClientConfig.getRowMessageHandlerProvider().provide(rowClientConfig, this);
        this.requestSender = new RequestSender(rowClientConfig.getConnectionRepository(), rowClientConfig.getMessageIdGenerator(), rowClientConfig.getCallbackRegistry(), rowClientConfig.getSubscriptionListenerRegistry(), rowClientConfig.getMessageConverter());
    }

    @SneakyThrows
    @Override
    public void open() {
        throw new IllegalAccessException("Shouldn't call open() on SpringReuseRowWebsocketClient. Use reuse().");
    }

    @SneakyThrows
    @Override
    public void close() {
        if(springRowWebsocketSession != null){
            springRowWebsocketSession.close();
        }
    }
}
