/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

var subSocket;
var dataPoints = {};
var numTrackedProps = 0;
var plot;
var lastPlotDrawTime = 0;

var resetSim = false;
var lowPass = true;
var smoothCurv = true;
var lowPassStrength = 0.2;

var simOk = $('#simOk');
var progBar = $('#progBar');
var errorAlert = $('#errorAlert');
var errorMsg = $('#errorMsg');

$('.alert .close').on("click", function(e) {
    $(this).parent().hide();
});

$('#checkLowPass').change(function(e) {
    lowPass = !lowPass;
	var ob = new Array();
	for ( var d in dataPoints) {
		ob.push(createDataPlot(d, dataPoints[d]));
	}
	plot = $.plot($("#plot"), ob, plotOptions);
});

$('#checkSmoothCurv').change(function(e) {
	smoothCurv = !smoothCurv;
	var ob = new Array();
	for ( var d in dataPoints) {
		ob.push(createDataPlot(d, dataPoints[d]));
	}
	plot = $.plot($("#plot"), ob, plotOptions);
});

$('#txtLowPass').keyup(function(e) {
    var c = e.keyCode
    var value = $(this).val();

    if (value.match(/^\d+\.?\d*$/)) {
    	$(this).css("background-color", "");
    } else {
    	$(this).css("background-color", "red");
    }
    if (c == 13) {
    	$(this).blur();
    	lowPassStrength = parseFloat(value);
    	var ob = new Array();
    	for ( var d in dataPoints) {
    		ob.push(createDataPlot(d, dataPoints[d]));
    	}
    	plot = $.plot($("#plot"), ob, plotOptions);
    }
});

var redraw = function(force) {
	if (dataPoints != null && (force || (Date.now() - lastPlotDrawTime) > 750)) {
    	var ob = new Array();
    	for ( var d in dataPoints) {
    		ob.push(createDataPlot(d, dataPoints[d]));
    	}
    	plot = $.plot($("#plot"), ob, plotOptions);
		lastPlotDrawTime = Date.now();
	}
}

var plotLegendPos = "ne";
var plotOptions = {
//	yaxis : {
//		min : 0,
//		max : 1500,
//		show : true,
//		tickFormatter : function(val, axis) {
//			return val;
//		}
//	},
//	xaxis : {
//		min : 0,
//		max : 40,
//		show : true
//	},
	series: {
		curvedLines: {
			active: true
		}
	},
	legend : {
		backgroundOpacity : 0.5,
		noColumns : 0,
		backgroundColor : "white",
		position : plotLegendPos
	}
};

var createDataPlot = function(lbl, dps) {
	var a;
	if (lowPass) {
		a = new Array();
		if (dps != null && dps[0] != null) {
			var smooth = dps[0][1];
			for (var i=1; i<dps.length; i++) {
				smooth += (dps[i][0] - dps[i-1][0]) * (dps[i][1] - smooth) / lowPassStrength
				a.push([dps[i][0], smooth]);
			}
		}
	} else {
		a = dps;
	}
	
	if (smoothCurv) {
		return {
			label : lbl,
			data : a,
			lines: { show: true, lineWidth: 3}, 
			curvedLines: {apply:true}
		};
	} else {
		return {
			label : lbl,
			data : a
		};
	}
}

var createDataTrack = function(stateName, propName) {
	var _message = stateName + "." + propName;
	return {
		push : function() {
			if (!dataPoints[_message]) {
				subSocket.push(JSON.stringify({ type: "RegisterDataListener", dataPoint: _message }));
				dataPoints[_message] = new Array();
				numTrackedProps = numTrackedProps + 1;
			} else {
				subSocket.push(JSON.stringify({ type: "UnRegisterDataListener", dataPoint: _message }));
				dataPoints[_message] = null;
				numTrackedProps = numTrackedProps - 1;
			}

			var ob = new Array();
			for ( var d in dataPoints) {
				ob.push(createDataPlot(d, dataPoints[d]));
			}
			plot = $.plot($("#plot"), ob, plotOptions);
		},
	};
}

var loadSim = function(name) {
	if (subSocket) {
		simOk.hide();
		subSocket.push(JSON.stringify({ type: "RequestSimInfo", simName: name }))
	}
}

$(function() {
	"use strict";

	var simTitle = $('#simTitle');
	var simDesc = $('#simDesc');
	var simParams = $('#simParams');

	var socket = $.atmosphere;
	var transport = 'websocket';
	var params = null;

	var request = {
		url : "/wholecellcontrol",
		contentType : "application/json",
		logLevel : 'debug',
		transport : transport,
		fallbackTransport : 'long-polling'
	};

	request.onOpen = function(response) {
		console.log('Atmosphere connected using ' + response.transport);
		simOk.show();
	};

	request.onReconnect = function(rq, rs) {
		socket.info("Reconnecting")
	};

	request.onMessage = function(rs) {
		var message = rs.responseBody;

		try {
			var json = jQuery.parseJSON(message);
//			console.log("Got a valid JSON message: " + json)
		} catch (e) {
			console.log('This doesn\'t look like a valid JSON object: ',
					message.data);
			errorMsg.text("Received unknown object from server.");
			errorAlert.show();
			return;
		}

		if (json.graph) {
			updateSimInfo(json);
		} else if (json.state && json.prop) {
			updateData(json);
		} else if (json == "Simulation Done") {
			simDone();
		} 
		return;
	};

	request.onClose = function(rs) {
		simOk.hide();
		errorMsg.text("Lost connection to server.");
		errorAlert.show();
	};

	request.onError = function(rs) {
		simOk.hide();
		errorMsg.text("Lost connection to server.");
		errorAlert.show();
	};

	subSocket = socket.subscribe(request);

	var ypt = [];

	function getData(data) {
		ypt.push(data);
		return getPoints();
	}
	
	function getPoints() {
		var ret = [];
		for ( var i = 0; i < ypt.length; ++i)
			ret.push([ i, ypt[i] ]);
		return ret;
	}

	// Setup plot
	plot = $.plot($("#plot"), [ {
		label : "Select Data Points",
		data : ypt
	} ], plotOptions);

	
	$('#runSim').click(function() {
		if (resetSim) {
			subSocket.push(JSON.stringify({ type: "ResetSimulation" }));
			var ob = new Array();
			for ( var d in dataPoints) {
				dataPoints[d] = new Array();
				ob.push(createDataPlot(d, dataPoints[d]));
			}
			plot = $.plot($("#plot"), ob, plotOptions);
			
			$('#runSim').addClass('btn-success').removeClass('btn-warning').text('Run Simulation');
			resetSim = false;
		} else {
			subSocket.push(JSON.stringify({ type: "StartSimulation", simName: name }));
			$('#runSim').addClass('disabled');
		}
	});
	
	function simDone() {
		$('#runSim').removeClass('btn-success').addClass('btn-warning').removeClass('disabled').text('Reset Simulation');
		resetSim = true;
		redraw(true);
	}

	function updateData(data) {
		dataPoints[data.state + "." + data.prop].push([
				parseFloat(data.time), parseFloat(data.value) ]);
		redraw(false);
	}

	function updateSimInfo(data) {
		var hugeGraph = new HugeGraph(subSocket);
		hugeGraph.setModel(data.graph);
		hugeGraph.draw();
		$('#runSim').removeClass('btn-warning').removeClass('disabled').addClass('btn-success');
		simTitle.text(data.title);
		simDesc.text(data.desc);
		params = data.params;
		simParams.text("");
		var html = '<form class="form-horizontal">';
		for (p in data.params) {
			var paramVal = data.params[p];
			html += '<div class="control-group"><label class="control-label" for="inputParam'+p+'">'+p+'</label><div class="controls"><input type="text" id="input'+p+'" placeholder="'+paramVal+'"></div></div>';
		}
		html += '</form>';
		simParams.append(html);
		
		for (p in data.params) {
			(function (pp) {
				$('#input'+pp).keyup(function(e) {
				    var c = e.keyCode
				    var value = $(this).val();
	
				    if (value.match(/^\d+\.?\d*$/)) {
				    	$(this).css("background-color", "");
				    } else {
				    	$(this).css("background-color", "red");
				    }
				    if (c == 13) {
				    	$(this).blur();
				    	subSocket.push(JSON.stringify({ type: "UpdateParam", param: pp, value: value }));
				    }
				});
			}(p));
		}
		simOk.show();
	}
});