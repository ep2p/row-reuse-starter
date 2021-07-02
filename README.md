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
