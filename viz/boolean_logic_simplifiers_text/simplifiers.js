
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
  vdd.wamp.sendData(session, logicStr);
});

var nodeTypeToHtml = {
  and: function(c) {
    var html = "AND: <ul>";

    for(var i=0; c.conditions && i<c.conditions.length; i++)
    {
      var cond = c.conditions[i];
      html += "<li>" + nodeTypeToHtml[cond.type](cond) + "</li>";
    }
    html += "</ul>";

    return html;
  },

  or: function (c) {
    var html = "AND: <ul>";

    for(var i=0; c.conditions && i<c.conditions.length; i++)
    {
      var cond = c.conditions[i];
      html += "<li>" + nodeTypeToHtml[cond.type](cond) + "</li>";
    }
    html += "</ul>";

    return html;
  },

  eq: function (c) {
    return c.value1.toString() + " = " + c.value2.toString();
  }
};

//Draws a condition 
function drawConditionGraph(condition) {
  var div = $("div#before-graph");
  div.html(nodeTypeToHtml[condition.type](condition));
}