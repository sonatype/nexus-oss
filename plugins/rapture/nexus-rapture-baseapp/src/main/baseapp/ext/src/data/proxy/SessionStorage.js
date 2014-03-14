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
 * @author Ed Spencer
 *
 * Proxy which uses HTML5 session storage as its data storage/retrieval mechanism. If this proxy is used in a browser
 * where session storage is not supported, the constructor will throw an error. A session storage proxy requires a
 * unique ID which is used as a key in which all record data are stored in the session storage object.
 *
 * It's important to supply this unique ID as it cannot be reliably determined otherwise. If no id is provided but the
 * attached store has a storeId, the storeId will be used. If neither option is presented the proxy will throw an error.
 *
 * Proxies are almost always used with a {@link Ext.data.Store store}:
 *
 *     new Ext.data.Store({
 *         proxy: {
 *             type: 'sessionstorage',
 *             id  : 'myProxyKey'
 *         }
 *     });
 *
 * Alternatively you can instantiate the Proxy directly:
 *
 *     new Ext.data.proxy.SessionStorage({
 *         id  : 'myOtherProxyKey'
 *     });
 *
 * Note that session storage is different to local storage (see {@link Ext.data.proxy.LocalStorage}) - if a browser
 * session is ended (e.g. by closing the browser) then all data in a SessionStorageProxy are lost. Browser restarts
 * don't affect the {@link Ext.data.proxy.LocalStorage} - the data are preserved.
 */
Ext.define('Ext.data.proxy.SessionStorage', {
    extend: 'Ext.data.proxy.WebStorage',
    alias: 'proxy.sessionstorage',
    alternateClassName: 'Ext.data.SessionStorageProxy',
    
    //inherit docs
    getStorageObject: function() {
        return window.sessionStorage;
    }
});
