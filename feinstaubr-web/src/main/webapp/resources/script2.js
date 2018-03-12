var chartData = [];

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
				populateWeatherList();
			}).always(function() {
				$("#progressbar").css("display", "none");
			});
		});

}

function populateWeatherList() {
	visualizeForecast(chartDataDwd, "#weather-dwd");
	visualizeForecast(chartDataOpenWeather, "#weather-open-weather");
}

function visualizeForecast(forecast, id) {
	var template = document.createElement('template');

	template.innerHTML = `
								<li class="mdc-list-divider" role="separator"></li>
								<li class="mdc-list-item" style="height:auto;">
									<span class="mdc-list-item__graphic">
										<i class="wi" style="font-size:2rem"></i>
									</span>
									<span class="mdc-list-item__text">
										<span class="mdc-list-item__secondary-text"></span>
									</span>
								</li>	
								
	`;
	
	var arrayLength = forecast.length;
	for (var i = 0; i < arrayLength; i++) {
		var date = new Date(forecast[i].forecastDate);
		var today = new Date();
		var tomorrow = new Date();
		tomorrow.setDate(today.getDate() + 1);
		if (date < today) {
			continue;
		}
		var li = template.content.cloneNode(true);
		if (forecast[i].weather) {
			li.querySelector("i").innerHTML = "&#" + forecast[i].weather + ";"
		}
		var dateString;
		if (date.getDate() === today.getDate() && date.getMonth() === today.getMonth() && date.getFullYear() === today.getFullYear()) {
			dateString = "Heute";
		} else if (date.getDate() === tomorrow.getDate() && date.getMonth() === tomorrow.getMonth() && date.getFullYear() === tomorrow.getFullYear()) {
			dateString = "Morgen";
		} else {
			dateString = pad(date.getDate()) + "." + pad(date.getMonth() + 1) + "." + pad(date.getFullYear());
		}
		dateString = dateString + " " + pad(date.getHours()) + ":" + pad(date.getMinutes());
		li.querySelector(".mdc-list-item__text").prepend(document.createTextNode(dateString));
		li.querySelector(".mdc-list-item__secondary-text").innerHTML = 
						"Temperatur: " + forecast[i].temperature.toLocaleString() + "° C<br />"+
						"Niederschlag: " + forecast[i].precipitation.toLocaleString() + " ml <br />" +
						"Bewölkung: " + forecast[i].cloudCover.toLocaleString() + "% <br />" +
						"Luftdruck: " + forecast[i].pressure.toLocaleString() + " hPA <br />";
		document.querySelector(id).appendChild(li);
	}

}
function pad(n) {
	return n<10 ? '0'+n : n;
}


function drawCharts() {
	var dataTable = new google.visualization.DataTable();
	dataTable.addColumn('datetime', 'Zeit');
//	dataTable.addColumn({type: 'string', role: 'annotation'});
	dataTable.addColumn('number', 'Temperatur DWD');
	dataTable.addColumn('number', 'Temperatur Open Weather');
	dataTable.addColumn('number', 'Niederschlag DWD');
	dataTable.addColumn('number', 'Niederschlag Open Weather');

	var arrayLength = chartDataDwd.length;
	for (var i = 0; i < arrayLength; i++) {
		var xAxisDate = new Date(chartDataDwd[i].forecastDate);
		var yValue = chartDataDwd[i].temperature;
		dataTable.addRow([xAxisDate, /*null, */yValue, null, chartDataDwd[i].precipitation, null]);
	}
	arrayLength = chartDataOpenWeather.length;
	for (var i = 0; i < arrayLength; i++) {
		var xAxisDate = new Date(chartDataOpenWeather[i].forecastDate);
		var yValue = chartDataOpenWeather[i].temperature;
		dataTable.addRow([xAxisDate, /*null, */null, yValue, null, chartDataOpenWeather[i].precipitation]);
	}
	
	var formatDate = new google.visualization.DateFormat({pattern: 'EEHH:mm'});
	formatDate.format(dataTable, 0);
	var formatCelsius = new google.visualization.NumberFormat({suffix: '° C'});
	formatCelsius.format(dataTable, 1);
	formatCelsius.format(dataTable, 2);
	var formatMilliliter = new google.visualization.NumberFormat({suffix: ' ml'});
	formatMilliliter.format(dataTable, 3);
	formatMilliliter.format(dataTable, 4);
	var chart = new google.visualization.ComboChart(document.getElementById("forecastchart"));
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
			seriesType: 'line',
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
            0: {targetAxisIndex:0, color: 'indianred' },
            1: {targetAxisIndex:0, color: 'red'},
            2: {targetAxisIndex:1, type:'bars', color:'lightblue'},
            3: {targetAxisIndex:1, type:'bars', color:'blue'}
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