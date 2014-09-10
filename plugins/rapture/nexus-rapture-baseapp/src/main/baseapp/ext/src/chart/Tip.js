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
 * @class Ext.chart.Tip
 * Provides tips for Ext.chart.series.Series.
 */
Ext.define('Ext.chart.Tip', {

    /* Begin Definitions */

    requires: ['Ext.tip.ToolTip', 'Ext.chart.TipSurface'],

    /* End Definitions */

    constructor: function(config) {
        var me = this,
            surface,
            sprites,
            tipSurface;
        if (config.tips) {
            me.tipTimeout = null;
            me.tipConfig = Ext.apply({}, config.tips, {
                renderer: Ext.emptyFn,
                constrainPosition: true,
                autoHide: true,
                shrinkWrapDock: true
            });
            me.tooltip = new Ext.tip.ToolTip(me.tipConfig);
            me.chart.surface.on('mousemove', me.tooltip.onMouseMove, me.tooltip);
            me.chart.surface.on('mouseleave', function() {
                me.hideTip();
            });
            if (me.tipConfig.surface) {
                //initialize a surface
                surface = me.tipConfig.surface;
                sprites = surface.sprites;
                tipSurface = new Ext.chart.TipSurface({
                    id: 'tipSurfaceComponent',
                    sprites: sprites
                });
                if (surface.width && surface.height) {
                    tipSurface.setSize(surface.width, surface.height);
                }
                me.tooltip.add(tipSurface);
                me.spriteTip = tipSurface;
            }
        }
    },

    showTip: function(item) {
        var me = this,
            tooltip,
            spriteTip,
            tipConfig,
            trackMouse,
            sprite,
            surface,
            surfaceExt,
            pos,
            x,
            y;
        if (!me.tooltip) {
            return;
        }
        clearTimeout(me.tipTimeout);
        tooltip = me.tooltip;
        spriteTip = me.spriteTip;
        tipConfig = me.tipConfig;
        trackMouse = tooltip.trackMouse;
        if (!trackMouse) {
            tooltip.trackMouse = true;
            sprite = item.sprite;
            surface = sprite.surface;
            surfaceExt = Ext.get(surface.getId());
            if (surfaceExt) {
                pos = surfaceExt.getXY();
                x = pos[0] + (sprite.attr.x || 0) + (sprite.attr.translation && sprite.attr.translation.x || 0);
                y = pos[1] + (sprite.attr.y || 0) + (sprite.attr.translation && sprite.attr.translation.y || 0);
                tooltip.targetXY = [x, y];
            }
        }
        if (spriteTip) {
            tipConfig.renderer.call(tooltip, item.storeItem, item, spriteTip.surface);
        } else {
            tipConfig.renderer.call(tooltip, item.storeItem, item);
        }
        tooltip.delayShow(trackMouse);
        tooltip.trackMouse = trackMouse;
    },

    hideTip: function(item) {
        var tooltip = this.tooltip;
        if (!tooltip) {
            return;
        }
        clearTimeout(this.tipTimeout);
        this.tipTimeout = setTimeout(function() {
            tooltip.delayHide();
        }, 0);
    }
});
