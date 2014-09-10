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
 * @author Ed Spencer
 *
 * Simple wrapper class that represents a set of records returned by a Proxy.
 */
Ext.define('Ext.data.ResultSet', {
    /**
     * @cfg {Boolean} loaded
     * True if the records have already been loaded. This is only meaningful when dealing with
     * SQL-backed proxies.
     */
    loaded: true,

    /**
     * @cfg {Number} count
     * The number of records in this ResultSet. Note that total may differ from this number.
     */
    count: 0,

    /**
     * @cfg {Number} total
     * The total number of records reported by the data source. This ResultSet may form a subset of
     * those records (see {@link #count}).
     */
    total: 0,

    /**
     * @cfg {Boolean} success
     * True if the ResultSet loaded successfully, false if any errors were encountered.
     */
    success: false,

    /**
     * @cfg {Ext.data.Model[]} records (required)
     * The array of record instances.
     */

    /**
     * Creates the resultSet
     * @param {Object} [config] Config object.
     */
    constructor: function(config) {
        Ext.apply(this, config);

        /**
         * @property {Number} totalRecords
         * Copy of this.total.
         * @deprecated Will be removed in Ext JS 5.0. Use {@link #total} instead.
         */
        this.totalRecords = this.total;

        if (config.count === undefined) {
            this.count = this.records.length;
        }
    }
});