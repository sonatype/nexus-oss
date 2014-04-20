/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Analytics Event store.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.store.AnalyticsEvent', {
  extend: 'Ext.data.Store',
  model: 'NX.coreui.model.AnalyticsEvent',

  pageSize: 250,

  proxy: {
    type: 'direct',

    api: {
      read: 'NX.direct.analytics_Events.read'
    },

    reader: {
      type: 'json',
      root: 'data',
      successProperty: 'success'
    }
  },

  sortOnLoad: true,
  sorters: { property: 'timestamp', direction: 'ASC' }
});
