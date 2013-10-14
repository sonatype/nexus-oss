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
/*global NX, Ext, Sonatype, Nexus*/

/**
 * Controller.
 *
 * @since 2.7
 */
NX.define('Nexus.controller.Controller', {
  mixins: [
    'Nexus.LogAwareMixin'
  ],

  control: function(config) {
    var me = this;
    Ext.iterate(config, function(key) {
      if (key.startsWith('#')) {
        var id = key.substring(1);

        Ext.ComponentMgr.onAvailable(id, function(obj) {
          var events = config['#' + obj.id];

          Ext.iterate(events, function(event) {
            obj.on(event, events[event], me);
            me.logDebug('Registered for event "' + event + '" on ' + obj.id);
          });
        }, me);
      }
    });
  }
});