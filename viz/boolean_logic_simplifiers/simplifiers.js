
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
    .gravity(0)
    .distance(50)
    .charge(function (node, index){
      return -100;
    })
    .on("tick", tick)
    .size([w, h]);

// Time duration in ms
var duration = 200;

var node,
    link,
    root;


// Returns a function that can be passed to d3 which will look up attribute values 
// by type
// TODO change this to use CSS styling instead. Just put the node type as a 
function attribFunction(submapName, attribute) {
  // A map of node type to circle and text settings
  
  var typeSettings = {or: {circle: {cx: 0,
                                    cy: 0,
                                    r: 10,
                                    fill: "green"},

                           text: {fill: "white", 
                                  dx: "-0.45em",
                                  dy: "4px"}},

                      and: {circle: {cx: 0,
                                     cy: 0,
                                     r: 15,
                                     fill: "blue"},

                            text: {fill: "white", 
                                   dx: "-13px",
                                   dy: "5px"}},

                      eq:  {circle: {cx: 0,
                                     cy: 0,
                                     r: 0,
                                     fill: "white"},

                            text: {fill: "black", 
                                   dx: "-0.75em",
                                   dy: "5px"}}};

  return function(d) { return typeSettings[d.type][submapName][attribute]; };
};     


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
  node = vis.selectAll("g.node")
      .data(iterationData.nodes);

  // Enter any new nodes.
  node.enter().append("g")
      .attr("class", "node")
      .call(force.drag);

  node.append("circle")
      .attr("cx", attribFunction("circle", "cx"))
      .attr("cy", attribFunction("circle", "cy"))
      .attr("r", attribFunction("circle", "r"))
      .style("fill", attribFunction("circle", "fill"));
      // TODO Disabled for now
      // .on("click", click)

                           
  node.append("text")
      .attr("dx", attribFunction("text", "dx"))
      .attr("dy", attribFunction("text", "dy"))
      .attr("fill", attribFunction("text", "fill"))
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
