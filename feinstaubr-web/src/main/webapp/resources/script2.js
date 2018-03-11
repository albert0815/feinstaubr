var chartDataDwd;
var chartDataOpenWeather;

function loadData() {
	
        var drawer = new mdc.drawer.MDCTemporaryDrawer(document.querySelector('.mdc-drawer--temporary'));
        document.querySelector('.menu').addEventListener('click', () => {console.log("clicked");drawer.open = true;});
        
		$.getJSON("rest/forecast/10865/DWD/3")
		.done(function(data) {
			chartDataDwd = data;
			$.getJSON("rest/forecast/10865/OPEN_WEATHER/3")
			.done(function(data) {
				chartDataOpenWeather = data;
				drawCharts();
			}).always(function() {
				$("#progressbar").css("display", "none");
			});
		});

}


function drawCharts() {
	var dataTable = new google.visualization.DataTable();
	dataTable.addColumn('datetime', 'Zeit');
//	dataTable.addColumn({type: 'string', role: 'annotation'});
	dataTable.addColumn('number', 'Vorhersage DWD');
	dataTable.addColumn('number', 'Vorhersage Open Weather');
	var arrayLength = chartDataDwd.length;
	for (var i = 0; i < arrayLength; i++) {
		var xAxisDate = new Date(chartDataDwd[i].forecastDate);
		var yValue = chartDataDwd[i].temperature;
		dataTable.addRow([xAxisDate, /*null, */yValue, null]);
	}
	arrayLength = chartDataOpenWeather.length;
	for (var i = 0; i < arrayLength; i++) {
		var xAxisDate = new Date(chartDataOpenWeather[i].forecastDate);
		var yValue = chartDataOpenWeather[i].temperature;
		dataTable.addRow([xAxisDate, /*null, */null, yValue]);
	}
//	dataTable.addRow([new Date(), "Jetzt", null]);
	var formatDate = new google.visualization.DateFormat({pattern: 'EEHH:mm'});
	formatDate.format(dataTable, 0);
	var chart = new google.visualization.LineChart(document.getElementById("forecastchart"));
	var formatString = "EE HH:mm";
	var minDate = new Date();
	var maxDate = new Date();
	maxDate.setDate(minDate.getDate() + 2);
	maxDate.setHours(23);
	maxDate.setMinutes(59);
	maxDate.setSeconds(59);
	maxDate.setMilliseconds(0);
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
//			minValue : yMinValue,
			format: '0'
		},
		hAxis : {
			gridlines : {
				count : gridCount,
			},
		    viewWindow: {
		        min: minDate,
		        max: maxDate
		    },
	        format: formatString
		},
        series: {
            0: { color: 'red' }
        },
        annotations: {
            stem: {
                color: 'green'
              },
            style: 'line'
        }

	};

	chart.draw(dataTable, options);
}