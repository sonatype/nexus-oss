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
 * Base class for any client-side storage. Used as a superclass for {@link Ext.data.proxy.Memory Memory} and
 * {@link Ext.data.proxy.WebStorage Web Storage} proxies. Do not use directly, use one of the subclasses instead.
 * @private
 */
Ext.define('Ext.data.proxy.Client', {
    extend: 'Ext.data.proxy.Proxy',
    alternateClassName: 'Ext.data.ClientProxy',
    
    /**
     * @property {Boolean} isSynchronous
     * `true` in this class to identify that requests made on this proxy are
     * performed synchronously
     */
    isSynchronous: true,

    /**
     * Abstract function that must be implemented by each ClientProxy subclass. This should purge all record data
     * from the client side storage, as well as removing any supporting data (such as lists of record IDs)
     */
    clear: function() {
        //<debug>
        Ext.Error.raise("The Ext.data.proxy.Client subclass that you are using has not defined a 'clear' function. See src/data/ClientProxy.js for details.");
        //</debug>
    }
});
