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
 * @override Ext.rtl.dom.Layer
 * This override adds RTL positioning methods to Ext.dom.Layer.
 */
Ext.define('Ext.rtl.dom.Layer', {
    override: 'Ext.dom.Layer',

    rtlLocalXYNames: {
        get: 'rtlGetLocalXY',
        set: 'rtlSetLocalXY'
    },

    rtlSetLocalX: function() {
        this.callParent(arguments);
        return this.sync();
    },

    rtlSetLocalXY: function() {
        this.callParent(arguments);
        return this.sync();
    },

    rtlSetLocalY: function() {
        this.callParent(arguments);
        return this.sync();
    },

    rtlSetXY: function(xy, animate, duration, callback, easing) {
        var me = this;
        
        // Callback will restore shadow state and call the passed callback
        callback = me.createCB(callback);

        me.fixDisplay();
        me.beforeAction();
        me.callParent([xy, animate, duration, callback, easing]);
        if (!animate) {
            callback();
        }
        return me;
    },
    
    setRtl: function(rtl) {
        var me = this,
            shadow = me.shadow;
            
        me.localXYNames = rtl ? me.rtlLocalXYNames : Ext.dom.Layer.prototype.localXYNames;
        
        if (shadow) {
            shadow.localXYNames = me.localXYNames;
        }
    }

});
