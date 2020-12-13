package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.ws.RowWebsocketSession;
import lab.idioglossia.row.client.ws.SpringRowWebsocketSession;
import lab.idioglossia.row.client.ws.WebsocketSession;
import lab.idioglossia.row.server.ws.RowServerWebsocket;
import lab.idioglossia.row.server.ws.SpringRowServerWebsocket;
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
