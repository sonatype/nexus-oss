/* 
 *	Always On Top extension for Ext JS 4.x
 *
 *	Copyright (c) 2011 Eirik Lorentsen (http://www.eirik.net/)
 *
 *	Examples and documentation at: http://www.eirik.net/Ext/ux/util/AlwaysOnTop.html
 *
 *	Dual licensed under the MIT (http://www.opensource.org/licenses/mit-license.php) 
 *	and GPL (http://www.opensource.org/licenses/gpl-license.php) licenses.
 *
 *	Version: 1.1
 *	Last changed date: 2011-12-22
 */

Ext.define('Ext.ux.util.AlwaysOnTop', {
	extend: 'Ext.app.Controller',

	alwaysOnTopManager: null,

	init: function() {
		this.control({
			'component{isFloating()}': {
				'render': function (component, options) {
					this.onComponentRender(component, options);
				}
			}
		});
		/* Uncommenting the code below makes sure that all Ext.window.MessageBoxes stay on top. */
		/*
		Ext.override(Ext.window.MessageBox, {
			alwaysOnTop: true
		});
		*/
		/* Uncommenting the code below makes sure that all form errormessages stay on top.
		   Necessary if you have a form inside a alwaysOnTop window. */
		/*
		Ext.override(Ext.tip.ToolTip, {
			alwaysOnTop: true
		});
		*/
	},

	onComponentRender: function (component, options) {
		if (component.alwaysOnTop) {
			if (!this.alwaysOnTopManager) {
				this.alwaysOnTopManager = Ext.create('Ext.ZIndexManager');
			}
			this.alwaysOnTopManager.register(component);
		}
		if (this.alwaysOnTopManager) {
			/* Making sure the alwaysOnTopManager always has the highest zseed */
			if (Ext.ZIndexManager.zBase > this.alwaysOnTopManager.zseed) {
				this.alwaysOnTopManager.zseed = this.alwaysOnTopManager.getNextZSeed();
			}
		}
	}

});


/*    Changelog:
 *
 *    2011-09-01 - 1.1: Changed ComponentQuery from matching windows to all floating components. And renamed extension from Ext.ux.window.AlwaysOnTop to Ext.ux.util.AlwaysOnTop
 *
 */  
