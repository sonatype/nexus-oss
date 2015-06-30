/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * Message controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Message', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Icons',
    'NX.Messages'
  ],

  mixins: {
    logAware: 'NX.LogAware'
  },

  models: [
    'Message'
  ],
  stores: [
    'Message'
  ],
  views: [
    'header.Messages',
    'message.Panel',
    'message.Notification'
  ],

  refs: [
    {
      ref: 'button',
      selector: 'nx-header-messages'
    },
    {
      ref: 'panel',
      selector: 'nx-message-panel'
    }
  ],

  /**
   * @protected
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'message-default': {
        file: 'bell.png',
        variants: ['x16', 'x32'],
        preload: true
      },
      'message-primary': {
        file: 'information.png',
        variants: ['x16', 'x32'],
        preload: true
      },
      'message-danger': {
        file: 'exclamation.png',
        variants: ['x16', 'x32'],
        preload: true
      },
      'message-warning': {
        file: 'warning.png',
        variants: ['x16', 'x32'],
        preload: true
      },
      'message-success': {
        file: 'accept.png',
        variants: ['x16', 'x32'],
        preload: true
      }
    });

    me.listen({
      controller: {
        '#User': {
          signout: me.clearMessages
        }
      },
      component: {
        'nx-header-messages': {
          click: me.toggleMessages
        },
        'nx-message-panel button[action=clear]': {
          click: me.clearMessages
        },
        'nx-message-panel button[action=close]': {
          click: me.toggleMessages
        }
      }
    });

    me.getStore('Message').on('datachanged', me.updateHeader, me);
  },

  /**
   * Change the panel title when the # of records in the store changes.
   *
   * @private
   */
  updateHeader: function () {
    var me = this,
        button = me.getButton(),
        count = me.getStore('Message').getCount();

    if (button) {
      if (count) {
        button.setText(count);
      }
      else {
        button.setText('');
      }
    }
  },

  /**
   * @private
   * @param button
   */
  toggleMessages: function(button) {
    var me = this,
        panel = me.getPanel();

    if (panel.isVisible()) {
      panel.hide();
    }
    else {
      panel.show();
    }
  },

  /**
   * @private
   */
  clearMessages: function () {
    this.getMessageStore().removeAll();
  },

  /**
   * @public
   */
  addMessage: function (message) {
    var me = this,
        store = me.getStore('Message');

    if (!message.type) {
      message.type = 'default';
    }

    message.timestamp = new Date();

    // add new messages to the top of the store
    store.insert(0, message);

    // show transient message notification
    me.getView('message.Notification').create({
      ui: 'nx-message-' + message.type,
      iconCls: NX.Icons.cls('message-' + message.type, 'x16'),
      title: Ext.String.capitalize(message.type),
      html: message.text
    });
  }
});
