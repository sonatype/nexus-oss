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
 * @class Ext.fx.target.Element
 * 
 * This class represents a animation target for an {@link Ext.Element}. In general this class will not be
 * created directly, the {@link Ext.Element} will be passed to the animation and
 * and the appropriate target will be created.
 */
Ext.define('Ext.fx.target.Element', {

    /* Begin Definitions */
    
    extend: 'Ext.fx.target.Target',
    
    /* End Definitions */

    type: 'element',

    getElVal: function(el, attr, val) {
        if (val == undefined) {
            if (attr === 'x') {
                val = el.getX();
            } else if (attr === 'y') {
                val = el.getY();
            } else if (attr === 'scrollTop') {
                val = el.getScroll().top;
            } else if (attr === 'scrollLeft') {
                val = el.getScroll().left;
            } else if (attr === 'height') {
                val = el.getHeight();
            } else if (attr === 'width') {
                val = el.getWidth();
            } else {
                val = el.getStyle(attr);
            }
        }
        return val;
    },

    getAttr: function(attr, val) {
        var el = this.target;
        return [[ el, this.getElVal(el, attr, val)]];
    },

    setAttr: function(targetData) {
        var target = this.target,
            ln = targetData.length,
            attrs, attr, o, i, j, ln2;
            
        for (i = 0; i < ln; i++) {
            attrs = targetData[i].attrs;
            for (attr in attrs) {
                if (attrs.hasOwnProperty(attr)) {
                    ln2 = attrs[attr].length;
                    for (j = 0; j < ln2; j++) {
                        o = attrs[attr][j];
                        this.setElVal(o[0], attr, o[1]);
                    }
                }
            }
        }
    },
    
    setElVal: function(element, attr, value){
        if (attr === 'x') {
            element.setX(value);
        } else if (attr === 'y') {
            element.setY(value);
        } else if (attr === 'scrollTop') {
            element.scrollTo('top', value);
        } else if (attr === 'scrollLeft') {
            element.scrollTo('left',value);
        } else if (attr === 'width') {
            element.setWidth(value);
        } else if (attr === 'height') {
            element.setHeight(value);
        } else {
            element.setStyle(attr, value);
        }
    }
});
