package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.RowClient;
import lab.idioglossia.row.client.callback.ResponseCallback;
import lab.idioglossia.row.client.callback.SubscriptionListener;
import lab.idioglossia.row.client.model.RowRequest;
import lab.idioglossia.row.client.tyrus.RequestSender;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.tyrus.RowMessageHandler;
import lab.idioglossia.row.client.ws.SpringRowWebsocketSession;
import lab.idioglossia.row.client.ws.handler.PipelineFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class SpringReuseRowWebsocketClient implements RowClient {
    private final RequestSender requestSender;
    private final ReusedClientRegistry reusedClientRegistry;
    @Getter
    private RowMessageHandler<SpringRowWebsocketSession> rowMessageHandler;
    @Getter
    private final RowClientConfig<SpringRowWebsocketSession> rowClientConfig;
    @Getter
    private SpringRowWebsocketSession springRowWebsocketSession;

    public SpringReuseRowWebsocketClient(ReusedClientRegistry reusedClientRegistry, RowMessageHandler<SpringRowWebsocketSession> rowMessageHandler, RowClientConfig<SpringRowWebsocketSession> rowClientConfig) {
        this.reusedClientRegistry = reusedClientRegistry;
        this.rowMessageHandler = rowMessageHandler;
        this.rowClientConfig = rowClientConfig;
        this.requestSender = new RequestSender(rowClientConfig.getConnectionRepository(), rowClientConfig.getMessageIdGenerator(), rowClientConfig.getCallbackRegistry(), rowClientConfig.getSubscriptionListenerRegistry(), rowClientConfig.getMessageConverter());
    }

    @Override
    public void sendRequest(RowRequest<?, ?> rowRequest, ResponseCallback<?> responseCallback) throws IOException {
        requestSender.sendRequest(rowRequest, responseCallback);
    }

    @Override
    public void subscribe(RowRequest<?, ?> rowRequest, ResponseCallback<?> responseCallback, SubscriptionListener<?> subscriptionListener) throws IOException {
        requestSender.sendSubscribe(rowRequest, responseCallback, subscriptionListener);
    }

    public synchronized void reuse(WebSocketSession webSocketSession){
        this.springRowWebsocketSession = new SpringRowWebsocketSession(rowClientConfig.getAttributes(), webSocketSession.getUri(), rowClientConfig.getWebsocketConfig());
        this.springRowWebsocketSession.setNativeSession(webSocketSession);
        reusedClientRegistry.register(webSocketSession.getId(), this);
        rowClientConfig.getConnectionRepository().setConnection(this.springRowWebsocketSession);
        this.rowMessageHandler = new RowMessageHandler<SpringRowWebsocketSession>(PipelineFactory.getPipeline(this.rowClientConfig), rowClientConfig.getConnectionRepository(), rowClientConfig.getRowTransportListener(), this);
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
