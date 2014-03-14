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
 * @class Ext.fx.target.CompositeElementCSS
 * 
 * This class represents a animation target for a {@link Ext.CompositeElement}, where the
 * constituent elements support CSS based animation. It allows each {@link Ext.Element} in 
 * the group to be animated as a whole. In general this class will not be created directly, 
 * the {@link Ext.CompositeElement} will be passed to the animation and the appropriate target 
 * will be created.
 */
Ext.define('Ext.fx.target.CompositeElementCSS', {

    /* Begin Definitions */

    extend: 'Ext.fx.target.CompositeElement',

    requires: ['Ext.fx.target.ElementCSS'],

    /* End Definitions */
    setAttr: function() {
        return Ext.fx.target.ElementCSS.prototype.setAttr.apply(this, arguments);
    }
});