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
