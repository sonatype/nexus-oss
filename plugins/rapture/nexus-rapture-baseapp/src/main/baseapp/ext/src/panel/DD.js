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
 * DD implementation for Panels.
 * @private
 */
Ext.define('Ext.panel.DD', {
    extend: 'Ext.dd.DragSource',
    requires: ['Ext.panel.Proxy'],

    constructor : function(panel, cfg){
        var me = this;
        
        me.panel = panel;
        me.dragData = {panel: panel};
        me.panelProxy = new Ext.panel.Proxy(panel, cfg);
        me.proxy = me.panelProxy.proxy;

        me.callParent([panel.el, cfg]);
        me.setupEl(panel);
    },
    
    setupEl: function(panel){
        var me = this,
            header = panel.header,
            el = panel.body;
            
        if (header) {
            me.setHandleElId(header.id);
            el = header.el;
        }
        if (el) {
            el.setStyle('cursor', 'move');
            me.scroll = false;
        } else {
            // boxready fires after first layout, so we'll definitely be rendered
            panel.on('boxready', me.setupEl, me, {single: true});
        }
    },

    showFrame: Ext.emptyFn,
    startDrag: Ext.emptyFn,
    
    b4StartDrag: function(x, y) {
        this.panelProxy.show();
    },
    
    b4MouseDown: function(e) {
        var x = e.getPageX(),
            y = e.getPageY();
            
        this.autoOffset(x, y);
    },
    
    onInitDrag : function(x, y){
        this.onStartDrag(x, y);
        return true;
    },
    
    createFrame : Ext.emptyFn,
    
    getDragEl : function(e){
        var ghost = this.panelProxy.ghost;
        if (ghost) {
            return ghost.el.dom;
        }
    },
    
    endDrag : function(e){
        this.panelProxy.hide();
        this.panel.saveState();
    },

    autoOffset : function(x, y) {
        x -= this.startPageX;
        y -= this.startPageY;
        this.setDelta(x, y);
    },
    
    // Override this, we don't want to repair on an "invalid" drop, the panel
    // should main it's position
    onInvalidDrop: function(target, e, id) {
        var me = this;
        
        if (me.beforeInvalidDrop(target, e, id) !== false) {
            if (me.cachedTarget) {
                if(me.cachedTarget.isNotifyTarget){
                    me.cachedTarget.notifyOut(me, e, me.dragData);
                }
                me.cacheTarget = null;
            }

            if (me.afterInvalidDrop) {
                /**
                * An empty function by default, but provided so that you can perform a custom action
                * after an invalid drop has occurred by providing an implementation.
                * @param {Event} e The event object
                * @param {String} id The id of the dropped element
                * @method afterInvalidDrop
                */
                me.afterInvalidDrop(e, id);
            }
        }
    }
});