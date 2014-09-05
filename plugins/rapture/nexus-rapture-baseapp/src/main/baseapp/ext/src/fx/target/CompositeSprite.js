/*
This file is part of Ext JS 4.2

Copyright (c) 2011-2014 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2014-09-02 11:12:40 (ef1fa70924f51a26dacbe29644ca3f31501a5fce)
*/
/**
 * @class Ext.fx.target.CompositeSprite

This class represents a animation target for a {@link Ext.draw.CompositeSprite}. It allows
each {@link Ext.draw.Sprite} in the group to be animated as a whole. In general this class will not be
created directly, the {@link Ext.draw.CompositeSprite} will be passed to the animation and
and the appropriate target will be created.

 * @markdown
 */

Ext.define('Ext.fx.target.CompositeSprite', {

    /* Begin Definitions */

    extend: 'Ext.fx.target.Sprite',

    /* End Definitions */

    getAttr: function(attr, val) {
        var out     = [],
            sprites = [].concat(this.target.items),
            length  = sprites.length,
            i,
            sprite;

        for (i = 0; i < length; i++) {
            sprite = sprites[i];
            out.push([sprite, val != undefined ? val : this.getFromPrim(sprite, attr)]);
        }

        return out;
    }
});
