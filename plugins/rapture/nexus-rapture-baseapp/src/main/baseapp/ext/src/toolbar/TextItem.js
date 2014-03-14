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
 * A simple class that renders text directly into a toolbar.
 *
 *     @example
 *     Ext.create('Ext.panel.Panel', {
 *         title: 'Panel with TextItem',
 *         width: 300,
 *         height: 200,
 *         tbar: [
 *             { xtype: 'tbtext', text: 'Sample Text Item' }
 *         ],
 *         renderTo: Ext.getBody()
 *     });
 *
 * @constructor
 * Creates a new TextItem
 * @param {Object} text A text string, or a config object containing a #text property
 */
Ext.define('Ext.toolbar.TextItem', {
    extend: 'Ext.toolbar.Item',
    requires: ['Ext.XTemplate'],
    alias: 'widget.tbtext',
    alternateClassName: 'Ext.Toolbar.TextItem',

    /**
     * @cfg {String} text
     * The text to be used as innerHTML (html tags are accepted).
     */
    text: '',

    renderTpl: '{text}',
    //
    baseCls: Ext.baseCSSPrefix + 'toolbar-text',
    
    ariaRole: null,

    beforeRender : function() {
        var me = this;

        me.callParent();

        Ext.apply(me.renderData, {
            text: me.text
        });
    },

    /**
     * Updates this item's text, setting the text to be used as innerHTML.
     * @param {String} text The text to display (html accepted).
     */
    setText : function(text) {
        var me = this;
        me.text = text;
        if (me.rendered) {
            me.el.update(text);
            me.updateLayout();
        }
    }
});