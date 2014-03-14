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
 * Private utility class that manages the internal Shadow cache.
 * @private
 */
Ext.define('Ext.ShadowPool', {
    singleton: true,
    requires: ['Ext.DomHelper'],

    markup: (function() {
        return Ext.String.format(
            '<div class="{0}{1}-shadow" role="presentation"></div>',
            Ext.baseCSSPrefix,
            Ext.isIE && !Ext.supports.CSS3BoxShadow ? 'ie' : 'css'
        );
    }()),

    shadows: [],

    pull: function() {
        var sh = this.shadows.shift();
        if (!sh) {
            sh = Ext.get(Ext.DomHelper.insertHtml("beforeBegin", document.body.firstChild, this.markup));
            sh.autoBoxAdjust = false;
            //<debug>
            // tell the spec runner to ignore this element when checking if the dom is clean 
            sh.dom.setAttribute('data-sticky', true);
            //</debug>
        }
        return sh;
    },

    push: function(sh) {
        this.shadows.push(sh);
    },
    
    reset: function() {
        var shadows = [].concat(this.shadows),
            s,
            sLen    = shadows.length;

        for (s = 0; s < sLen; s++) {
            shadows[s].remove();
        }

        this.shadows = [];
    }
});