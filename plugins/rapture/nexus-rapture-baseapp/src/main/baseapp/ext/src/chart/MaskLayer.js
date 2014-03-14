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
 * @private
 */
Ext.define('Ext.chart.MaskLayer', {
    extend: 'Ext.Component',
    
    constructor: function(config) {
        config = Ext.apply(config || {}, {
            style: 'position:absolute;background-color:#ff9;cursor:crosshair;opacity:0.5;border:1px solid #00f;'
        });
        this.callParent([config]);    
    },
    
    initComponent: function() {
        var me = this;
        me.callParent(arguments);
        me.addEvents(
            'mousedown',
            'mouseup',
            'mousemove',
            'mouseenter',
            'mouseleave'
        );
    },

    initDraggable: function() {
        this.callParent(arguments);
        this.dd.onStart = function (e) {
            var me = this,
                comp = me.comp;
    
            // Cache the start [X, Y] array
            this.startPosition = comp.getPosition(true);
    
            // If client Component has a ghost method to show a lightweight version of itself
            // then use that as a drag proxy unless configured to liveDrag.
            if (comp.ghost && !comp.liveDrag) {
                 me.proxy = comp.ghost();
                 me.dragTarget = me.proxy.header.el;
            }
    
            // Set the constrainTo Region before we start dragging.
            if (me.constrain || me.constrainDelegate) {
                me.constrainTo = me.calculateConstrainRegion();
            }
        };
    }
});