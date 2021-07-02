package io.ep2p.row.resuse;

import io.ep2p.row.client.ws.RowWebsocketSession;
import io.ep2p.row.client.ws.SpringRowWebsocketSession;
import io.ep2p.row.client.ws.WebsocketSession;
import io.ep2p.row.server.ws.RowServerWebsocket;
import io.ep2p.row.server.ws.SpringRowServerWebsocket;
import org.springframework.web.socket.WebSocketSession;

public interface ClientToServerWebsocketPort {
    RowServerWebsocket port(WebsocketSession websocketSession);

    class Default implements ClientToServerWebsocketPort {
        @Override
        public RowServerWebsocket port(WebsocketSession websocketSession) {
            if(websocketSession instanceof RowWebsocketSession){
                return new TyrusServerWebsocketWrapper(((RowWebsocketSession) websocketSession).getNativeSession());
            }else if(websocketSession instanceof SpringRowWebsocketSession){
                WebSocketSession webSocketSession = ((SpringRowWebsocketSession) websocketSession).getNativeSession();
                return new SpringRowServerWebsocket(webSocketSession);
            }
            return null;
        }
    }
}
