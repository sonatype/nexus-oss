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
 * This class provides a DOM ClassList API to buffer access to an element's class.
 * Instances of this class are created by {@link Ext.layout.ContextItem#getClassList}.
 */
Ext.define('Ext.layout.ClassList', (function () {

    var splitWords = Ext.String.splitWords,
        toMap = Ext.Array.toMap;

    return {
        dirty: false,

        constructor: function (owner) {
            this.owner = owner;
            this.map = toMap(this.classes = splitWords(owner.el.className));
        },

        /**
         * Adds a single class to the class list.
         */
        add: function (cls) {
            var me = this;

            if (!me.map[cls]) {
                me.map[cls] = true;
                me.classes.push(cls);
                if (!me.dirty) {
                    me.dirty = true;
                    me.owner.markDirty();
                }
            }
        },

        /**
         * Adds one or more classes in an array or space-delimited string to the class list.
         */
        addMany: function (classes) {
            Ext.each(splitWords(classes), this.add, this);
        },

        contains: function (cls) {
            return this.map[cls];
        },

        flush: function () {
            this.owner.el.className = this.classes.join(' ');
            this.dirty = false;
        },

        /**
         * Removes a single class from the class list.
         */
        remove: function (cls) {
            var me = this;

            if (me.map[cls]) {
                delete me.map[cls];
                me.classes = Ext.Array.filter(me.classes, function (c) {
                    return c != cls;
                });
                if (!me.dirty) {
                    me.dirty = true;
                    me.owner.markDirty();
                }
            }
        },

        /**
         * Removes one or more classes in an array or space-delimited string from the class
         * list.
         */
        removeMany: function (classes) {
            var me = this,
                remove = toMap(splitWords(classes));

            me.classes = Ext.Array.filter(me.classes, function (c) {
                if (!remove[c]) {
                    return true;
                }

                delete me.map[c];
                if (!me.dirty) {
                    me.dirty = true;
                    me.owner.markDirty();
                }
                return false;
            });
        }
    };
}()));
