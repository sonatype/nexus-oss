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
