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
// @tag extras,core
/**
 * A static {@link Ext.util.TaskRunner} instance that can be used to start and stop
 * arbitrary tasks. See {@link Ext.util.TaskRunner} for supported methods and task
 * config properties.
 *
 *    // Start a simple clock task that updates a div once per second
 *    var task = {
 *       run: function(){
 *           Ext.fly('clock').update(new Date().format('g:i:s A'));
 *       },
 *       interval: 1000 //1 second
 *    }
 *
 *    Ext.TaskManager.start(task);
 *
 * See the {@link #start} method for details about how to configure a task object.
 */
Ext.define('Ext.util.TaskManager', {
    extend: 'Ext.util.TaskRunner',

    alternateClassName: [
        'Ext.TaskManager'
    ],

    singleton: true
});
