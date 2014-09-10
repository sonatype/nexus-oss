/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2014 Sencha Inc

Contact:  http://www.sencha.com/contact

Pre-release code in the Ext repository is intended for development purposes only and will
not always be stable. 

Use of pre-release code is permitted with your application at your own risk under standard
Ext license terms. Public redistribution is prohibited.

For early licensing, please contact us at licensing@sencha.com

Build date: 2014-09-02 11:12:40 (ef1fa70924f51a26dacbe29644ca3f31501a5fce)
*/
/**
 * A simple element that adds extra horizontal space between items in a toolbar.
 * By default a 2px wide space is added via CSS specification:
 *
 *     .x-toolbar .x-toolbar-spacer {
 *         width: 2px;
 *     }
 *
 * Example:
 *
 *     @example
 *     Ext.create('Ext.panel.Panel', {
 *         title: 'Toolbar Spacer Example',
 *         width: 300,
 *         height: 200,
 *         tbar : [
 *             'Item 1',
 *             { xtype: 'tbspacer' }, // or ' '
 *             'Item 2',
 *             // space width is also configurable via javascript
 *             { xtype: 'tbspacer', width: 50 }, // add a 50px space
 *             'Item 3'
 *         ],
 *         renderTo: Ext.getBody()
 *     });
 */
Ext.define('Ext.toolbar.Spacer', {
    extend: 'Ext.Component',
    alias: 'widget.tbspacer',
    alternateClassName: 'Ext.Toolbar.Spacer',
    baseCls: Ext.baseCSSPrefix + 'toolbar-spacer',
    focusable: false,
    
    ariaRole: 'presentation'
});