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
/*global define,NX*/
define('nexus-logging-plugin-boot', [
  'Nexus/logging/app/store/Logger',
  'Nexus/logging/app/view/Panel',
  'Nexus/logging/app/controller/Logging'
], function () {
  NX.log.debug('Main nexus-logging-plugin modules loaded');
  var sp = Sonatype.lib.Permissions;
  // install panel into main NX navigation
  Sonatype.Events.on('nexusNavigationInit', function (panel) {
    panel.add({
      enabled: sp.checkPermission('nexus:logging', sp.READ),
      sectionId: 'st-nexus-config',
      title: 'Logging',
      tabId: 'logging',
      tabCode: function () {
        var controller = NX.create('Nexus.logging.app.controller.Logging');
        controller.init();
        var panel = NX.create('Nexus.logging.app.view.Panel', {
          id: 'logging'
        });
        return panel;
      }
    });
  });
});