(function() {

	const template = document.createElement('template');
	template.innerHTML = `
			<link rel="stylesheet" href="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css" />
			
			<header class="mdc-toolbar mdc-toolbar--fixed">
				<div class="mdc-toolbar__row">
					<section class="mdc-toolbar__section mdc-toolbar__section--align-start">
						<slot name="menu"></slot>
					</section>
				</div>
				<slot name="belowMenu"></slot>
			</header>
		`;	
	
	class MaterialDesignMenuComponent extends HTMLElement {
		connectedCallback() {
			console.log(this.querySelector('md-loading-component'));
			console.log("menu connected");
			var loadingBar = "";
			if (this.querySelector('md-loading-component')) {
				loadingBar = this.querySelector('md-loading-component').outerHTML;
			}
	//		this.appendChild(template.content.cloneNode(true));
			
			const shadowRoot = this.attachShadow({mode: 'open'}).appendChild(template.content.cloneNode(true));
		}
		
		static get observedAttributes() {
			//name observed attributes 
			return ['active']; 
		}
	}
	customElements.define("md-menu-component", MaterialDesignMenuComponent);
})();

(function() {
	const template = document.createElement('template');
	template.innerHTML = `
		<div role="progressbar" class="mdc-linear-progress mdc-linear-progress--indeterminate">
			<div class="mdc-linear-progress__buffering-dots"></div>
			<div class="mdc-linear-progress__buffer"></div>
			<div class="mdc-linear-progress__bar mdc-linear-progress__primary-bar">
				<span class="mdc-linear-progress__bar-inner"></span>
			</div>
			<div class="mdc-linear-progress__bar mdc-linear-progress__secondary-bar">
				<span class="mdc-linear-progress__bar-inner"></span>
			</div>
		</div>				
	`;
	
	class MaterialDesignLoadingComponent extends HTMLElement {
		connectedCallback() {
			console.log("loading connected");
			//this.appendChild(template.content.cloneNode(true));
			//mdc.linearProgress.MDCLinearProgress.attachTo(this)
		}
		
		attributeChangedCallback(attrName, oldVal, newVal) {
			console.log("value changed");
			if (attrName == 'active') {
				if (newVal === 'true' && this.childNodes.length == 0) {
					this.appendChild(template.content.cloneNode(true));
				} else {
					this.innerHTML = '';
				}
			}
		}
	
		
		static get observedAttributes() {
			return ['active']; 
		}
	}
	customElements.define("md-loading-component", MaterialDesignLoadingComponent);
})();

(function() {
	const template = document.createElement('template');
	template.innerHTML = `
		<link rel="stylesheet" href="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css" />
		<div class="mdc-layout-grid">
			<div class="mdc-layout-grid__inner">
				<slot></slot>
			</div>
		</div>
	`;

	class MaterialDesignGridComponent extends HTMLElement {
		connectedCallback() {
			this.root = this.attachShadow({mode: 'open'});
			this.root.appendChild(template.content.cloneNode(true));
		}
	}
	customElements.define("md-grid-component", MaterialDesignGridComponent);
})();

(function() {
	const template = document.createElement('template');
	template.innerHTML = `
		<link rel="stylesheet" href="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css" />
		<div class="mdc-card">
			<h2 class="mdc-typography--title" style="padding-left:1rem"></h2>
			<slot></slot>
		</div>
	`;

	class MaterialCardGridComponent extends HTMLElement {
		connectedCallback() {
			this.root = this.attachShadow({mode: 'open'});
			this.root.appendChild(template.content.cloneNode(true));
			var title = this.getAttribute("card-title");
			this.root.querySelector("h2").innerHTML = title;
		}
	}
	customElements.define("md-card-component", MaterialCardGridComponent);
})();


(function() {
	const template = document.createElement('template');
	template.innerHTML = `
		<link rel="stylesheet" href="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css" />
		<ul class="mdc-list">
			<slot></slot>
		</ul>
	`;

	class MaterialListComponent extends HTMLElement {
		connectedCallback() {
			this.root = this.attachShadow({mode: 'open'});
			this.root.appendChild(template.content.cloneNode(true));
		}
	}
	customElements.define("md-list-component", MaterialListComponent);
})();

(function() {
	const template = document.createElement('template');
	template.innerHTML = `
		<link rel="stylesheet" href="https://unpkg.com/material-components-web@latest/dist/material-components-web.min.css" />
		<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons"/>
		<li class="mdc-list-item">
			<span class="mdc-list-item__graphic">
				<i class="material-icons" aria-hidden="true"></i>
			</span>
			<span class="mdc-list-item__text">
				<slot></slot>
			</span>
		</li>
	`;

	class MaterialListEntryComponent extends HTMLElement {
		connectedCallback() {
			this.root = this.attachShadow({mode: 'open'});
			this.root.appendChild(template.content.cloneNode(true));
			var icon = this.getAttribute("icon");
			this.root.querySelector("i").innerHTML = icon;
		}
	}
	customElements.define("md-list-entry-component", MaterialListEntryComponent);
})();


