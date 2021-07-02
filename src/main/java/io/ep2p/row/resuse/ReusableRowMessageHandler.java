package io.ep2p.row.resuse;

import io.ep2p.row.client.RowClient;
import io.ep2p.row.client.callback.RowTransportListener;
import io.ep2p.row.client.exception.MessageDataProcessingException;
import io.ep2p.row.client.pipeline.StoppablePipeline;
import io.ep2p.row.client.ConnectionRepository;
import io.ep2p.row.client.RowMessageHandler;
import io.ep2p.row.client.ws.SpringRowWebsocketSession;
import io.ep2p.row.client.ws.WebsocketSession;
import io.ep2p.row.client.ws.handler.MessageHandlerInput;
import io.ep2p.row.server.service.ProtocolService;
import org.springframework.web.socket.TextMessage;

import javax.websocket.CloseReason;

public class ReusableRowMessageHandler extends RowMessageHandler {
    private final ClientToServerWebsocketPort clientToServerWebsocketPort;
    private final ProtocolService protocolService;
    private final StoppablePipeline<MessageHandlerInput, Void> pipeline;
    private final RowTransportListener rowTransportListener;

    public ReusableRowMessageHandler(StoppablePipeline<MessageHandlerInput, Void> pipeline, ConnectionRepository<SpringRowWebsocketSession> connectionRepository, RowTransportListener<SpringRowWebsocketSession> rowTransportListener, RowClient rowClient, ProtocolService protocolService, ClientToServerWebsocketPort clientToServerWebsocketPort) {
        super(pipeline, connectionRepository, rowTransportListener, rowClient);
        this.pipeline = pipeline;
        this.clientToServerWebsocketPort = clientToServerWebsocketPort;
        this.rowTransportListener = rowTransportListener;
        this.protocolService = protocolService;
    }

    @Override
    public void onMessage(WebsocketSession rowWebsocketSession, String text) {
        try {
            MessageHandlerInput messageHandlerInput = new MessageHandlerInput(text);
            this.pipeline.execute(messageHandlerInput, null);

            if(messageHandlerInput.getResponseDto() == null){
                protocolService.handle(clientToServerWebsocketPort.port(rowWebsocketSession),new TextMessage(text));
            }
        } catch (MessageDataProcessingException var4) {
            this.rowTransportListener.onError(rowWebsocketSession, var4);
        }
    }

    @Override
    public void onError(WebsocketSession rowWebsocketSession, Throwable throwable) {
        super.onError(rowWebsocketSession, throwable);
    }

    @Override
    public void onClose(WebsocketSession rowWebsocketSession, CloseReason closeReason) {
        super.onClose(rowWebsocketSession, closeReason);
    }
}
