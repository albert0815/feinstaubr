$("#progressbar").css("display", "block");

$.getJSON("rest/forecast/10865/OPEN_WEATHER")
	.done(function(data) {
		if (data.weather) {
			$("#icon").html("&#" + data.weather + ";");
		}
		var d = new Date(data.forecastDate);
		$("#forecast_time").html(pad(d.getHours()) + ":" + pad(d.getMinutes()) + " Uhr");
		$("#forecast_temp").html(data.temperature.toLocaleString() + "° C");
		$("#forecast_precipitation").html(data.precipitation.toLocaleString() + " ml Niederschlag");
		$("#forecast_clouds").html((data.cloudCover).toLocaleString() + "% Bewölkung");
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
