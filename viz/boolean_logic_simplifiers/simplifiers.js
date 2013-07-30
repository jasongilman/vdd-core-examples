
var session = null;

// Creates a player control inside the player div. The function returned can be used to 
// set the data of the player and the function handler.
var playerUpdateFn = vdd.player.createPlayerFn($("div#player"));

var h = 400;
var w = 800;

var chart = d3.select("svg.chart")
  .attr("width", w) 
  .attr("height", h);

// Time duration in ms
var duration = 200;

// Handles receiving visualization data through WAMP.
function onVizData(topic, eventData) {
  console.log("visualization data received", eventData);
  // Sets the event data and the handler function.
  playerUpdateFn(eventData, displayData);
}

// Connect using the WAMP protocol and register callback for visualization data
session = vdd.wamp.connect(onVizData);

// Displays one iteration's worth of data.
function displayData(iterationData) {
  console.log("Displaying data", iterationData);

}

// Handle submitting boolean logic
$("a#submit-logic-text").click(function (event) {
  console.log(event);
  var logicStr = $("textarea")[0].value;
  vdd.wamp.sendData(session, logicStr);
});