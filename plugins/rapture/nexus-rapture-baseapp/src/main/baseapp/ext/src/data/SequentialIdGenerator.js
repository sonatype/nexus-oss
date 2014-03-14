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
 * @author Don Griffin
 *
 * This class is a sequential id generator. A simple use of this class would be like so:
 *
 *     Ext.define('MyApp.data.MyModel', {
 *         extend: 'Ext.data.Model',
 *         idgen: 'sequential'
 *     });
 *     // assign id's of 1, 2, 3, etc.
 *
 * An example of a configured generator would be:
 *
 *     Ext.define('MyApp.data.MyModel', {
 *         extend: 'Ext.data.Model',
 *         idgen: {
 *             type: 'sequential',
 *             prefix: 'ID_',
 *             seed: 1000
 *         }
 *     });
 *     // assign id's of ID_1000, ID_1001, ID_1002, etc.
 *
 */
Ext.define('Ext.data.SequentialIdGenerator', {
    extend: 'Ext.data.IdGenerator',
    alias: 'idgen.sequential',

    constructor: function() {
        var me = this;

        me.callParent(arguments);

        me.parts = [ me.prefix, ''];
    },

    /**
     * @cfg {String} prefix
     * The string to place in front of the sequential number for each generated id. The
     * default is blank.
     */
    prefix: '',

    /**
     * @cfg {Number} seed
     * The number at which to start generating sequential id's. The default is 1.
     */
    seed: 1,

    /**
     * Generates and returns the next id.
     * @return {String} The next id.
     */
    generate: function () {
        var me = this,
            parts = me.parts;

        parts[1] = me.seed++;
        return parts.join('');
    }
});
