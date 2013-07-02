var WS_URI = 'ws://localhost:8080/ws';
var BASE_TOPIC_URI = "http://vdd-core/";

function vddConnect() {
  var sess;

  // Connect to WebSocket
  ab.connect(
      WS_URI,
      // Connection callback
      function (session) {
          sess = session;
          console.log("Connected to " + WS_URI, sess.sessionid());
          sess.prefix("event", BASE_TOPIC_URI + "event#"); 
          sess.prefix("rpc",   BASE_TOPIC_URI + "rpc#");

          sess.subscribe("event:vizdata", onVizData);       // Subscribe to viz data channel
      },
      // Disconnection callback
      function (code, reason) {
          sess = null;
          if (code != 0) {  // ignore app disconnects
              console.log("Connection lost (" + reason + ")");
          }
      },
      // Options
      {'maxRetries': 60, 'retryDelay': 30000}
  );
}

vddConnect();