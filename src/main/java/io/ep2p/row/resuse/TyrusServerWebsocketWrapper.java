package io.ep2p.row.resuse;

import io.ep2p.row.server.ws.RowServerWebsocket;
import org.springframework.web.socket.CloseStatus;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

public class TyrusServerWebsocketWrapper implements RowServerWebsocket<Session> {
    private final Session session;

    public TyrusServerWebsocketWrapper(Session session) {
        this.session = session;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return session.getUserProperties();
    }

    @Override
    public Object getNativeSession() {
        return session;
    }

    @Override
    public <T> T getNativeSession(Class<T> var1) {
        return (T) session;
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public URI getUri() {
        return session.getRequestURI();
    }

    @Override
    public void close(CloseStatus closeStatus) throws IOException {
        CloseReason.CloseCode closeCode = CloseReason.CloseCodes.getCloseCode(closeStatus.getCode());
        session.close(new CloseReason(closeCode, ""));
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    public boolean isSecure() {
        return session.isSecure();
    }

    @Override
    public void sendTextMessage(String payload) throws IOException {
        session.getBasicRemote().sendText(payload);
    }

    @Override
    public void sendPingMessage(ByteBuffer byteBuffer) throws IOException {
        session.getAsyncRemote().sendPing(byteBuffer);
    }

    @Override
    public void sendPongMessage(ByteBuffer byteBuffer) throws IOException {
        session.getAsyncRemote().sendPong(byteBuffer);
    }

    @Override
    public void closeInternal(CloseStatus closeStatus) throws IOException {
        CloseReason.CloseCode closeCode = CloseReason.CloseCodes.getCloseCode(closeStatus.getCode());
        session.close(new CloseReason(closeCode, ""));
    }
}
