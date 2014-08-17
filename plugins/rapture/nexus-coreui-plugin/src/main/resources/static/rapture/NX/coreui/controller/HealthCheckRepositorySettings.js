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
/*global Ext, NX*/

/**
 * HealthCheck repository settings controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.HealthCheckRepositorySettings', {
  extend: 'Ext.app.Controller',

  stores: [
    'HealthCheckRepositoryStatus'
  ],
  views: [
    'healthcheck.HealthCheckRepositorySettings',
    'healthcheck.HealthCheckEula'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-repository-feature' },
    { ref: 'panel', selector: 'nx-coreui-healthcheck-repository-settings' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.listen({
      component: {
        'nx-coreui-repository-list': {
          selection: me.onSelection
        },
        'nx-coreui-healthcheck-repository-settings': {
          activate: me.onActivate,
          deactivate: me.onDeactivate
        },
        'nx-coreui-healthcheck-repository-settings form': {
          submitted: me.onSettingsSubmitted
        }
      }
    });
  },

  /**
   * @private
   * Set "active" flag on panel.
   * @param {NX.coreui.view.healthcheck.HealthCheckRepositorySettings} panel activated
   */
  onActivate: function(panel) {
    var me = this;

    panel.active = true;
    me.loadSettings();
  },

  /**
   * @private
   * Unset "active" flag on panel.
   * @param {NX.coreui.view.healthcheck.HealthCheckRepositorySettings} panel deactivated
   */
  onDeactivate: function(panel) {
    panel.active = false;
  },

  /**
   * @private
   * Add "Health Check" panel to repository tabs if we have a proxy, if not already present and/or load health check
   * settings into the panel.
   * @param {NX.coreui.view.repository.RepositoryList} grid repository grid
   * @param {NX.coreui.model.Repository} model selected repository
   */
  onSelection: function(grid, model) {
    var me = this,
        panel = me.getPanel();

    if (model && (model.get('type') === 'proxy' &&
        ((model.get('format') !== 'maven2') || model.get('repositoryPolicy') === 'RELEASE'))) {

      if (!panel) {
        me.getFeature().addTab({ xtype: 'nx-coreui-healthcheck-repository-settings', title: 'Health Check' });
        panel = me.getPanel();
      }
      panel.setRepository(model);
      me.loadSettings();
    }
    else {
      if (panel) {
        me.getFeature().removeTab(panel);
      }
    }
  },

  /**
   * @private
   * Load health check settings for current repository.
   */
  loadSettings: function() {
    var me = this,
        panel = me.getPanel(),
        form = panel.down('nx-settingsform');

    if (panel.active) {
      form.fireEvent('load', form);
    }
  },

  onSettingsSubmitted: function() {
    this.getHealthCheckRepositoryStatusStore().load();
  }

});