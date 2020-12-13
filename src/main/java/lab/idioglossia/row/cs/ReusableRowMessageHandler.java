package lab.idioglossia.row.cs;

import lab.idioglossia.row.client.RowClient;
import lab.idioglossia.row.client.callback.RowTransportListener;
import lab.idioglossia.row.client.exception.MessageDataProcessingException;
import lab.idioglossia.row.client.pipeline.StoppablePipeline;
import lab.idioglossia.row.client.tyrus.ConnectionRepository;
import lab.idioglossia.row.client.tyrus.RowMessageHandler;
import lab.idioglossia.row.client.ws.SpringRowWebsocketSession;
import lab.idioglossia.row.client.ws.WebsocketSession;
import lab.idioglossia.row.client.ws.handler.MessageHandlerInput;
import lab.idioglossia.row.server.service.ProtocolService;
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
