// From http://mbostock.github.io/d3/tutorial/bar-2.html
// See also http://mbostock.github.io/d3/tutorial/bar-1.html

// Creates a player control inside the player div. The function returned can be used to 
// set the data of the player and the function handler.
var playerUpdateFn = vdd.player.createPlayerFn($("div#player"));

var h = 400;
var w = 800;

var chart = d3.select("svg.chart")
// 90% would make it scale with the width but we would have to handle that with the xscale as well
  .attr("width", w) 
  .attr("height", h);

// Time duration in ms
var duration = 200;

var xScale = d3.scale.ordinal()
  .rangeRoundBands([0, w], 0.05, 0);
var yScale = d3.scale.linear()
  .range([20, h]);

function onVizData(topic, eventData) {
  console.log("visualization data received", eventData);
  // Sets the event data and the handler function.
  playerUpdateFn(eventData, displayData);
}

vdd.wamp.connect(onVizData);

function displayData(iterationData) {
  console.log("Displaying data", iterationData);

  var pivot = iterationData.pivot;
  var items = iterationData.items;
  var lefts = iterationData.left;
  var rights = iterationData.right;

  //Update scale domains
  xScale.domain(d3.range(items.length));
  yScale.domain([0, d3.max(items)]);

  var colorSelector = function(d) {
    if (d == pivot) {
      return "red";
    }
    else if (lefts.indexOf(d) != -1){
      return "green";
    }
    else if (rights.indexOf(d) != -1){
      return "green";
    }
    else {
      return "steelblue";
    }
  };

  // TODO make the pivot selection thing a separate animation

  var bars = chart.selectAll("rect")
    .data(items);

  // Existing Rects
  bars.transition()
    .duration(duration)
    .attr("x", function(d, i) { return xScale(i); })
    .attr("y", function(d) { return h - yScale(d); })
    .attr("fill", colorSelector)
    .attr("width", xScale.rangeBand())
    .attr("height", function(d) { return yScale(d); });

  // New Rects
  bars.enter().append("rect")
    .attr("x", function(d, i) { return xScale(i); })
    .attr("y", function(d) { return h - yScale(d); })
    .attr("fill", colorSelector)
    .attr("width", xScale.rangeBand())
    .attr("height", function(d) { return yScale(d); });
  
  // Rects leaving
  bars.exit()
    .transition()   
    .duration(duration)
    //Move past the right edge of the SVG
    .attr("x", w)   
    .remove();

  var text = chart.selectAll("text")
    .data(items)

  // text added  
  text.enter()
    .append("text")
    .text(function(d) { return d; })
    .attr("text-anchor", "middle")
    .attr("x", function(d,i) { return xScale(i) + xScale.rangeBand() / 2; })
    .attr("y", function(d) { return h - yScale(d) + 14 })
    .attr("font-family", "sans-serif")
    .attr("font-size", "11px")
    .attr("fill", "white");

  // Update existing values
  text
    .transition()   
    .duration(duration)
    .text(function(d) { return d; })
    .attr("x", function(d,i) { return xScale(i) + xScale.rangeBand() / 2; })
    .attr("y", function(d) { return h - yScale(d) + 14 });

  // Exiting text removed
  text.exit()
    .transition()   
    .duration(duration)
    .text(function(d) { return "so long"; })
    .attr("x", w)   //Move past the right edge of the SVG
    .remove();
}