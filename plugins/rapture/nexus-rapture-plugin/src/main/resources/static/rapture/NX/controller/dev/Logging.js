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
/*global Ext*/

/**
 * Logging dev-panel controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.dev.Logging', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.util.log.ConsoleSink',
    'NX.util.log.RemoteSink'
  ],

  stores: [
    'LogEvent',
    'LogLevel'
  ],

  refs: [
    {
      ref: 'panel',
      selector: 'nx-dev-logging'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.listen({
      component: {
        'nx-dev-logging button[action=clear]': {
          click: function(button) {
            me.getStore('LogEvent').removeAll();
          }
        },
        'nx-dev-logging checkbox[itemId=console]': {
          change: function(checkbox) {
            NX.util.log.ConsoleSink.setEnabled(checkbox.getValue());
          }
        },
        'nx-dev-logging checkbox[itemId=remote]': {
          change: function(checkbox) {
            NX.util.log.RemoteSink.setEnabled(checkbox.getValue());
          }
        },
        'nx-dev-logging combobox[itemId=threshold]': {
          select: function(combo) {
            me.getController('Logging').setThreshold(combo.getValue());
          },
          afterrender: function(combo) {
            combo.select(me.getController('Logging').getThreshold());
          }
        }
      }
    });

    // TODO: automatically scroll to bottom of event list, how can we do that?
  }
});