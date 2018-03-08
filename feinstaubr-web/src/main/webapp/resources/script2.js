var chartData;

function loadData() {
	
        var drawer = new mdc.drawer.MDCTemporaryDrawer(document.querySelector('.mdc-drawer--temporary'));
        document.querySelector('.menu').addEventListener('click', () => {console.log("clicked");drawer.open = true;});
        
		var jqForecastXHR = $.getJSON("rest/forecast/10865/3")
		.done(function(data) {
			chartData = data;
			drawChart();
		}).always(function() {
			$("#progressbar").css("display", "none");
		});

}


function drawChart() {
	var dataTable = new google.visualization.DataTable();
	dataTable.addColumn('datetime', 'Zeit');
//	dataTable.addColumn({type: 'string', role: 'annotation'});
	dataTable.addColumn('number', 'Vorhersage');
	var arrayLength = chartData.length;
	for (var i = 0; i < arrayLength; i++) {
		var xAxisDate = new Date(chartData[i].forecastDate);
		var yValue = chartData[i].temperature;
		dataTable.addRow([xAxisDate, /*null, */yValue]);
	}
//	dataTable.addRow([new Date(), "Jetzt", null]);
	var formatDate = new google.visualization.DateFormat({pattern: 'EE HH:mm'});
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