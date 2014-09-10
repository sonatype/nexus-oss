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
 * A simple class that adds a vertical separator bar between toolbar items (css class: 'x-toolbar-separator').
 *
 *     @example
 *     Ext.create('Ext.panel.Panel', {
 *         title: 'Toolbar Separator Example',
 *         width: 300,
 *         height: 200,
 *         tbar : [
 *             'Item 1',
 *             { xtype: 'tbseparator' },
 *             'Item 2'
 *         ],
 *         renderTo: Ext.getBody()
 *     });
 */
Ext.define('Ext.toolbar.Separator', {
    extend: 'Ext.toolbar.Item',
    alias: 'widget.tbseparator',
    alternateClassName: 'Ext.Toolbar.Separator',
    baseCls: Ext.baseCSSPrefix + 'toolbar-separator',
    focusable: false,
    
    ariaRole: 'separator'
});