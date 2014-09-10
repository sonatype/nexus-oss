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
 * Layout class for {@link Ext.form.field.Text} fields. Handles sizing the input field.
 * @private
 */
Ext.define('Ext.layout.component.field.Text', {
    extend: 'Ext.layout.component.field.Field',
    alias: 'layout.textfield',
    requires: ['Ext.util.TextMetrics'],

    type: 'textfield',
    
    // Private. Forces the width to be set into the DOM when a configured width (See Component layout's beginLayoutCycle).
    // This is needed because the 100% width in the input element needs a width to be 100% of.
    setWidthInDom: true,
    
    canGrowWidth: true,

    beginLayoutCycle: function(ownerContext) {
        this.callParent(arguments);
        
        // Clear height, in case a previous layout cycle stretched it.
        if (ownerContext.heightModel.shrinkWrap) {
            ownerContext.inputContext.el.setStyle('height', '');
        }
    },

    measureContentWidth: function (ownerContext) {
        var me = this,
            owner = me.owner,
            width = me.callParent(arguments),
            inputContext = ownerContext.inputContext,
            inputEl, value, calcWidth, max, min;

        if (owner.grow && me.canGrowWidth && !ownerContext.state.growHandled) {
            inputEl = owner.inputEl;

            // Find the width that contains the whole text value
            value = Ext.util.Format.htmlEncode(inputEl.dom.value || (owner.hasFocus ? '' : owner.emptyText) || '');
            value += owner.growAppend;
            calcWidth = inputEl.getTextWidth(value) + inputContext.getFrameInfo().width;

            max = owner.growMax;
            min = Math.min(max, width);
            max = Math.max(owner.growMin, max, min);

            // Constrain
            calcWidth = Ext.Number.constrain(calcWidth, owner.growMin, max);
            inputContext.setWidth(calcWidth);
            ownerContext.state.growHandled = true;
            
            // Now that we've set the inputContext, we need to recalculate the width
            inputContext.domBlock(me, 'width');
            width = NaN;
        }
        return width;
    },
    
    publishInnerHeight: function(ownerContext, height) {
        ownerContext.inputContext.setHeight(height - this.measureLabelErrorHeight(ownerContext));
    },

    beginLayoutFixed: function(ownerContext, width, suffix) {
        var me = this,
            ieInputWidthAdjustment = me.ieInputWidthAdjustment;

        if (ieInputWidthAdjustment) {
            me.adjustIEInputPadding(ownerContext);
            if(suffix === 'px') {
                width -= ieInputWidthAdjustment;
            }
        }

        me.callParent(arguments);
    },

    adjustIEInputPadding: function(ownerContext) {
        // adjust for IE 6/7 strict content-box model
        this.owner.bodyEl.setStyle('padding-right', this.ieInputWidthAdjustment + 'px');
    }
});
