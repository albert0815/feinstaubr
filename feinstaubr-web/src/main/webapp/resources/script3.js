$("#progressbar").css("display", "block");

$.getJSON("rest/forecast/10865")
	.done(function(data) {
		var icon;
		switch (data.weather) {
		case "1": icon = "wi-day-sunny"; break;
		case "2": icon = "wi-day-cloudy"; break;
		case "3": icon = "wi-cloudy"; break;
		case "4": icon = "wi-cloudy"; break;
		case "5": 
		case "6": icon = "wi-fog"; break;
		case "7":
		case "8": 
		case "9": icon = "wi-rain"; break;
		case "10":
		case "11": icon = "wi-hail"; break;
		case "12": 
		case "13": icon = "wi-rain-mix"; break;
		case "14": 
		case "15": 
		case "16": icon = "wi-snow"; break;
		case "17": icon = "wi-sleet"; break;
		case "18": 
		case "19": icon = "wi-showers"; break;
		case "20": 
		case "21": 
		case "22": 
		case "23": icon = "wi-snow"; break;
		case "24":
		case "25": icon = "wi-sleet"; break;
		case "26": icon = "wi-lightning"; break;
		case "27": 
		case "28": icon = "wi-thunderstorm"; break;
		case "29": 
		case "30": icon = "wi-storm-showers"; break;
		case "31": icon = "wi-strong-wind"; break;
		}
		$("#icon").addClass(icon);
		var d = new Date(data.forecastDate);
		$("#forecast").html(pad(d.getHours()) + ":" + pad(d.getMinutes()) + " Uhr: " + data.temperature + "° C, " + data.chanceOfRain + "% Regenwahrscheinlichkeit");
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


function pad(n) {
	return n<10 ? '0'+n : n;
}
