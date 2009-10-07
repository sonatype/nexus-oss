// vim: sw=4:ts=4:nu:nospell:fdc=4
/**
 * WebPage Layout Generator
 *
 * @author    Ing. Jozef Sakáloš
 * @copyright (c) 2008, by Ing. Jozef Sakáloš
 * @date      6. April 2008
 * @version   $Id: WebPage.js 169 2008-04-11 21:10:41Z jozo $
 *
 * @license WebPage.js is licensed under the terms of the Open Source
 * LGPL 3.0 license. Commercial use is permitted to the extent that the 
 * code/component(s) do NOT become part of another Open Source or Commercially
 * licensed development library or toolkit without explicit permission.
 * 
 * License details: http://www.gnu.org/licenses/lgpl.html
 */
 
/*global Ext, WebPage */
 
Ext.ns('WebPage');
 
WebPage = function(config) {
	Ext.apply(this, config, {
		 autoRender:true
		,autoTitle:true
		,langCombo:false
		,ctCreate:{tag:'div', id:'ct-wrap', cn:[{tag:'div', id:'ct'}]}
	});

	if(this.autoRender) {
		this.render();
	}
};

Ext.override(WebPage, {
	 navlinks:[{
		 text:'Home'
		,href:'http://extjs.eu'
		,target:'_self'
	},{
		 text:'Blog'
		,href:'http://blog.extjs.eu'
		,target:'_self'
	},{
		 text:'Examples'
		,href:'http://examples.extjs.eu'
		,target:'_blank'
	},{
		 text:'ExtJS'
		,href:'http://extjs.com'
		,target:'_blank'
	},{
		 text:'Forum'
		,href:'http://extjs.com/forum'
		,target:'_blank'
	},{
		 text:'Learn'
		,href:'http://extjs.com/learn'
		,target:'_blank'
	},{
		 text:'Docs'
		,href:'http://extjs.com/deploy/dev/docs'
		,target:'_blank'
	},{
		 text:'Samples'
		,href:'http://extjs.com/deploy/dev/examples'
		,target:'_blank'
	},{
		 text:'Profile'
		,href:'http://extjs.com/forum/member.php?u=2178'
		,target:'_blank'
	}]
	,navlinksTpl: new Ext.XTemplate(
		 '<ul>'
		+'<tpl for="navlinks">'
		+'<li><a href="{href}" target="{target}">{text}</a></li>'
		+'</tpl>'
		+'</ul><div class="cleaner"></div>'
	)
	,render:function() {
		var body = Ext.getBody();
		var dh = Ext.DomHelper;

		// create wrap and container
		this.wrap = dh.insertFirst(body, this.ctCreate, true);
		this.ct = Ext.get('ct');

		if(this.width) {
			this.ct.setWidth(this.width);
		}

		this.north = dh.append(this.ct, {tag:'div', id:'north'}, true);
		if(this.northHeight) {
			this.north.setHeight(this.northHeight);
		}

		this.nav = dh.append(this.ct, {tag:'div', id:'navlinks'}, true);
		if(this.navHeight) {
			this.nav.setHeight(this.navHeight);
		}

		if(this.adRowContent) {
			this.adrow = dh.append(this.ct, {tag:'div', id:'adrow'}, true);
			if(this.adrowHeight) {
				this.adrow.setHeight(this.adrowHeight);
			}
		}

		this.west = dh.append(this.ct, {tag:'div', id:'west'}, true);
		if(this.westWidth) {
			this.west.setWidth(this.westWidth);
		}

		this.center = dh.append(this.ct, {tag:'div', id:'center'}, true);
		if(this.westWidth && this.eastWidth) {
			this.center.setWidth(this.ct.getWidth() - this.westWidth - this.eastWidth);
		}

		this.east = dh.append(this.ct, {tag:'div', id:'east'}, true);
		if(this.eastWidth) {
			this.east.setWidth(this.eastWidth);
		}

		this.south = dh.append(this.ct, {tag:'div', id:'south'}, true);
		if(this.southHeight) {
			this.south.setHeight(this.southHeight);
		}

		// {{{
		// north content
		if(this.northContent) {
			this.north.appendChild(this.northContent);
			this.northContent = Ext.get(this.northContent).removeClass('x-hidden');
		}
		else if(this.autoTitle) {
			var title = Ext.fly('page-title');
			if(title) {
				title = title.dom.innerHTML;
				title += this.version ? ' - ver.: ' + this.version : '';
			}
			this.north.createChild({tag:'h1', html:title});
		}

		// theme select combo
		var themeCt = this.north.createChild({tag:'div', id:'themect', cls:'x-hidden'});
		this.themeCombo = new Ext.ux.ThemeCombo({
			 renderTo:themeCt
			,width:themeCt.getWidth()
		});

		// language select combo
		if(this.langCombo) {
			var langCt = this.north.createChild({tag:'div', id:'langct', cls:'x-hidden'});
			this.langCombo = new Ext.ux.LangSelectCombo({
				 renderTo:langCt
				,width:langCt.getWidth()
				,editable:false
			});
		}
		// }}}

		if(this.navlinks) {
			this.navlinksTpl.overwrite(this.nav, {navlinks:this.navlinks});
		}

		if(this.adrow) {
			this.adrow.appendChild(this.adRowContent);
			this.adRowContent = Ext.get(this.adRowContent).removeClass('x-hidden');
		}

		if(this.westContent) {
			this.west.appendChild(this.westContent);
			this.westContent = Ext.get(this.westContent).removeClass('x-hidden');
		}

		if(this.centerContent) {
			this.center.appendChild(this.centerContent);
			this.centerContent = Ext.get(this.centerContent).removeClass('x-hidden');
		}

		if(this.eastContent) {
			this.east.appendChild(this.eastContent);
			this.eastContent = Ext.get(this.eastContent).removeClass('x-hidden');
		}

		if(this.southContent) {
			this.south.appendChild(this.southContent);
			this.southContent = Ext.get(this.southContent).removeClass('x-hidden');
		}

		(function() {
			themeCt.removeClass('x-hidden');
			if(langCt) {
				langCt.removeClass('x-hidden');
			}
		}).defer(250)

	} // eo function render
});
 
// eof
