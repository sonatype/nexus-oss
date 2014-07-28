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
 * HealthCheck repository settings controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.HealthCheckRepositorySettings', {
  extend: 'Ext.app.Controller',

  models: [
    'HealthCheckRepositoryStatus'
  ],
  stores: [
    'HealthCheckRepositoryStatus'
  ],
  views: [
    'healthcheck.HealthCheckRepositorySettings',
    'healthcheck.HealthCheckEula'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-repository-feature' },
    { ref: 'panel', selector: 'nx-coreui-healthcheck-repository-settings' },
    { ref: 'list', selector: 'nx-coreui-repository-list' },
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.listen({
      controller: {
        '#Permissions': {
          changed: me.refreshHealthCheckColumn
        },
        '#Refresh': {
          refresh: me.loadHealthCheckStatus
        }
      },
      component: {
        'nx-coreui-repository-list': {
          beforerender: me.loadHealthCheckStatus,
          afterrender: me.bindHealthCheckColumn,
          selection: me.onSelection
        },
        'nx-coreui-healthcheck-repository-settings': {
          activate: me.onActivate,
          deactivate: me.onDeactivate
        },
        'nx-coreui-healthcheck-eula button[action=agree]': {
          click: me.onAgree
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
      me.loadSettings()
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

  /**
   * @private
   * Show EULA if not already accepted.
   */
  onAgree: function(button) {
    var me = this,
        win = button.up('window'),
        form = me.getPanel().down('nx-settingsform'),
        saveButton = form.down('button[action=save]');

    win.close();
    form.getForm().setValues({ eulaAccepted: true });
    saveButton.fireEvent('click', saveButton);
  },

  loadHealthCheckStatus: function() {
    var me = this,
        list = me.getList();

    if (list) {
      me.getHealthCheckRepositoryStatusStore().load();
    }
  },

  bindHealthCheckColumn: function(grid) {
    var me = this;
    grid.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted("nexus:healthcheck", "read")
        ),
        {
          satisfied: Ext.pass(me.addHealthCheckColumn, grid),
          unsatisfied: Ext.pass(me.removeHealthCheckColumn, grid),
          scope: me
        }
    );
  },

  addHealthCheckColumn: function(grid) {
    var me = this,
        column = grid.healthCheckColumn;

    if (!column) {
      column = grid.healthCheckColumn = Ext.create('Ext.grid.column.Column', {
        header: 'Health Check',
        width: 120,
        renderer: Ext.bind(me.renderHealthCheckColumn, me)
      });
      grid.headerCt.insert(2, column);
      grid.getView().refresh();
    }
  },

  removeHealthCheckColumn: function(grid) {
    var column = grid.healthCheckColumn;
    if (column) {
      grid.headerCt.remove(column);
      grid.getView().refresh();
      delete grid.healthCheckColumn;
    }
  },

  renderHealthCheckColumn: function(value, metadata, record) {
    var me = this,
        status = me.getHealthCheckRepositoryStatusStore().getById(record.getId());

    if (status) {
      if (status.get('enabled')) {
        return '<div><img src="' + me.imageUrl('security-alert.png') + '">&nbsp;'
            + status.get('securityIssueCount') + '&nbsp;&nbsp;<img src="' + me.imageUrl('license-alert.png')
            + '" style="margin-left:10px">&nbsp;' + status.get('licenseIssueCount') + '</div>';
      }
      else if (NX.Permissions.check('nexus:healthcheck', 'update')) {
        return '<div><img src="' + me.imageUrl('analyze.png') + '"></div>';
      }
    }
    return '<div><img src="' + me.imageUrl('analyze_disabled.png') + '"></div>';
  },

  refreshHealthCheckColumn: function() {
    this.getList().getView().refresh();
  },

  imageUrl: function(name) {
    return NX.util.Url.urlOf('static/rapture/resources/images/' + name);
  }

});