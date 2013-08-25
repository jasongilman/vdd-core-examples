
// This represents the vdd websocket session.
var session = null;
var duration = 750;

// Creates a player control inside the player div. The function returned can be used to 
// set the data of the player and the function handler.
var playerUpdateFn = vdd.player.createPlayerFn($("div#player"), {duration: duration});

// Handles receiving visualization data through WAMP.
function onVizData(topic, eventData) {
  console.log("visualization data received", eventData);

  drawConditionGraph(eventData[0]);
  playerUpdateFn(eventData, drawConditionGraph);
}

// Connect using the WAMP protocol and register callback for visualization data
session = vdd.wamp.connect(onVizData);

// Handle submitting boolean logic
$("a#submit-logic-text").click(function (event) {
  var logicStr = $("textarea")[0].value;
  vdd.wamp.sendData(session, "boolean-logic-simplifiers-text.driver/test-simplifiers", logicStr);
});

function drawConditionGraph(condition) {
  var div = $("div.boolean-graph");
  div.html(nodeTypeToHtml[condition.type](condition));
}

function conditionsToHtmlList(conditions) {
  var html = "<ul>";
  for(var i=0; conditions && i<conditions.length; i++)
  {
    var c = conditions[i];
    html += "<li>" + nodeTypeToHtml[c.type](c) + "</li>";
  }
  return html + "</ul>";
}

var nodeTypeToHtml = {
  and: function(c) {
    return "AND: " + conditionsToHtmlList(c.conditions);
  },
  or: function (c) {
    return "OR: " + conditionsToHtmlList(c.conditions);
  },
  eq: function (c) {
    return c.value1.toString() + " = " + c.value2.toString();
  }
};
