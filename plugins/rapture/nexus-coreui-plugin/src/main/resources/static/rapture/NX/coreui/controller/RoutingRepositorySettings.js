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
 * Routing repository settings controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.RoutingRepositorySettings', {
  extend: 'Ext.app.Controller',

  views: [
    'routing.RoutingHostedRepositorySettings',
    'routing.RoutingProxyRepositorySettings'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-repository-feature' },
    { ref: 'hostedPanel', selector: 'nx-coreui-routing-hosted-repository-settings' },
    { ref: 'proxyPanel', selector: 'nx-coreui-routing-proxy-repository-settings' }
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
        'nx-coreui-routing-hosted-repository-settings': {
          activate: me.onActivate,
          deactivate: me.onDeactivate
        },
        'nx-coreui-routing-proxy-repository-settings': {
          activate: me.onActivate,
          deactivate: me.onDeactivate
        },
        'nx-coreui-routing-proxy-repository-settings #discoveryEnabled': {
          change: me.onDiscoveryEnabledChange
        },
        'nx-coreui-routing-proxy-repository-settings button[action=update]': {
          click: me.updateNow
        }
      }
    });
  },

  /**
   * @private
   * Set "active" flag on panel.
   */
  onActivate: function(panel) {
    var me = this;

    panel.active = true;
    me.loadSettings(panel);
  },

  /**
   * @private
   * Unset "active" flag on panel.
   */
  onDeactivate: function(panel) {
    panel.active = false;
  },

  /**
   * @private
   * Add "Routing" panel to repository tabs, if not already present and/or load routing settings into the panel.
   * @param {NX.coreui.view.repository.RepositoryList} grid repository grid
   * @param {NX.coreui.model.Repository} model selected repository
   */
  onSelection: function(grid, model) {
    var me = this,
        hostedPanel = me.getHostedPanel(),
        proxyPanel = me.getProxyPanel();

    if (model &&
        (model.get('format') === 'maven2' && (model.get('type') === 'hosted') || model.get('type') === 'group')) {
      if (!hostedPanel) {
        me.getFeature().addTab({ xtype: 'nx-coreui-routing-hosted-repository-settings', title: 'Routing' });
        hostedPanel = me.getHostedPanel();
      }
      hostedPanel.setRepository(model);
      me.loadSettings(hostedPanel);
    }
    else {
      if (hostedPanel) {
        me.getFeature().removeTab(hostedPanel);
      }
    }

    if (model && (model.get('format') === 'maven2' && model.get('type') === 'proxy')) {
      if (!proxyPanel) {
        me.getFeature().addTab({ xtype: 'nx-coreui-routing-proxy-repository-settings', title: 'Routing' });
        proxyPanel = me.getProxyPanel();
      }
      proxyPanel.setRepository(model);
      me.loadSettings(proxyPanel);
    }
    else {
      if (proxyPanel) {
        me.getFeature().removeTab(proxyPanel);
      }
    }
  },

  /**
   * @private
   * Load routing settings for current repository.
   */
  loadSettings: function(panel) {
    var form = panel.down('nx-settingsform');

    if (panel.active) {
      form.fireEvent('load', form);
    }
  },

  /**
   * @private
   * Enable/Disable discovery interval.
   */
  onDiscoveryEnabledChange: function(discoveryEnabled) {
    var discoveryInterval = discoveryEnabled.up('form').down('#discoveryInterval');

    if (discoveryEnabled.getValue()) {
      discoveryInterval.setValue(24);
      discoveryInterval.enable();
    }
    else {
      discoveryInterval.setValue(undefined);
      discoveryInterval.disable();
    }
  },

  /**
   * @private
   * Force updates prefix list.
   */
  updateNow: function() {
    var me = this,
        panel = me.getProxyPanel();

    NX.direct.coreui_RoutingRepositorySettings.updatePrefixFile(panel.getRepository().getId(), function(response) {
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Discovery started for: ' + panel.getRepository().get('name'), type: 'success'
        });
        me.loadSettings(panel);
      }
    });
  }

});