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
	dataTable.addColumn('number', 'Vorhersage');
	var arrayLength = chartData.length;
	for (var i = 0; i < arrayLength; i++) {
		var xAxisDate = new Date(chartData[i].forecastDate);
		var yValue = chartData[i].temperature;
		dataTable.addRow([xAxisDate, yValue]);
	}
	var formatDate = new google.visualization.DateFormat({pattern: 'dd.MM.yyyy HH:mm'});
	formatDate.format(dataTable, 0);
	var chart = new google.visualization.LineChart(document.getElementById("forecastchart"));
	var formatString = "dd.MM. HH:mm";
	var minDate = new Date();
	minDate.setHours(0);
	minDate.setMinutes(0);
	minDate.setSeconds(0);
	minDate.setMilliseconds(0);
	var maxDate = new Date();
	maxDate.setHours(23);
	maxDate.setMinutes(59);
	maxDate.setSeconds(59);
	maxDate.setMilliseconds(0);
	minDate.setDate(minDate.getDate() + 3);
	
	var options = {
	        width: '100%',
	        height: '100%',
	        chartArea: {
	            left: "10%",
	            top: "7%",
	            height: "80%",
	            width: "80%"
	        },
//	        focusTarget : 'category',
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
//				count : gridCount,
			},
		    viewWindow: {
		        min: minDate,
		        max: maxDate
		    },
	        format: formatString
		}
	};

	chart.draw(dataTable, options);
}