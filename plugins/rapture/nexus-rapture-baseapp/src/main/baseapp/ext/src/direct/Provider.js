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
 * Ext.direct.Provider is an abstract class meant to be extended.
 *
 * For example Ext JS implements the following subclasses:
 *
 *     Provider
 *     |
 *     +---{@link Ext.direct.JsonProvider JsonProvider}
 *         |
 *         +---{@link Ext.direct.PollingProvider PollingProvider}
 *         |
 *         +---{@link Ext.direct.RemotingProvider RemotingProvider}
 *
 * @abstract
 */
Ext.define('Ext.direct.Provider', {
   alias: 'direct.provider',

    mixins: {
        observable: 'Ext.util.Observable'
    },
    
    isProvider: true,

   /**
     * @cfg {String} id
     * The unique id of the provider (defaults to an {@link Ext#id auto-assigned id}).
     * You should assign an id if you need to be able to access the provider later and you do
     * not have an object reference available, for example:
     *
     *      Ext.direct.Manager.addProvider({
     *          type: 'polling',
     *          url:  'php/poll.php',
     *          id:   'poll-provider'
     *      });
     *      var p = {@link Ext.direct.Manager}.{@link Ext.direct.Manager#getProvider getProvider}('poll-provider');
     *     p.disconnect();
     *
     */
    
    /**
     * @cfg {String[]} relayedEvents
     * List of Provider events that should be relayed by {@link Ext.direct.Manager}.
     * 'data' event is always relayed.
     */
    
    constructor: function(config) {
        var me = this;
        
        Ext.apply(me, config);
        
        Ext.applyIf(me, {
            id: Ext.id(null, 'provider-')
        });

        me.addEvents(
            /**
             * @event connect
             * Fires when the Provider connects to the server-side
             *
             * @param {Ext.direct.Provider} provider The {@link Ext.direct.Provider Provider}.
             */
            'connect',
            
            /**
             * @event disconnect
             * Fires when the Provider disconnects from the server-side
             *
             * @param {Ext.direct.Provider} provider The {@link Ext.direct.Provider Provider}.
             */
            'disconnect',
            
            /**
             * @event data
             * Fires when the Provider receives data from the server-side
             *
             * @param {Ext.direct.Provider} provider The {@link Ext.direct.Provider Provider}.
             * @param {Ext.direct.Event} e The Ext.direct.Event type that occurred.
             */
            'data',
            
            /**
             * @event exception
             * Fires when the Provider receives an exception from the server-side
             */
            'exception'
        );

        me.mixins.observable.constructor.call(me, config);
    },

    /**
     * Returns whether or not the server-side is currently connected.
     * Abstract method for subclasses to implement.
     * @template
     */
    isConnected: function() {
        return false;
    },

    /**
     * Abstract method for subclasses to implement.
     * @template
     */
    connect: Ext.emptyFn,

    /**
     * Abstract method for subclasses to implement.
     * @template
     */
    disconnect: Ext.emptyFn
});
