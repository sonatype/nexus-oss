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
 * A layout that arranges items vertically down a Container. This layout optionally divides available vertical space
 * between child items containing a numeric `flex` configuration.
 *
 * This layout may also be used to set the widths of child items by configuring it with the {@link #align} option.
 *
 *     @example
 *     Ext.create('Ext.Panel', {
 *         width: 500,
 *         height: 400,
 *         title: "VBoxLayout Panel",
 *         layout: {
 *             type: 'vbox',
 *             align: 'center'
 *         },
 *         renderTo: document.body,
 *         items: [{
 *             xtype: 'panel',
 *             title: 'Inner Panel One',
 *             width: 250,
 *             flex: 2
 *         },
 *         {
 *             xtype: 'panel',
 *             title: 'Inner Panel Two',
 *             width: 250,
 *             flex: 4
 *         },
 *         {
 *             xtype: 'panel',
 *             title: 'Inner Panel Three',
 *             width: '50%',
 *             flex: 4
 *         }]
 *     });
 */
Ext.define('Ext.layout.container.VBox', {

    /* Begin Definitions */

    alias: ['layout.vbox'],
    extend: 'Ext.layout.container.Box',
    alternateClassName: 'Ext.layout.VBoxLayout',

    /* End Definitions */

    /**
     * @cfg {String} align
     * Controls how the child items of the container are aligned. Acceptable configuration values for this property are:
     *
     * - **left** : **Default** child items are aligned horizontally at the **left** side of the container.
     * - **center** : child items are aligned horizontally at the **mid-width** of the container.
     * - **right** : child items are aligned horizontally at the **right** of the container.
     * - **stretch** : child items are stretched horizontally to fill the width of the container.
     * - **stretchmax** : child items are stretched horizontally to the size of the largest item.
     */
    align : 'left', // left, center, stretch, strechmax

    /**
     * @cfg {"round"/"floor"/"ceil"} [alignRoundingMethod='round'] The Math method to use
     * for rounding fractional pixels when `{@link #align}:center` is used.
     */
    
    /**
     * @cfg {Boolean} constrainAlign
     * Limits the size of {@link #align aligned} components to the size of the container under certain circumstances.
     * Firstly, the container width must not be determined by the width of the child components. Secondly, the child
     * components must have their width {@link Ext.AbstractComponent#shrinkWrap shrinkwrapped}.
     */
    constrainAlign: false,

    type: 'vbox',

    direction: 'vertical',

    horizontal: false,

    names: {
        // parallel
        beforeX: 'top',
        beforeScrollX: 'top',
        beforeScrollerSuffix: '-before-scroller',
        afterScrollerSuffix: '-after-scroller',
        leftCap: 'Top',
        afterX: 'bottom',
        width: 'height',
        contentWidth: 'contentHeight',
        minWidth: 'minHeight',
        maxWidth: 'maxHeight',
        widthCap: 'Height',
        widthModel: 'heightModel',
        widthIndex: 1,
        x: 'y',
        scrollLeft: 'scrollTop',
        overflowX: 'overflowY',
        hasOverflowX: 'hasOverflowY',
        invalidateScrollX: 'invalidateScrollY',
        parallelMargins: 'tb',

        // perpendicular
        center: 'center',
        beforeY: 'left',
        afterY: 'right',
        height: 'width',
        contentHeight: 'contentWidth',
        minHeight: 'minWidth',
        maxHeight: 'maxWidth',
        heightCap: 'Width',
        heightModel: 'widthModel',
        heightIndex: 0,
        y: 'x',
        overflowY: 'overflowX',
        hasOverflowY: 'hasOverflowX',
        invalidateScrollY: 'invalidateScrollX',
        perpendicularMargins: 'lr',

        // Methods
        getWidth: 'getHeight',
        getHeight: 'getWidth',
        setWidth: 'setHeight',
        setHeight: 'setWidth',
        gotWidth: 'gotHeight',
        gotHeight: 'gotWidth',
        setContentWidth: 'setContentHeight',
        setContentHeight: 'setContentWidth',
        setWidthInDom: 'setHeightInDom',
        setHeightInDom: 'setWidthInDom',
        getScrollLeft: 'getScrollTop',
        setScrollLeft: 'setScrollTop',
        scrollTo: 'scrollTo'
    },

    sizePolicy: {
        flex: {
            '': {
                readsWidth : 1,
                readsHeight: 0,
                setsWidth  : 0,
                setsHeight : 1
            },
            stretch: {
                readsWidth : 0,
                readsHeight: 0,
                setsWidth  : 1,
                setsHeight : 1
            },
            stretchmax: {
                readsWidth : 1,
                readsHeight: 0,
                setsWidth  : 1,
                setsHeight : 1
            }
        },
        '': {
            readsWidth : 1,
            readsHeight: 1,
            setsWidth  : 0,
            setsHeight : 0
        },
        stretch: {
            readsWidth : 0,
            readsHeight: 1,
            setsWidth  : 1,
            setsHeight : 0
        },
        stretchmax: {
            readsWidth : 1,
            readsHeight: 1,
            setsWidth  : 1,
            setsHeight : 0
        }
    }
});
