package lab.idioglossia.row.cs;

import lab.idioglossia.row.server.config.properties.WebSocketProperties;
import lab.idioglossia.row.server.repository.RowSessionRegistry;
import lab.idioglossia.row.server.repository.SubscriptionRegistry;
import lab.idioglossia.row.server.service.ProtocolService;
import lab.idioglossia.row.server.utl.WebsocketSessionUtil;
import lab.idioglossia.row.server.ws.RowWebSocketHandler;
import lab.idioglossia.row.server.ws.RowWsListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.CloseReason;

public class ReusableRowWebSocketHandler extends RowWebSocketHandler {
    private final ReusedClientRegistry reusedClientRegistry;
    private final ProtocolService protocolService;
    private final RowSessionRegistry rowSessionRegistry;

    public ReusableRowWebSocketHandler(RowSessionRegistry rowSessionRegistry, WebSocketProperties webSocketProperties, RowWsListener rowWsListener, SubscriptionRegistry subscriptionRegistry, ProtocolService protocolService, boolean trackHeartbeats, ReusedClientRegistry reusedClientRegistry) {
        super(rowSessionRegistry, webSocketProperties, rowWsListener, subscriptionRegistry, protocolService, trackHeartbeats);
        this.reusedClientRegistry = reusedClientRegistry;
        this.protocolService = protocolService;
        this.rowSessionRegistry = rowSessionRegistry;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        boolean request = this.protocolService.handle(this.rowSessionRegistry.getSession(WebsocketSessionUtil.getUserId(session), session.getId()), message);

        if(!request){
            SpringReuseRowWebsocketClient springReuseRowWebsocketClient = reusedClientRegistry.get(session.getId());
            if(springReuseRowWebsocketClient == null)
                return;
            springReuseRowWebsocketClient.getRowMessageHandler().onMessage(springReuseRowWebsocketClient.getSpringRowWebsocketSession(), message.getPayload());
        }

        this.updateHeartbeat(session);
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);

        SpringReuseRowWebsocketClient springReuseRowWebsocketClient = reusedClientRegistry.get(session.getId());
        if(springReuseRowWebsocketClient == null)
            return;
        springReuseRowWebsocketClient.getRowClientConfig().getRowTransportListener().onError(springReuseRowWebsocketClient.getSpringRowWebsocketSession(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        SpringReuseRowWebsocketClient springReuseRowWebsocketClient = reusedClientRegistry.get(session.getId());
        if(springReuseRowWebsocketClient == null)
            return;
        springReuseRowWebsocketClient.getRowClientConfig().getRowTransportListener().onClose(springReuseRowWebsocketClient, springReuseRowWebsocketClient.getSpringRowWebsocketSession(), new CloseReason(CloseReason.CloseCodes.getCloseCode(status.getCode()), status.getReason()));
    }

}
