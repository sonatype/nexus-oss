/* Load this script using conditional IE comments if you need to support IE 7 and IE 6. */

window.onload = function() {
	function addIcon(el, entity) {
		var html = el.innerHTML;
		el.innerHTML = '<span style="font-family: \'icomoon\'">' + entity + '</span>' + html;
	}
	var icons = {
			'icon-file-css' : '&#xe00f;',
			'icon-file' : '&#xe001;',
			'icon-file-2' : '&#xe010;',
			'icon-stats' : '&#xe011;',
			'icon-file-xml' : '&#xe000;',
			'icon-checkmark' : '&#xe002;',
			'icon-arrow-up' : '&#xe00e;',
			'icon-arrow-down' : '&#xe012;',
			'icon-close' : '&#xe013;',
			'icon-star' : '&#xe014;',
			'icon-star-2' : '&#xe015;',
			'icon-attachment' : '&#xe016;',
			'icon-link' : '&#xe017;',
			'icon-plus' : '&#xe019;',
			'icon-minus' : '&#xe01a;',
			'icon-play' : '&#xe01c;',
			'icon-pause' : '&#xe01d;',
			'icon-stop' : '&#xe01e;',
			'icon-forward' : '&#xe01f;',
			'icon-search' : '&#xe020;',
			'icon-camera' : '&#xe022;',
			'icon-stop-2' : '&#xe023;',
			'icon-cancel-circle' : '&#xe024;',
			'icon-book' : '&#xe003;',
			'icon-cog' : '&#xe004;',
			'icon-screen' : '&#xe005;',
			'icon-keyboard' : '&#xe006;',
			'icon-tag' : '&#xe007;',
			'icon-folder-open' : '&#xe008;',
			'icon-folder' : '&#xe009;',
			'icon-clock' : '&#xe00a;',
			'icon-busy' : '&#xe00b;',
			'icon-spinner' : '&#xe00c;',
			'icon-cog-2' : '&#xe00d;',
			'icon-play-2' : '&#xe01b;',
			'icon-bug' : '&#xe018;'
		},
		els = document.getElementsByTagName('*'),
		i, attr, html, c, el;
	for (i = 0; ; i += 1) {
		el = els[i];
		if(!el) {
			break;
		}
		attr = el.getAttribute('data-icon');
		if (attr) {
			addIcon(el, attr);
		}
		c = el.className;
		c = c.match(/icon-[^\s'"]+/);
		if (c && icons[c[0]]) {
			addIcon(el, icons[c[0]]);
		}
	}
};