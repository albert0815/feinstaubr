class MvgComponent extends HTMLElement {
	
	connectedCallback() {
		console.info( 'connected' );
		console.log("huhu");
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition((position) =>
			{
				this.checkStations(position);
			}, () => this.checkStations()
			);
		} else {
			this.checkStations();
		}
	}
	
	checkStations(position) {
		console.log("this: " + this);
		console.log(position);
		var url = "/rest/mvg";
		if (position) {
			url = url + "/" + position.coords.latitude + "/" + position.coords.longitude
		}
		
		fetch(url).
		then (r => r.json()).then(
				stations => {
					console.log(stations);
					var result = `
						<md-grid-component>
					`;
					for (var i = 0; i < stations.length; i++) {
						result += `
						<div class="mdc-layout-grid__cell">
							<md-card-component card-title="${stations[i].name}">
								<md-list-component>
						`;
						for (var j = 0; j < stations[i].departures.length; j++) {
							var d = new Date(stations[i].departures[j].departureTime);
							var time = this.pad(d.getHours()) + ":" + this.pad(d.getMinutes());
							var icon;
							switch (stations[i].departures[j].product) {
							case "BUS": icon = 'directions_bus'; break;
							case "SUBWAY": icon = 'directions_railway'; break;
							case "TRAM": icon = 'directions_railway'; break;
							case "TRAIN": icon = 'directions_railway'; break;
							}

							result += `
									<md-list-entry-component icon="${icon}">
										${time} ${stations[i].departures[j].line} ${stations[i].departures[j].destination}
									</md-list-entry-component>
								`;
						}
						result += `
								</md-list-component>
							</md-card-component>
						</div>
								`;
					}
					result += `
							</md-grid-component>
						`;
					this.innerHTML = result;
					this.dispatchEvent(new Event('loading-done'));
				}
		);
	}
	
	pad(n) {
		return n<10 ? '0'+n : n;
	}

	constructor() {
		super();
	}
}
customElements.define("mvg-component", MvgComponent);
