/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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
 * Health Check Search Result Version store.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.store.HealthCheckSearchResultVersion', {
  extend: 'Ext.data.Store',
  model: 'NX.coreui.model.HealthCheckSearchResultVersion',

  proxy: {
    type: 'memory',
    reader: {
      type: 'json'
    }
  },

  status: {
    capped: false,
    disabled: false,
    error: false
  },

  load: function(groupId, artifactId, versions) {
    var me = this,
        versionDetails = [];

    me.loading = true;
    me.status = {
      capped: false,
      disabled: false,
      error: false
    };
    NX.direct.healthcheck_Search.read(groupId, artifactId, versions, function(response) {
      me.loading = false;
      if (Ext.isObject(response) && response.success) {
        me.status = {
          capped: response.data['capped'],
          disabled: response.data['disabled'],
          error: response.data['error']
        };
        Ext.Object.each(response.data.versions, function(key, value) {
          versionDetails.push(Ext.apply({ version: key }, value));
        });
        me.loadData(versionDetails);
      }
      else {
        me.status['error'] = true;
      }
    });
  }

});
