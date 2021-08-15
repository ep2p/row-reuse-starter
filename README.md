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

What you should consider adding to your code is to create a map of unique identifier of each node/server to their server-websocket-session after you identify the request sender.
Then later if you need to create a client socket to that particular server, you can check this map.

if the value is not in the map, you may want to create a new row-websocket-client. Otherwise, you can pass the server-websocket-session to `SpringReuseRowClientFactory.getRowClient()` to reuse the same connection and have a `RowClient` instance.