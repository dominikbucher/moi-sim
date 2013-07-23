/*
 * Authors: 
 * - Dominik Bucher, ETH Zurich, Github: dominikbucher
 */

(function() {
  var FlowLine, HugeGraph, TransformationBox;
  var __hasProp = Object.prototype.hasOwnProperty, __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };
  HugeGraph = (function() {
    function HugeGraph(socket) {
      this.display_in_element = 'hugeGraph';
      document.getElementById(this.display_in_element).innerHTML = '';
      this.display_width = $('#hugeGraph').width();
      this.display_height = $('#hugeGraph').height();
      this.r = Raphael(this.display_in_element, this.display_width, this.display_height);
      this.left_margin = 20;
      this.right_margin = 20;
      this.y_space = 10;
      this.threshold_for_drawing = 0;
      this.box_width = 125;
      this.flow_edge_width = 2;
      this.flow_curve = 0.25;
      this.socket = socket;
	  
	  this.states = {};
	  this.processes = {};
	  
      this.leftStateBoxes = {};
      this.rightStateBoxes = {};
	  this.processBoxes = {};
      this.lines = {};
    }
	HugeGraph.prototype.setModel = function(model) {
		this.states = model.states;
		this.processes = model.processes;
		
		for (s in this.states) {
			name = this.states[s].name;
			new_box = new TransformationBox(this, name, [], this.states[s].props);
			this.leftStateBoxes[name] = new_box;
			new_box = new TransformationBox(this, name, this.states[s].props, []);
			this.rightStateBoxes[name] = new_box;
		}
		for (p in this.processes) {
			name = this.processes[p].name;
			new_box = new TransformationBox(this, name, [], []);
			this.processBoxes[name] = new_box;
			for (s in this.processes[p].readProps) {
				var stateName = this.processes[p].readProps[s].state;
				var propName = this.processes[p].readProps[s].name;
				new_line = new FlowLine(this, stateName, propName, 1, name, []);
				this.lines[stateName + "." + propName + ":" + name + "."] = new_line;
			}
			for (s in this.processes[p].writeProps) {
				var stateName = this.processes[p].writeProps[s].state;
				var propName = this.processes[p].writeProps[s].name;
				new_line = new FlowLine(this, name, [], 1, stateName, propName);
				this.lines[name + ".:" + stateName + "." + propName] = new_line;
			}
		}
	};
	HugeGraph.prototype.draw = function() {
		this.positionElements();
		for (l in this.lines) {
			var line = this.lines[l];
			line.draw(this.r);
		}
		for (s in this.leftStateBoxes) {
			var box = this.leftStateBoxes[s];
			box.draw(this.r);
		}
		for (s in this.rightStateBoxes) {
			var box = this.rightStateBoxes[s];
			box.draw(this.r);
		}
		for (s in this.processBoxes) {
			var box = this.processBoxes[s];
			box.draw(this.r);
		}
	};
	HugeGraph.prototype.positionElements = function() {
		var y = 0;
		for (s in this.leftStateBoxes) {
			var box = this.leftStateBoxes[s];
			box.resize();
			box.x = 0;
			box.y = y;
			y += box.height + this.y_space;
		}
		this.r.setSize(this.r.width, Math.max(this.r.height, y));
		y = 0;
		for (s in this.rightStateBoxes) {
			var box = this.rightStateBoxes[s];
			box.resize();
			box.x = this.display_width - this.box_width;
			box.y = y;
			y += box.height + this.y_space;
		}
		this.r.setSize(this.r.width, Math.max(this.r.height, y));
		y = 0;
		for (s in this.processBoxes) {
			var box = this.processBoxes[s];
			box.resize();
			box.x = (this.display_width - this.box_width)/2;
			box.y = y;
			y += box.height + this.y_space;
		}
		this.r.setSize(this.r.width, Math.max(this.r.height, y));
		for (l in this.lines) {
			var line = this.lines[l];
			if (this.leftStateBoxes[line.left_box_name] == null) {
				line.ox = this.processBoxes[line.left_box_name].x + this.box_width;
				line.oy = this.processBoxes[line.left_box_name].getOutPropPos(line.left_box_prop);
				this.processBoxes[line.left_box_name].right_lines[line.name()] = line;
				line.dx = this.rightStateBoxes[line.right_box_name].x;
				line.dy = this.rightStateBoxes[line.right_box_name].getInPropPos(line.right_box_prop);
				this.rightStateBoxes[line.right_box_name].left_lines[line.name()] = line;
			} else if (this.rightStateBoxes[line.right_box_name] == null) {
				line.ox = this.leftStateBoxes[line.left_box_name].x + this.box_width;
				line.oy = this.leftStateBoxes[line.left_box_name].getOutPropPos(line.left_box_prop);
				this.leftStateBoxes[line.left_box_name].right_lines[line.name()] = line;
				line.dx = this.processBoxes[line.right_box_name].x;
				line.dy = this.processBoxes[line.right_box_name].getInPropPos(line.right_box_prop);
				this.processBoxes[line.right_box_name].left_lines[line.name()] = line;
			} else {
				line.ox = this.leftStateBoxes[line.left_box_name].x + this.box_width;
				line.oy = this.leftStateBoxes[line.left_box_name].getOutPropPos(line.left_box_prop);
				this.leftStateBoxes[line.left_box_name].right_lines[line.name()] = line;
				line.dx = this.rightStateBoxes[line.right_box_name].x;
				line.dy = this.rightStateBoxes[line.right_box_name].getInPropPos(line.right_box_prop);
				this.rightStateBoxes[line.right_box_name].left_lines[line.name()] = line;
			}
		}
	};
    HugeGraph.prototype.convert_box_description_labels_callback = function(name) {
      return name;
    };
    return HugeGraph;
  })();
  FlowLine = (function() {
    function FlowLine(hugeGraph, left_box_name, left_box_prop, flow, right_box_name, right_box_prop) {
      this.hugeGraph = hugeGraph;
      this.hover_stop = __bind(this.hover_stop, this);
      this.hover_start = __bind(this.hover_start, this);
      this.flow = flow;
      this.colour = void 0;
      this.ox = 0;
      this.oy = 0;
      this.dx = 0;
      this.dy = 0;
	  this.left_box_name = left_box_name;
	  this.left_box_prop = left_box_prop;
	  this.right_box_name = right_box_name;
	  this.right_box_prop = right_box_prop;
      //this.left_box = this.hugeGraph.find_or_create_transformation_box(left_box_name);
      //this.right_box = this.hugeGraph.find_or_create_transformation_box(right_box_name);
      //this.left_box.right_lines.push(this);
      //this.right_box.left_lines.push(this);
    }
	FlowLine.prototype.name = function() {
		return this.left_box_name + "." + this.left_box_prop + ":" + this.right_box_name + "." + this.right_box_prop;
	}
    FlowLine.prototype.path = function() {
      var curve;
      curve = (this.dx - this.ox) * this.hugeGraph.flow_curve;
      return "M " + this.ox + "," + this.oy + " Q " + (this.ox + curve) + "," + this.oy + " " + ((this.ox + this.dx) / 2) + "," + ((this.oy + this.dy) / 2) + " Q " + (this.dx - curve) + "," + this.dy + " " + this.dx + "," + this.dy;
    };
    FlowLine.prototype.draw = function(r) {
      this.outer_line = r.path(this.path()).attr({
        'stroke-width': this.flow_edge_width,
        'stroke': "#000"
      });
      /*this.inner_line = r.path(this.path()).attr({
        'stroke-width': 1,
        'stroke': "#000"
      });*/
      this.outer_line.hover(this.hover_start, this.hover_stop);
      //r.set().push(this.inner_line, this.outer_line).hover(this.hover_start, this.hover_stop);
      /*this.left_label = r.text(this.ox + 1, this.oy - (this.size / 2) - 5, this.labelText()).attr({
        'text-anchor': 'start'
      });
      this.right_label = r.text(this.dx - 1, this.dy - (this.size / 2) - 5, this.labelText()).attr({
        'text-anchor': 'end'
      });
      this.left_label.hide();
      return this.right_label.hide();*/
    };
	FlowLine.prototype.hover_start = function() {
      this.outer_line.attr({"stroke":"#aaf"});
    };
    FlowLine.prototype.hover_stop = function() {
      this.outer_line.attr({"stroke":"#000"});
    };
    return FlowLine;
  })();
  TransformationBox = (function() {
    function TransformationBox(hugeGraph, name, inProperties, outProperties) {
      this.hugeGraph = hugeGraph;
      this.name = name;
      this.hover_end = __bind(this.hover_end, this);
      this.hover_start = __bind(this.hover_start, this);
      this.text_click = __bind(this.text_click, this);
      this.label_text = this.hugeGraph.convert_box_description_labels_callback(name);
      this.line_colour = "orange";
      this.inProperties = inProperties;
	  this.inPropertiesPositions = {};
      this.outProperties = outProperties;
	  this.outPropertiesPositions = {};
      this.left_lines = [];
      this.right_lines = [];
      this.x = 0;
      this.y = 0;
	  this.height = 0;
    }
	TransformationBox.prototype.resize = function() {
		this.height = 50 + Math.max(this.inProperties.length, this.outProperties.length) * 20;
		var y = 50;
		for (p in this.outProperties) {
			var prop = this.outProperties[p].name;
			this.outPropertiesPositions[prop] = this.y + y;
			y += 20;
		}
		y = 50;
		for (p in this.inProperties) {
			var prop = this.inProperties[p].name;
			this.inPropertiesPositions[prop] = this.y + y;
			y += 20;
		}
	}	
	TransformationBox.prototype.draw = function(r) {
		var onlyOneText = (this.inProperties === this.outProperties);
		var set, text, circ, prop, path;
		set = r.set();
		this.box = r.rect(this.x, this.y, this.hugeGraph.box_width, this.height, 0).attr({"fill": "#F5F5F5", "stroke": "#ddd", "opacity": 0.8});
		text = r.text(this.x + 15, this.y + 20, this.name).attr({'text-anchor':'start'});
		set.push(this.box, text);
		var y = 50;
		for (p in this.outProperties) {
			prop = this.outProperties[p].name;
			circ = r.circle(this.x + this.hugeGraph.box_width - 10, this.y + y, 3).attr({"fill": "#222"});
			if (!onlyOneText) {
				text = r.text(this.x + this.hugeGraph.box_width - 20, this.y + y, prop).attr({'text-anchor':'end'});
			} else {
				text = r.text(this.x + this.hugeGraph.box_width - 100, this.y + y, prop);
			}
			var socketPush = createDataTrack(this.name, prop);
			text.click(socketPush.push);
			text.attr({"cursor":"pointer"});
			set.push(text, circ);
			y += 20;
		}
		
		y = 50;
		for (p in this.inProperties) {
			prop = this.inProperties[p].name;
			circ = r.circle(this.x + 10, this.y + y, 3).attr({"fill": "#222"});
			if (!onlyOneText) {
				text = r.text(this.x + 20, this.y + y, prop).attr({'text-anchor':'start'});
			}
			set.push(text, circ);
			y += 20;
		}
      set.hover(this.hover_start, this.hover_end);
	}
	TransformationBox.prototype.getInPropPos = function(name) {
		if (this.inPropertiesPositions[name] == null) {
			return this.y + 20;
		} else {
			return this.y + this.inPropertiesPositions[name]; 
		}
    };
	TransformationBox.prototype.getOutPropPos = function(name) {
		if (this.outPropertiesPositions[name] == null) {
			return this.y + 20;
		} else {
			return this.y + this.outPropertiesPositions[name]; 
		}
    };
	TransformationBox.prototype.hover_start = function() {
      this.box.attr({"fill":"#ADF0FF", "opacity": 1});
      for (l in this.hugeGraph.lines) {
    	  if (!this.left_lines[l]) {
    		  if (!this.right_lines[l]) {
    			  this.hugeGraph.lines[l].outer_line.attr({"stroke":"#ADF0FF", "opacity": 0.4});
    		  }
    	  } 		  
      }
//	  for (l in this.left_lines) {
//		this.left_lines[l].outer_line.attr({"stroke":"#ADF0FF"});
//	  }
//	  for (l in this.right_lines) {
//		this.right_lines[l].outer_line.attr({"stroke":"#ADF0FF"});
//	  }
    };
    TransformationBox.prototype.hover_end = function() {
        this.box.attr({"fill":"#f5f5f5", "opacity": 0.8});
        for (l in this.hugeGraph.lines) {
        	this.hugeGraph.lines[l].outer_line.attr({"stroke":"#000", "opacity": 1});
        }
//   	  for (l in this.left_lines) {
//  		this.left_lines[l].outer_line.attr({"stroke":"#000"});
//  	  }
//  	  for (l in this.right_lines) {
//  		this.right_lines[l].outer_line.attr({"stroke":"#000"});
//  	  }
     };
     TransformationBox.prototype.text_click = function(propPath) {
    	this.hugeGraph.socket.push(propPath);
     };

    return TransformationBox;
  })();
  window.HugeGraph = HugeGraph;
}).call(this);
