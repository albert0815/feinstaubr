var chartData = [];

var weekday = new Array(7);
weekday[0] =  "Sonntag";
weekday[1] = "Montag";
weekday[2] = "Dienstag";
weekday[3] = "Mittwoch";
weekday[4] = "Donnerstag";
weekday[5] = "Freitag";
weekday[6] = "Samstag";


function loadData() {
	
        var drawer = new mdc.drawer.MDCTemporaryDrawer(document.querySelector('.mdc-drawer--temporary'));
        document.querySelector('.menu').addEventListener('click', () => {console.log("clicked");drawer.open = true;});
        
		$.getJSON("rest/forecast/home/3")
		.done(function(data) {
			chartData = data;
			drawCharts();
			populateWeatherList();
		}).always(function() {
			$("#progressbar").css("display", "none");
		});

}

function populateWeatherList() {
	visualizeForecast(chartData, "#weather");
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
			dateString = weekday[date.getDay()];
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
	dataTable.addColumn('number', 'Temperatur');
	dataTable.addColumn('number', 'Niederschlag');

	var arrayLength = chartData.length;
	for (var i = 0; i < arrayLength; i++) {
		var xAxisDate = new Date(chartData[i].forecastDate);
		var yValue = chartData[i].temperature;
		dataTable.addRow([xAxisDate, /*null, */ chartData[i].temperature, chartData[i].precipitation]);
	}
	
	var formatDate = new google.visualization.DateFormat({pattern: 'EEHH:mm'});
	formatDate.format(dataTable, 0);
	var formatCelsius = new google.visualization.NumberFormat({suffix: '° C'});
	formatCelsius.format(dataTable, 1);
	var formatMilliliter = new google.visualization.NumberFormat({suffix: ' ml'});
	formatMilliliter.format(dataTable, 2);
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
	
	var columnRange = dataTable.getColumnRange(2);
	if (columnRange.max < 20) {
		precipitationMaxValue = 20;
	} else {
		precipitationMaxValue = columnRange.max;
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
		/*
		vAxis : {
			0: {viewWindow : { min:0, max : precipitationMaxValue}, format: '0'},
			1: {viewWindow : { max : precipitationMaxValue}, format: '0'}
		},*/
		vAxes: {
			 0: {  },
			 1: { viewWindow: { min: 0, max: precipitationMaxValue } }
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
            0: {targetAxisIndex:0, color: 'red' },
            1: {targetAxisIndex:1, type:'bars', color:'blue'},
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