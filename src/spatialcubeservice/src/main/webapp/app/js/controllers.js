'use strict';

// TODO: Move these common functions into an Angular service.
if (vpac.socket === undefined)
	vpac.socket = {};
vpac.socket.fromRef = function(ref, nodeMap, direction) {
	var re = /^#([^\/]+)(\/(.+))?$/;
	var match = re.exec(ref);
	if (match == null)
		return null;
	var nodeId = match[1];
	var socketName = match[3];
	var node = nodeMap[nodeId];
	if (node === undefined)
		return null;
	var sockets;
	if (direction == 'input')
		sockets = node.inputs;
	else if (direction == 'output')
		sockets = node.outputs;
	else
		sockets = node.inputs.concat(node.outputs);
	for (var i = 0; i < sockets.length; i++) {
		var socket = sockets[i];
		if (socket.name == socketName)
			return [node, socket];
	}
	return [node, null];
};
vpac.socket.toRef = function(node, socket) {
	return '#' + node.id + '/' + socket.name;
};

angular.module('graphEditor').controller('GraphEditor', function GraphEditor($scope, $location, datasets, filters, $http) {
	console.log('init graph editor');

	// Allow debugging on-demand: press F7 to prevent the page from updating.
	$(window).keydown(function(e) {
		if (e.keyCode == 118)
			debugger;
	});

	$scope.node_map = {};
	$scope.getUniqueNodeId = function(prefix) {
		var re = /\W+/g;
		prefix = prefix.replace(re, '_');
		var i = 0;
		while (true) {
			var id = prefix + '_' + i++;
			if ($scope.node_map[id] === undefined)
				return id;
		}
	};
	$scope.$watch('nodes', function(newNodes, oldNodes) {
		console.log('Nodes changed');
		// Remove old nodes from the map.
		for (var i = 0; i < oldNodes.length; i++) {
			var node = oldNodes[i];
			if (vpac.indexOf(newNodes, node) < 0)
				delete $scope.node_map[node.id];
		}
		// Ensure each node has an ID, and is in the map.
		for (var i = 0; i < newNodes.length; i++) {
			var node = newNodes[i];
			if (node.id === undefined) {
				node.id = $scope.getUniqueNodeId(node.name);
			}
			if ($scope.node_map[node.id] === undefined)
				$scope.node_map[node.id] = node;
			for (var j = 0; j < node.inputs.length; j++) {
				var socket = node.inputs[j];
				if (!vpac.socket.isLiteral(socket)) {
					socket.value = undefined;
					if (socket.connections === undefined)
						socket.connections = [];
				} else {
					socket.connections = undefined;
					if (socket.value === undefined)
						socket.value = '';
				}
			}
			for (var j = 0; j < node.outputs.length; j++) {
				var socket = node.outputs[j];
				if (!vpac.socket.isLiteral(socket)) {
					socket.value = undefined;
					if (socket.connections === undefined)
						socket.connections = [];
				}
			}
		}
	}, true);

	$scope.$on('socketRefChanged', function(event, isInput, newRef, oldRef) {
		console.log('Socket ref changed!', newRef, oldRef);
		for (var i = 0; i < $scope.nodes.length; i++) {
			var node = $scope.nodes[i];
			var sockets;
			if (isInput)
				sockets = node.outputs;
			else
				sockets = node.inputs;
			for (var j = 0; j < sockets.length; j++) {
				var socket = sockets[j];
				if (socket.connections === undefined)
					continue;
				for (var k = 0; k < socket.connections.length; k++) {
					if (socket.connections[k] == oldRef) {
						console.log('Updating connection for',
								vpac.socket.toRef(node, socket));
						socket.connections[k] = newRef;
					}
				}
			}
		}
	});

	$scope.toolbox = [ {
		name: 'Inputs',
		values: []
	}, {
		name: 'Filters',
		values: []
	} ];

	$scope.fetchTools = function() {
		console.log('Refreshing');
		$scope.toolbox[0] = datasets.get();
		$scope.toolbox[1] = filters.get();
	};
	$scope.fetchTools();

	$scope.currentPreview = null;
	$scope.previewImages = [];
	$scope.downloadButtonDisable = false;
	$scope.getPreviewImages = function() {
		if ($scope.currentPreview == null)
			return $scope.previewImages;
		else
			return [$scope.currentPreview].concat($scope.previewImages);
	};

	var previewNum = 0;
	$scope.MAX_PREVIEW_ITEMS = 6;
	$scope.fetchPreview = function(preview) {
		if(!preview && $scope.downloadButtonDisable)
			return;
		console.log("preview", preview);
		var query = vpac.serialiseQuery($scope.nodes, {preview: preview});
		console.log('Fetching preview', query);
		var requestData = $.param({query: query, preview:preview});
		var state = angular.copy($scope.nodes);
		vpac.removeAngularHash(state);
		var queryURL = preview ? '../Data/MultiPreviewQuery' : '../Data/QueryOutput.json';
		console.log(state);
		$http({
			method: 'POST',
			headers: {'Content-Type': 'application/x-www-form-urlencoded'},
			url: queryURL,
			data: requestData
		}).success(function(data, status, headers, config) {
			console.log('fetched', status, data, config.data);
			var previewParam = true;
			if(config.data.indexOf("preview=") != -1) {
				angular.forEach(config.data.split("&"), function(param){
					if(param.split("=")[0] == "preview")
						if(param.split("=")[1] == "true")
							previewParam = true;
						else
							previewParam = false;
				});
			}
			if(previewParam) {
				var preview = {
					seq: ++previewNum,
					data: data,
					nodeGraph: state,
					success: true
				};
				$scope.previewImages = vpac.pushFront($scope.previewImages, preview,
						$scope.MAX_PREVIEW_ITEMS);
				$scope.currentPreview = preview;
				console.log($scope.previewImages);
			} else {
				console.log("taskId", data.taskId);
				$scope.progressTrackingOn = true;
				$scope.downloadButtonDisable = true;
				$scope.progressTracking(data.taskId);
			}
		}).error(function(data, status, headers, config) {
			console.log('Error fetching preview:', status, data);
			var errstring;
			// First try to find an exception with an error message; just display the message.
			var errmatch = /root cause.*?QueryConfigurationException: (.*)$/m.exec(data);
			if (errmatch == null) {
				var errmatch = /root cause.*?([a-zA-Z0-9_]+: .*)$/m.exec(data);
			}
			if (errmatch == null) {
				// If no message, display the class name.
				errmatch = /root cause.*?([a-zA-Z0-9_.]+)$/m.exec(data);
			}
			if (errmatch == null) {
				// Neither matched; just show the whole error report.
				errstring = data;
			} else {
				errstring = errmatch[1];
			}
			var preview = {
				seq: ++previewNum,
				error: errstring,
				nodeGraph: state,
				success: false
			};
			$scope.previewImages = vpac.pushFront($scope.previewImages, preview,
					$scope.MAX_PREVIEW_ITEMS);
			$scope.currentPreview = preview;
		});
	};
	$scope.progressTracking = function(taskId) {
		console.log("taskId - progressTracking:", taskId);
		if($scope.progressTrackingOn) {
			setTimeout(function() {
				$http({
					method: 'GET',
					url: "../Data/Task/" + taskId + ".json"
				}).success(function(data, status, headers, config) {
					$scope.progress = data.currentStepProgress;
					if(data.state == "RUNNING") {
						console.log("progress", data.currentStepProgress);
					} else if (data.state == "FINISHED") {
						if(data.currentStepProgress == 100.0 && $scope.progressTrackingOn) {
							$scope.progressTrackingOn = false;
							window.location = "../Data/Download/" + taskId;
							$scope.downloadButtonDisable = false;
						}
					} else if (data.state == "EXECUTION_ERROR") {
						$scope.progressTrackingOn = false;
						$scope.downloadButtonDisable = false;
					}
				});
				$scope.progressTracking(taskId);
			}, 5000);
		}
	}
	$scope.selectPreview = function(preview) {
		$scope.currentPreview = preview;
		$scope.restoreState(preview.nodeGraph)
	};
	$scope.restoreState = function(nodeGraph) {
		$scope.nodes = angular.copy(nodeGraph);
		$scope.fullRepaintRequired = true;
	};

	var initnodes = function() {
		var nodes = [ {
		    "name": "Output",
		    "id": "output",
		    "type": "output",
		    "position": {
		      "y": 102.7890625,
		      "x": 776.5,
		      "left": 708,
		      "top": 23
		    },
		    "inputs": [
		      {
		        "name": "grid",
		        "connections": [],
		        "type": "meta"
		      },
		      {
		        "name": "band",
		        "connections": [
		          "#Blur_0/output"
		        ],
		        "type": "scalar",
		        "synthetic": true
		      }
		    ],
		    "outputs": []
		  },
		  {
		    "position": {
		      "x": 101,
		      "y": 59,
		      "left": 32.5,
		      "top": 39.2109375
		    },
		    "description": "",
		    "outputs": [
		      {
		        "name": "grid",
		        "type": "meta",
		        "connections": []
		      },
		      {
		        "name": "time",
		        "type": "scalar,axis",
		        "connections": []
		      },
		      {
		        "name": "y",
		        "type": "scalar,axis",
		        "connections": []
		      },
		      {
		        "name": "x",
		        "type": "scalar,axis",
		        "connections": []
		      },
		      {
		        "name": "B30",
		        "type": "scalar",
		        "connections": [
	                "#Blur_0/input"
		        ]
		      },
		      {
		        "name": "B40",
		        "type": "scalar",
		        "connections": []
		      },
		      {
		        "name": "B50",
		        "type": "scalar",
		        "connections": []
		      }
		    ],
		    "qualname": "rsa:small_landsat/100m",
		    "inputs": [],
		    "name": "small_landsat",
		    "type": "input",
		    "id": "small_landsat_0"
		  },
		  {
		    "position": {
		      "x": 419.5,
		      "y": 208.7890625,
		      "left": 351,
		      "top": 189
		    },
		    "description": "Blur pixel using 2D Gaussian kernel",
		    "outputs": [
		      {
		        "name": "output",
		        "type": "Cell",
		        "connections": ["#output/band"]
		      }
		    ],
		    "qualname": "org.vpac.ndg.query.Blur",
		    "inputs": [
		      {
		        "name": "input",
		        "type": "PixelSource",
		        "connections": [
		          "#small_landsat_0/B30"
		        ]
		      }
		    ],
		    "name": "Blur",
		    "type": "filter",
		    "id": "Blur_0"
		  } ];
		
		//check if the dataset name and bands are specified in the URL, modify the initial 
		//graph editor setup accordingly
		//format of the URL needs to be something like (note the / is url encoded as %2F)
		//   . . . app/index.html#?dataset=rsa:test01%2F25m&bands=13117,13125
		if (($location.search()).dataset !== undefined && ($location.search()).bands !== undefined) {
			
			var datasetqualname = ($location.search()).dataset;
			var bands = ($location.search()).bands;
			
			var datasetname = vpac.datasetNameFromQualname(datasetqualname);
			
			var dsnode = nodes[1];
			dsnode.name = datasetname;
			dsnode.qualname = datasetqualname;
			
			var oldoutputs = dsnode.outputs;
			var newoutputs = [];
			
			for (var i = 0; i < 4; i++) {
				//add the first 4 outputs (grid, time, y, x) to the new list of outputs for this node
				newoutputs.push(oldoutputs[i]);
			}
			
			var bandlist = bands.split(",");
			for (var i = 0; i < bandlist.length; i++) {
				//add a new output for each of the bands included in the URL
				var newoutput = {
				        "name": bandlist[i],
				        "type": "scalar",
				        "connections": []
				      };
				newoutputs.push(newoutput);
			}
			dsnode.outputs = newoutputs;

		}
		
		
		return nodes;
	};
	
	$scope.nodes = initnodes();
});


angular.module('graphEditor').controller('node', function($scope, $element, $dialog) {
	console.log('Node controller', $scope.node.name);

	$scope.destroyNode = function(nodes, node) {
		console.log($scope);
		var i = vpac.indexOf(nodes, node);
		console.log('Destroying node', node.name, i);
		nodes.splice(i, 1);
	};
	$scope.canDestroyNode = function() {
		return $scope.node.type != 'output';
	}

	// SOCKETS

	// When a socket is created, it calls this function. This queues up
	// redraw of the connections and endpoints, because jsPlumb can't
	// calculate the offsets until Angular has finished doing its thang.
	$scope.socketsChanged = function() {
		console.log('Sockets changed:', $scope.node.name);
		$scope.dirty = true;
	};

	$scope.getSocketName = function(socket) {
		var lowerCaseFirstChar = function(str) {
			return str.charAt(0).toLowerCase() + str.substr(1);
		};
		var match = /^(input|output)(.+)$/.exec(socket.name);
		if (match != null)
			return lowerCaseFirstChar(match[2]);
		var match = /^(in|out)(.+)$/.exec(socket.name);
		if (match != null && match[2] != 'put')
			return lowerCaseFirstChar(match[2]);

		// Just return original name. No camel case here, so don't change case.
		return socket.name;
	};

	$scope.showValue = function(socket) {
		return vpac.socket.isLiteral(socket);
	};

	$scope.canAddSocket = function(direction) {
		if ($scope.node.type == 'output' && direction == 'input')
			return true;
		else if ($scope.node.type == 'input' && direction == 'output')
			return true;
		return false;
	};

	$scope.canRemoveSocket = function(socket) {
		return socket.synthetic === true;
	};

	$scope.addSocket = function(direction) {
		var socket = {
			type: 'synthetic',
			connections: [],
			synthetic: true
		};
		if (direction == 'input') {
			// Find unique name.
			var socketSet = {};
			for (var i = 0; i < $scope.node.inputs.length; i++) {
				var otherSocket = $scope.node.inputs[i];
				socketSet[otherSocket.name] = true;
			}
			var j = 0;
			var name;
			while (true) {
				name = "Band" + j;
				if (!(name in socketSet))
					break;
				j++;
			}
			socket.name = name;
			$scope.node.inputs.push(socket);
		} else {
			// Use a name that is a regular expression matching any other socket
			// that starts with a B (e.g. B20, B30, Band1, etc.)
			socket.name = "B.*";
			$scope.node.outputs.push(socket);
		}
	};

	var editInputNodeSocket = function(socket) {
		var d = $dialog.dialog({
			controller: 'editSocketDialog',
			resolve: {
				socket: function() {
					return angular.copy(socket);
				},
				node: function() {
					return angular.copy($scope.node);
				}
			},
			templateUrl: 'template/inputSocketDialog.html'
		});
		d.open().then(function(result) {
			console.log('Dialog result:', result, $scope);
			if (!result)
				return;

			var oldRef = vpac.socket.toRef($scope.node, socket);
			socket.name = result.name;
			// Update references to this socket.
			$scope.$emit('socketRefChanged', false,
					vpac.socket.toRef($scope.node, socket),
					oldRef);
			// The width may have changed, so queue a redraw.
			$scope.dirty = true;
		});
	};
	var editOutputNodeSocket = function(socket) {
		var d = $dialog.dialog({
			controller: 'editSocketDialog',
			resolve: {
				socket: function() {
					return angular.copy(socket);
				},
				node: function() {
					return angular.copy($scope.node);
				}
			},
			templateUrl: 'template/outputSocketDialog.html'
		});
		d.open().then(function(result) {
			console.log('Dialog result:', result, $scope);
			if (!result)
				return;

			var oldRef = vpac.socket.toRef($scope.node, socket);
			socket.name = result.name;
			// Update references to this socket.
			$scope.$emit('socketRefChanged', true,
					vpac.socket.toRef($scope.node, socket),
					oldRef);
			// The width may have changed, so queue a redraw.
			$scope.dirty = true;
		});
	};
	var editFilterLiteralInputSocket = function(socket) {
		var d = $dialog.dialog({
			controller: 'editSocketDialog',
			resolve: {
				socket: function() {
					return angular.copy(socket);
				},
				node: function() {
					return angular.copy($scope.node);
				}
			},
			templateUrl: 'template/literalSocketDialog.html'
		});
		d.open().then(function(result) {
			console.log('Dialog result:', result, $scope);
			if (!result)
				return;
			socket.value = result.value;
			// The width may have changed, so queue a redraw.
			$scope.dirty = true;
		});
	};
	$scope.editSocket = function(socket) {
		console.log('editSocket', socket);
		if ($scope.node.type == 'output' && socket.synthetic)
			editOutputNodeSocket(socket);
		else if ($scope.node.type == 'input' && socket.synthetic)
			editInputNodeSocket(socket);
		else if ($scope.node.type == 'filter' && vpac.socket.isLiteral(socket))
			editFilterLiteralInputSocket(socket);
		else
			console.log("Can't edit " + socket.type + " socket of " + $scope.node.type + " node.");
	};

	$scope.destroySocket = function(socket) {
		console.log('destroySocket', socket);
		var i;
		i = vpac.indexOf($scope.node.inputs, socket);
		if (i >= 0) {
			console.log('Destroying input socket', socket.name, i);
			$scope.node.inputs.splice(i, 1);
		} else {
			i = vpac.indexOf($scope.node.outputs, socket);
			console.log('Destroying output socket', socket.name, i);
			$scope.node.outputs.splice(i, 1);
		}
	};
});

angular.module('graphEditor').controller('editSocketDialog', function($scope, dialog, socket, node) {
	console.log('editSocket scope', $scope);
	$scope.socket = socket;
	$scope.node = node;

	$scope.close = function(socket) {
		dialog.close(socket);
	};
});