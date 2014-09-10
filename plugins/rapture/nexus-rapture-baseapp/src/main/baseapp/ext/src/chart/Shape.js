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
 * @private
 */
Ext.define('Ext.chart.Shape', {

    /* Begin Definitions */

    singleton: true,

    /* End Definitions */

    circle: function (surface, opts) {
        return surface.add(Ext.apply({
            type: 'circle',
            x: opts.x,
            y: opts.y,
            stroke: null,
            radius: opts.radius
        }, opts));
    },
    line: function (surface, opts) {
        return surface.add(Ext.apply({
            type: 'rect',
            x: opts.x - opts.radius,
            y: opts.y - opts.radius,
            height: 2 * opts.radius,
            width: 2 * opts.radius / 5
        }, opts));
    },
    square: function (surface, opts) {
        return surface.add(Ext.applyIf({
            type: 'rect',
            x: opts.x - opts.radius,
            y: opts.y - opts.radius,
            height: 2 * opts.radius,
            width: 2 * opts.radius,
            radius: null
        }, opts));
    },
    triangle: function (surface, opts) {
        opts.radius *= 1.75;
        return surface.add(Ext.apply({
            type: 'path',
            stroke: null,
            path: "M".concat(opts.x, ",", opts.y, "m0-", opts.radius * 0.58, "l", opts.radius * 0.5, ",", opts.radius * 0.87, "-", opts.radius, ",0z")
        }, opts));
    },
    diamond: function (surface, opts) {
        var r = opts.radius;
        r *= 1.5;
        return surface.add(Ext.apply({
            type: 'path',
            stroke: null,
            path: ["M", opts.x, opts.y - r, "l", r, r, -r, r, -r, -r, r, -r, "z"]
        }, opts));
    },
    cross: function (surface, opts) {
        var r = opts.radius;
        r = r / 1.7;
        return surface.add(Ext.apply({
            type: 'path',
            stroke: null,
            path: "M".concat(opts.x - r, ",", opts.y, "l", [-r, -r, r, -r, r, r, r, -r, r, r, -r, r, r, r, -r, r, -r, -r, -r, r, -r, -r, "z"])
        }, opts));
    },
    plus: function (surface, opts) {
        var r = opts.radius / 1.3;
        return surface.add(Ext.apply({
            type: 'path',
            stroke: null,
            path: "M".concat(opts.x - r / 2, ",", opts.y - r / 2, "l", [0, -r, r, 0, 0, r, r, 0, 0, r, -r, 0, 0, r, -r, 0, 0, -r, -r, 0, 0, -r, "z"])
        }, opts));
    },
    arrow: function (surface, opts) {
        var r = opts.radius;
        return surface.add(Ext.apply({
            type: 'path',
            path: "M".concat(opts.x - r * 0.7, ",", opts.y - r * 0.4, "l", [r * 0.6, 0, 0, -r * 0.4, r, r * 0.8, -r, r * 0.8, 0, -r * 0.4, -r * 0.6, 0], "z")
        }, opts));
    },
    drop: function (surface, x, y, text, size, angle) {
        size = size || 30;
        angle = angle || 0;
        surface.add({
            type: 'path',
            path: ['M', x, y, 'l', size, 0, 'A', size * 0.4, size * 0.4, 0, 1, 0, x + size * 0.7, y - size * 0.7, 'z'],
            fill: '#000',
            stroke: 'none',
            rotate: {
                degrees: 22.5 - angle,
                x: x,
                y: y
            }
        });
        angle = (angle + 90) * Math.PI / 180;
        surface.add({
            type: 'text',
            x: x + size * Math.sin(angle) - 10, // Shift here, Not sure why.
            y: y + size * Math.cos(angle) + 5,
            text:  text,
            'font-size': size * 12 / 40,
            stroke: 'none',
            fill: '#fff'
        });
    }
});