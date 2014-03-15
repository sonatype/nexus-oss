/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Watches over Ext.Direct communication.
 *
 * @since 3.0
 */
Ext.define('NX.controller.ExtDirect', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Security'
  ],

  init: function () {
    var me = this;

    me.listen({
      direct: {
        '*': {
          beforecallback: me.checkResponse
        }
      }
    });
  },

  /**
   * @private
   * Checks Ext.Direct response and automatically show warning messages if an error occurred.
   * If response specifies that authentication is required, will show the login window.
   */
  checkResponse: function (provider, transaction) {
    var me = this,
        result = transaction.result;

    if (Ext.isDefined(result)
        && Ext.isDefined(result.success) && result.success === false) {

      if (Ext.isDefined(result.authenticationRequired) && result.authenticationRequired === true) {
        NX.Messages.add({text: result.message, type: 'warning'});
        NX.Security.askToAuthenticate();
      }
      else if (Ext.isDefined(result.message)) {
        NX.Messages.add({ text: result.message, type: 'warning' });
      }
      else if (Ext.isDefined(result.messages)) {
        var message = Ext.Array.from(result.messages).join('<br/>');
        if (message) {
          NX.Messages.add({ text: message, type: 'warning' });
        }
      }
    }

    if (Ext.isDefined(transaction.serverException)) {
      NX.Messages.add({ text: transaction.serverException.exception.message, type: 'warning' });
    }
  }

});