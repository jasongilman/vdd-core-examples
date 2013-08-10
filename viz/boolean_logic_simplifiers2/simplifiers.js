
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
  .append("g")
    .attr("transform", "translate(10,10)");

var tree = d3.layout.tree()
    .size([w - 20, h - 20]);

// Time duration in ms
var duration = 1000;

var root = {},
    nodes = tree(root);

root.parent = root;
root.px = root.x;
root.py = root.y;

var diagonal = d3.svg.diagonal();


// Displays one iteration's worth of data.
function displayData(iterationData) {
  console.log("Displaying data", iterationData);

  // Fix the root node in the center of the graph.
  iterationData.nodes[0].fixed = true
  iterationData.nodes[0].x = w/2;
  iterationData.nodes[0].y = h/2;


  // Restart the force layout.
  force
      .nodes(iterationData.nodes)
      .links(iterationData.links)
      .start();

  // Update the links…
  link = linksGroup.selectAll("line.link")
      .data(iterationData.links); 

  // Enter any new links.
  link.enter().append("svg:line")
      .attr("class", "link")
      .attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  // Exit any old links.
  link.exit().remove();

  // Update the nodes…
  node = nodesGroup.selectAll("g.node")
      .data(iterationData.nodes);

   //Existing
  node.attr("class", function(d) { return d.type + "-type node"})
      .call(force.drag);

  node.select("circle")
      .attr("cx", attribFunction("circle", "cx"))
      .attr("cy", attribFunction("circle", "cy"))
      .attr("r", attribFunction("circle", "r"));

  node.select("text")
      .attr("dx", attribFunction("text", "dx"))
      .attr("dy", attribFunction("text", "dy"))
      .text(function(d) { return d.title });


  // Enter any new nodes.
  newNodes = node.enter().append("g")
      .attr("class", function(d) { return d.type + "-type node"})
      .call(force.drag);

  newNodes.append("circle")
      .attr("cx", attribFunction("circle", "cx"))
      .attr("cy", attribFunction("circle", "cy"))
      .attr("r", attribFunction("circle", "r"));

  newNodes.append("text")
      .attr("dx", attribFunction("text", "dx"))
      .attr("dy", attribFunction("text", "dy"))
      .text(function(d) { return d.title });

  // Exit any old nodes.
  node.exit().remove();
}

function tick() {
  link.attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; });

  node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
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
