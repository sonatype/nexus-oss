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
 * Provides Ext.direct support for submitting form data.
 *
 * This example illustrates usage of Ext.direct.Direct to **submit** a form through Ext.Direct.
 *
 *     var myFormPanel = new Ext.form.Panel({
 *         // configs for FormPanel
 *         title: 'Basic Information',
 *         renderTo: document.body,
 *         width: 300, height: 160,
 *         padding: 10,
 *         buttons:[{
 *             text: 'Submit',
 *             handler: function(){
 *                 myFormPanel.getForm().submit({
 *                     params: {
 *                         foo: 'bar',
 *                         uid: 34
 *                     }
 *                 });
 *             }
 *         }],
 *
 *         // configs apply to child items
 *         defaults: {anchor: '100%'},
 *         defaultType: 'textfield',
 *         items: [{
 *             fieldLabel: 'Name',
 *             name: 'name'
 *         },{
 *             fieldLabel: 'Email',
 *             name: 'email'
 *         },{
 *             fieldLabel: 'Company',
 *             name: 'company'
 *         }],
 *
 *         // configs for BasicForm
 *         api: {
 *             // The server-side method to call for load() requests
 *             load: Profile.getBasicInfo,
 *             // The server-side must mark the submit handler as a 'formHandler'
 *             submit: Profile.updateBasicInfo
 *         },
 *         // specify the order for the passed params
 *         paramOrder: ['uid', 'foo']
 *     });
 *
 * The data packet sent to the server will resemble something like:
 *
 *     {
 *         "action":"Profile","method":"updateBasicInfo","type":"rpc","tid":"6",
 *         "result":{
 *             "success":true,
 *             "id":{
 *                 "extAction":"Profile","extMethod":"updateBasicInfo",
 *                 "extType":"rpc","extTID":"6","extUpload":"false",
 *                 "name":"Aaron Conran","email":"aaron@sencha.com","company":"Sencha Inc."
 *             }
 *         }
 *     }
 *
 * The form will process a data packet returned by the server that is similar to the following:
 *
 *     // sample success packet (batched requests)
 *     [
 *         {
 *             "action":"Profile","method":"updateBasicInfo","type":"rpc","tid":3,
 *             "result":{
 *                 "success":true
 *             }
 *         }
 *     ]
 *
 *     // sample failure packet (one request)
 *     {
 *             "action":"Profile","method":"updateBasicInfo","type":"rpc","tid":"6",
 *             "result":{
 *                 "errors":{
 *                     "email":"already taken"
 *                 },
 *                 "success":false,
 *                 "foo":"bar"
 *             }
 *     }
 *
 * Also see the discussion in {@link Ext.form.action.DirectLoad}.
 */
Ext.define('Ext.form.action.DirectSubmit', {
    extend:'Ext.form.action.Submit',
    requires: ['Ext.direct.Manager'],
    alternateClassName: 'Ext.form.Action.DirectSubmit',
    alias: 'formaction.directsubmit',

    type: 'directsubmit',

    doSubmit: function() {
        var me = this,
            form = me.form,
            api = form.api,
            fn = api.submit,
            callback = Ext.Function.bind(me.onComplete, me),
            formInfo = me.buildForm(),
            options;
        
        if (typeof fn !== 'function') {
            //<debug>
            var fnName = fn;
            //</debug>
            
            api.submit = fn = Ext.direct.Manager.parseMethod(fn);
            me.cleanup(formInfo);

            //<debug>
            if (!Ext.isFunction(fn)) {
                Ext.Error.raise('Cannot resolve Ext.Direct API method ' + fnName);
            }
            //</debug>
        }
        
        if (me.timeout || form.timeout) {
            options = {
                timeout: me.timeout * 1000 || form.timeout * 1000
            };
        }
        
        fn.call(window, formInfo.formEl, callback, me, options);
        me.cleanup(formInfo);
    },

    // Direct actions have already been processed and therefore
    // we can directly set the result; Direct Actions do not have
    // a this.response property.
    processResponse: function(result) {
        return (this.result = result);
    },
    
    onComplete: function(data, response){
        if (data) {
            this.onSuccess(data);
        } else {
            this.onFailure(null);
        }
    }
});
