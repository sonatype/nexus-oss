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
/*global NX, Ext, Nexus*/

/**
 * Events store.
 *
 * @since 2.8
 */
NX.define('Nexus.analytics.store.Events', {
  extend: 'Ext.data.Store',

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  requires: [
    'Nexus.siesta'
  ],

  statics: {
    /**
     * @public
     */
    PAGE_SIZE: 250
  },

  /**
   * @constructor
   */
  constructor: function () {
    var me = this;

    Ext.apply(me, {
      storeId: 'nx-analytics-store-events',
      autoDestroy: true,
      restful: true,

      proxy: NX.create('Ext.data.HttpProxy', {
        url: Nexus.siesta.basePath + '/analytics/events'
      }),

      reader: NX.create('Ext.data.JsonReader', {
        root: 'events',
        totalProperty: 'total',
        fields: [
          'type',
          'timestamp',
          'userId',
          'sessionId',
          'attributes'
        ]
      })
    });

    me.constructor.superclass.constructor.call(me);
  }
});