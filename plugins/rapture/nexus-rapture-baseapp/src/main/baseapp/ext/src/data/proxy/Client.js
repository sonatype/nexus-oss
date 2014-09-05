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
