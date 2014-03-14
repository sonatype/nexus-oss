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
Ext.define('Ext.layout.component.field.Slider', {

    /* Begin Definitions */

    alias: ['layout.sliderfield'],

    extend: 'Ext.layout.component.field.Field',

    /* End Definitions */

    type: 'sliderfield',

    beginLayout: function(ownerContext) {
        this.callParent(arguments);

        ownerContext.endElContext   = ownerContext.getEl('endEl');
        ownerContext.innerElContext = ownerContext.getEl('innerEl');
        ownerContext.bodyElContext  = ownerContext.getEl('bodyEl');
    },

    publishInnerHeight: function (ownerContext, height) {
        var innerHeight = height - this.measureLabelErrorHeight(ownerContext),
            endElPad,
            inputPad;
        if (this.owner.vertical) {
            endElPad = ownerContext.endElContext.getPaddingInfo();
            inputPad = ownerContext.inputContext.getPaddingInfo();
            ownerContext.innerElContext.setHeight(innerHeight - inputPad.height - endElPad.height);
        } else {
            ownerContext.bodyElContext.setHeight(innerHeight);
        }
    },

    publishInnerWidth: function (ownerContext, width) {
        if (!this.owner.vertical) {
            var endElPad = ownerContext.endElContext.getPaddingInfo(),
                inputPad = ownerContext.inputContext.getPaddingInfo();

            ownerContext.innerElContext.setWidth(width - inputPad.left - endElPad.right - ownerContext.labelContext.getProp('width'));
        }
    },

    beginLayoutFixed: function(ownerContext, width, suffix) {
        var me = this,
            ieInputWidthAdjustment = me.ieInputWidthAdjustment;

        if (ieInputWidthAdjustment) {
            // adjust for IE 6/7 strict content-box model
            // RTL: This might have to be padding-left unless the senses of the padding styles switch when in RTL mode.
            me.owner.bodyEl.setStyle('padding-right', ieInputWidthAdjustment + 'px');
        }

        me.callParent(arguments);
    }
});
