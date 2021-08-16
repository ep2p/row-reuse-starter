# row-reuse-starter [![](https://jitpack.io/v/idioglossia/row-client-server-starter.svg)](https://jitpack.io/#idioglossia/row-client-server-starter)
A spring boot starter to run both row server and client and help **reusing websocket connection**.

Sample **application.yml** content to make it work:

```
row:
  cs:
    reuse: true
  enable: true
  ws:
    enable: true
  client:
    enable: true
    type: spring
  handler:
    track-heartbeats: false
```

Basically, using this library, two important things happen:

1. ROW client will add a logic to its message listener which will pass the messages that don't look like a response to a request to the `ProtocolService` of the server.
2. Row server will add a logic to its message listener which will pass the messages that don't look like a request to the client handler. So it's assuming that they are response to a previously sent message.

Before you create a new RowClient, you may want to check if you can find an already persisting connection in `io.ep2p.row.server.repository.RowSessionRegistry()`.
If you could get the `RowServerWebsocket` from the `RowSessionRegistry` then you can easily reuse it as a client socket in your `RowClient` by calling `SpringReuseRowClientFactory.getRowClient()`
