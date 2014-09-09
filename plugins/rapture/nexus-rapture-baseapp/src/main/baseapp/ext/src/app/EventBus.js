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
 * This class manages event dispatching for Controllers. The details of connecting classes
 * to this dispatching mechanism is delegated to {@link Ext.app.EventDomain} instances.
 *
 * @private
 */
Ext.define('Ext.app.EventBus', {
    singleton: true,

    requires: [
        'Ext.app.domain.Component'
    ],
    
    constructor: function() {
        var me = this,
            domains = Ext.app.EventDomain.instances;

        me.callParent();

        me.domains = domains;
        me.bus = domains.component.bus; // compat
    },

    /**
     * Adds a set of component event listeners for a controller. To work with event domains
     * other than component, see {@link #listen}.
     *
     * @param {Object} selectors Config object containing selectors and listeners.
     * @param {Ext.app.Controller} controller The listening controller instance.
     */
    control: function(selectors, controller) {
        return this.domains.component.listen(selectors, controller);
    },

    /**
     * Adds a set of event domain listeners for a controller. For more information on event
     * domains, see {@link Ext.app.EventDomain} and {@link Ext.app.Controller}.
     *
     * @param {Object} to Config object containing domains, selectors and listeners.
     * @param {Ext.app.Controller} controller The listening controller instance.
     */
    listen: function(to, controller) {
        var domains = this.domains,
            domain;

        for (domain in to) {
            if (to.hasOwnProperty(domain)) {
                domains[domain].listen(to[domain], controller);
            }
        }
    },

    /**
     * Removes all of a controller's attached listeners.
     *
     * @param {String} controllerId The id of the controller.
     */
    unlisten: function(controllerId) {
        var domains = Ext.app.EventDomain.instances,
            domain;
        
        for (domain in domains) {
            domains[domain].unlisten(controllerId);
        }
    }
});
