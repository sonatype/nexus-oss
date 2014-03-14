/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-09-18 17:18:59 (940c324ac822b840618a3a8b2b4b873f83a1a9b1)
*/
/**
 * Adds a separator bar to a menu, used to divide logical groups of menu items. Generally you will
 * add one of these by using "-" in your call to add() or in your items config rather than creating one directly.
 *
 *     @example
 *     Ext.create('Ext.menu.Menu', {
 *         width: 100,
 *         height: 100,
 *         floating: false,  // usually you want this set to True (default)
 *         renderTo: Ext.getBody(),  // usually rendered by it's containing component
 *         items: [{
 *             text: 'icon item',
 *             iconCls: 'add16'
 *         },{
 *             xtype: 'menuseparator'
 *         },{
 *            text: 'separator above'
 *         },{
 *            text: 'regular item'
 *         }]
 *     });
 */
Ext.define('Ext.menu.Separator', {
    extend: 'Ext.menu.Item',
    alias: 'widget.menuseparator',

    /**
     * @cfg {String} activeCls
     * @private
     */

    /**
     * @cfg {Boolean} canActivate
     * @private
     */
    canActivate: false,

    /**
     * @cfg {Boolean} clickHideDelay
     * @private
     */

    /**
     * @cfg {Boolean} destroyMenu
     * @private
     */

    /**
     * @cfg {Boolean} disabledCls
     * @private
     */

    focusable: false,

    /**
     * @cfg {String} href
     * @private
     */

    /**
     * @cfg {String} hrefTarget
     * @private
     */

    /**
     * @cfg {Boolean} hideOnClick
     * @private
     */
    hideOnClick: false,

    /**
     * @cfg {String} icon
     * @private
     */

    /**
     * @cfg {String} iconCls
     * @private
     */

    /**
     * @cfg {Object} menu
     * @private
     */

    /**
     * @cfg {String} menuAlign
     * @private
     */

    /**
     * @cfg {Number} menuExpandDelay
     * @private
     */

    /**
     * @cfg {Number} menuHideDelay
     * @private
     */

    /**
     * @cfg {Boolean} plain
     * @private
     */
    plain: true,

    /**
     * @cfg {String} separatorCls
     * The CSS class used by the separator item to show the incised line.
     */
    separatorCls: Ext.baseCSSPrefix + 'menu-item-separator',

    /**
     * @cfg {String} text
     * @private
     */
    text: '&#160;',
    
    ariaRole: 'separator',

    beforeRender: function(ct, pos) {
        var me = this;

        me.callParent();

        me.addCls(me.separatorCls);
    }
});