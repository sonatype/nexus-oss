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
/*global NX, Ext, Nexus, Sonatype*/

/**
 * Events ZIP created window.
 *
 * @since 2.8
 */
NX.define('Nexus.analytics.view.EventsZipCreated', {
  extend: 'Nexus.wonderland.view.FileCreated',
  xtype: 'nx-analytics-view-eventszip-created',

  requires: [
    'Nexus.analytics.Icons'
  ],

  fileType: 'Events ZIP',
  downloadButtonId: 'nx-analytics-button-eventszip-download',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.fileIcon = Nexus.analytics.Icons.get('zip').variant('x32');

    Nexus.analytics.view.EventsZipCreated.superclass.initComponent.apply(me, arguments);
  }

});