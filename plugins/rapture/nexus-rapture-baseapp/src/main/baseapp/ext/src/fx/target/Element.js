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
