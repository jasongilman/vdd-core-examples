
// This represents the vdd websocket session.
var session = null;

// Creates a player control inside the player div. The function returned can be used to 
// set the data of the player and the function handler.
var playerUpdateFn = vdd.player.createPlayerFn($("div#player"));

// Handles receiving visualization data through WAMP.
function onVizData(topic, eventData) {
  console.log("visualization data received", eventData);
  // Sets the event data and the handler function.
  playerUpdateFn(eventData, displayData);
}

// Connect using the WAMP protocol and register callback for visualization data
session = vdd.wamp.connect(onVizData);

// Handle submitting boolean logic
$("a#submit-logic-text").click(function (event) {
  console.log(event);
  var logicStr = $("textarea")[0].value;
  vdd.wamp.sendData(session, logicStr);
});


//////////////////////////////////////////
// D3 visualization stuff

var h = 400;
var w = 800;

var vis = d3.select("svg.chart")
  .attr("width", w) 
  .attr("height", h);

  var force = d3.layout.force()
    .on("tick", tick)
    .size([w, h]);

// Time duration in ms
var duration = 200;

var node,
    link,
    root;

// Displays one iteration's worth of data.
function displayData(iterationData) {
  console.log("Displaying data", iterationData);

  // Restart the force layout.
  force
      .nodes(iterationData.nodes)
      .links(iterationData.links)
      .start();

  // Update the links…
  link = vis.selectAll("line.link")
      .data(iterationData.links);

  // Enter any new links.
  link.enter().insert("svg:line", ".node")
      .attr("class", "link")
      .attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  // Exit any old links.
  link.exit().remove();

  // Update the nodes…
  node = vis.selectAll("circle.node")
      .data(iterationData.nodes)
      .style("fill", color);

  // Enter any new nodes.
  node.enter().append("svg:circle")
      .attr("class", "node")
      .attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; })
      .attr("r", function(d) { return 5; })
      .style("fill", color)
      // TODO Disabled for now
      // .on("click", click)
      .call(force.drag);

  // node.append("text")
  //     .attr("dx", function(d) { return d.x; }))
  //     .attr("dy", function(d) { return d.y; }))
  //     .text(function(d) { return d.title });

  // Exit any old nodes.
  node.exit().remove();
}


function tick() {
  link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  node.attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; });
}

// Color leaf nodes orange, and packages white or blue.
// TODO we could use different colors for and, or, eq etc.
function color(d) {
  // return d._children ? "#3182bd" : d.children ? "#c6dbef" : "#fd8d3c";
  return "#3182bd";
}

// Toggle children on click.
// TODO not enabled for now
function click(d) {
  if (d.children) {
    d._children = d.children;
    d.children = null;
  } else {
    d.children = d._children;
    d._children = null;
  }
  update();
}
