	var reloadEvent;
	var lastUpdate;
	const TIMEOUT=900000;
	var chartData;
	var jqXHR;
	
	var labels = new Map();
	labels.set("temperature", "Temperatur in °C");
	labels.set("humidity", "Luftfeuchtigkeit in %");
	labels.set("p2", "Feinstaub PM2.5 in μg/m³");
	labels.set("p1", "Feinstaub PM10 in μg/m³");

	
	function loadData() {
		$("#progressbar").css("display", "block");
		jqXHR = $.getJSON("rest/sensor/7620363/" + getPeriod())
			.done(function(data) {
				chartData = data;
				drawCurrentMinMax();
				drawCharts();
			}).fail(function(jqXHR, status, error) {
				if (jqXHR.statusText !== "abort") {
					$(".mdc-snackbar__text").html("Ein Fehler ist aufgetreten - " + error);
					$(".mdc-snackbar").addClass("mdc-snackbar--active");
				}
			}).always(function() {
				jqXHR = undefined;
				lastUpdate = new Date();
				$("#progressbar").css("display", "none");
			});
	}

	function drawCurrentMinMax() {
		if (chartData.current) {
			$("#current .val_date").html(convertDate(chartData.current.date));
			$("#current .val_temperature").html(chartData.current.temperature.toLocaleString() + " °C");
			$("#current .val_humidity").html(chartData.current.humidity.toLocaleString() + " %");
			$("#current .val_p1").html(chartData.current.p1.toLocaleString() + " μg/m³");
			$("#current .val_p2").html(chartData.current.p2.toLocaleString() + " μg/m³");
		}
		
		$.each(chartData.details, function (detailType, values) {
			var label;
			switch (detailType) {
			case "temperature":   label = " °C"; break;
			case "humidity":      label = " %"; break;
			case "p1": case "p2": label = " μg/m³"; break;
			}
			for (var key in values) {
				var value = values[key].value;
				if (typeof value === 'number') {
					value = value.toLocaleString();
				}
				value = value + label;

				if (values[key].date) {
					if (getPeriod() === "day") {
						var d = new Date(values[key].date); 
						value = value + " (" + pad(d.getHours()) + ":" + pad(d.getMinutes()) + " Uhr)";
					} else {
						value = value + " (" + convertDate(values[key].date) + ")";
					}
				}
				$("#" + detailType + " .val_" + key).html(value);
			}
		});
	}
	
	function convertDate(date) {
		var d = new Date(date);
		return pad(d.getDate()) + "." + pad(d.getMonth() + 1) + "." + d.getFullYear() + 
		" " + pad(d.getHours()) + ":" + pad(d.getMinutes()) + ":" + pad(d.getSeconds());
	}
	
	function pad(n) {
		return n<10 ? '0'+n : n;
	}
	
	function getPeriod() {
		var period = location.hash.substr(1);
		if (!period) {
			period = "day";
		}
		return period;
	}

	function drawCharts() {
		$.each(chartData.charts, function (sensorType, data) {
			var dataTable = new google.visualization.DataTable();
			dataTable.addColumn('datetime', 'Time');
			dataTable.addColumn('number', labels.get(sensorType));
			var arrayLength = data.length;
			for (var i = 0; i < arrayLength; i++) {
				var xAxisDate = new Date(data[i][0] * 100000);
				var yValue = data[i][1];
				dataTable.addRow([xAxisDate, yValue]);
			}
			var formatDate;
			if (getPeriod() === "year") {
				formatDate = new google.visualization.DateFormat({pattern: 'dd.MM.yyyy'});
			} else {
				formatDate = new google.visualization.DateFormat({pattern: 'dd.MM.yyyy HH:mm'});
			}
			formatDate.format(dataTable, 0);
			var chart = new google.visualization.LineChart(document.getElementById(sensorType));
			var formatString = "dd.MM.";
			if (getPeriod() === "day") {
				formatString = "HH:mm";
			}
			var minDate = new Date();
			minDate.setHours(0);
			minDate.setMinutes(0);
			minDate.setSeconds(0);
			minDate.setMilliseconds(0);
			
			switch (getPeriod()) {
				case "week": minDate.setDate(minDate.getDate() - 6); break;
				case "month": minDate.setDate(minDate.getDate() - 28); break;
				case "year": minDate.setDate(minDate.getDate() - 365); break;
			}
			
			var gridCount;
			if ($(window).width() < 720) {
				gridCount = 5;
			}
			else {
				gridCount = 8;
			}
			
			var options = {
			        width: '100%',
			        height: '100%',
			        chartArea: {
			            left: "10%",
			            top: "7%",
			            height: "80%",
			            width: "80%"
			        },
			        focusTarget : 'category',
				legend : {
					position : 'none'
				},
				curveType : 'function',
				vAxis : {
					minValue : 0,
				},
				hAxis : {
					gridlines : {
						count : gridCount,
					},
				    viewWindow: {
				        min: minDate,
				        max: new Date()
				    },
			        format: formatString
				}
			};
	
			chart.draw(dataTable, options);
		});
	}
	
	function scheduleNextLoad(timeout) {
		reloadEvent = setTimeout(loadData, timeout);
	}

	$(window).on("resize", function (event) {
		drawCharts();
	});
	
	function getHiddenProp(){
		//FIXME replace with function from google material components
	    var prefixes = ['webkit','moz','ms','o'];
	    
	    // if 'hidden' is natively supported just return it
	    if ('hidden' in document) return 'hidden';
	    
	    // otherwise loop over all the known prefixes until we find one
	    for (var i = 0; i < prefixes.length; i++){
	        if ((prefixes[i] + 'Hidden') in document) 
	            return prefixes[i] + 'Hidden';
	    }

	    // otherwise it's not supported
	    return null;
	}
	
	function isHidden() {
	    var prop = getHiddenProp();
	    if (!prop) return false;
	    
	    return document[prop];
	}
	
	function visChange() {
		if (jqXHR) {
			return;
		}
	   if (isHidden()) {
		   clearTimeout(reloadEvent);
	   } else {
		   diff = new Date() - lastUpdate;
		   if (diff > TIMEOUT) {
			   loadData();
		   } else {
			   scheduleNextLoad(TIMEOUT - diff);
		   }
	   }
	   var txtFld = document.getElementById('visChangeText');

	   if (txtFld) {
	      if (isHidden())
	         txtFld.value += "Tab Hidden!\n";
	      else
	         txtFld.value += "Tab Visible!\n";
	   }
	}
	
	function updateActiveTab() {
		var linkToCurrentPeriod = $('a[href="#' + getPeriod() + '"]');
		$(".mdc-tab").removeClass("mdc-tab--active");
		$(linkToCurrentPeriod).addClass("mdc-tab--active");
	}
	
	window.onhashchange = function() {
		//change active tabb
		updateActiveTab();
		
		//abort all running ajax requests
		if (jqXHR) {
			jqXHR.abort();
		}
		
		loadData();
	}
