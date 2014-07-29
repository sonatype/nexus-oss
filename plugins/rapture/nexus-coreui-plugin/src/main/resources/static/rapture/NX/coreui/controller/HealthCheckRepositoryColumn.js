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
 * HealthCheck repository column controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.HealthCheckRepositoryColumn', {
  extend: 'Ext.app.Controller',

  models: [
    'HealthCheckRepositoryStatus'
  ],
  stores: [
    'HealthCheckRepositoryStatus'
  ],
  views: [
    'healthcheck.HealthCheckSummary'
  ],
  refs: [
    { ref: 'list', selector: 'nx-coreui-repository-list' },
    { ref: 'summary', selector: 'nx-coreui-healthcheck-summary' }
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
        }
      },
      store: {
        '#Repository': {
          load: me.loadHealthCheckStatus
        },
        '#HealthCheckRepositoryStatus': {
          load: me.refreshHealthCheckColumn
        }
      },
      component: {
        'nx-coreui-repository-list': {
          afterrender: me.bindHealthCheckColumn
        }
      }
    });
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
        view = grid.getView(),
        column = grid.healthCheckColumn;

    if (!column) {
      column = grid.healthCheckColumn = Ext.create('Ext.grid.column.Column', {
        id: 'healthCheckColumn',
        header: 'Health Check',
        width: 120,
        renderer: Ext.bind(me.renderHealthCheckColumn, me),
        listeners: {
          click: Ext.bind(me.maybeAskToEnable, me)
        }
      });
      grid.headerCt.insert(2, column);
      view.refresh();
      grid.healthCheckTooltip = Ext.create('Ext.ToolTip', {
        target: view.getEl(),
        delegate: view.getCellSelector(column),
        maxWidth: 500,
        mouseOffset: [0, 0],
        renderTo: document.body,
        hideDelay: 5000,
        listeners: {
          beforeshow: Ext.bind(me.updateHealthCheckColumnTooltip, me)
        }
      });
    }
  },

  removeHealthCheckColumn: function(grid) {
    var column = grid.healthCheckColumn;
    if (column) {
      grid.headerCt.remove(column);
      grid.getView().refresh();
      delete grid.healthCheckColumn;
      grid.healthCheckTooltip.destroy();
    }
  },

  renderHealthCheckColumn: function(value, metadata, record) {
    var me = this,
        status = me.getHealthCheckRepositoryStatusStore().getById(record.getId());

    if (status) {
      if (status.get('enabled')) {
        if (status.get('status')) {
          return status.get('status');
        }
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

  updateHealthCheckColumnTooltip: function(tip) {
    var me = this,
        view = me.getList().getView(),
        repository, status, html, cell;

    if (tip.triggerElement) {
      repository = view.getRecord(tip.triggerElement.parentNode)
      if (repository) {
        status = me.getHealthCheckRepositoryStatusStore().getById(repository.getId());
        if (status) {
          if (status.get('enabled')) {
            cell = view.getCell(repository, me.getList().healthCheckColumn);
            Ext.defer(me.showSummary, 0, me, [status, cell.getX(), cell.getY()]);
            return false;
          }
          else if (NX.Permissions.check('nexus:healthcheck', 'update')) {
            html = '<span><h2>Repository Health Check Analysis</h2>Click this button to request a Repository Health Check (RHC) ' +
                'by the Sonatype CLM service.  The process is non-invasive and non-disruptive.  Sonatype CLM ' +
                'will return actionable quality, security, and licensing information about the open source components in the repository.' +
                '<br><br><a href="http://links.sonatype.com/products/clm/rhc/home" ' +
                'target="_blank">How the Sonatype CLM Repository Health Check can help you make better software faster</a></span>';
          }
          else {
            html = '<span><h2>Insufficient Permissions to Analyze a Repository</h2>' +
                'To analyze a repository your user account must have permissions to start analysis.</span>';
          }
        }
        else {
          html = '<span><h2>Repository Health Check Unavailable</h2>A Repository Health Check (RHC) ' +
              'cannot be performed by the Sonatype CLM service on this repository, because it is an unsupported type or out of service.<br><br>' +
              '<a href="http://links.sonatype.com/products/clm/rhc/home" ' +
              'target="_blank">How the Sonatype CLM Repository Health Check can help you make better software faster</a></span>';
        }
        tip.update(html);
        return true;
      }
    }
    return false;
  },

  refreshHealthCheckColumn: function() {
    var me = this,
        list = me.getList();

    if (list) {
      list.getView().refresh();
    }
  },

  showSummary: function(status, x, y) {
    var me = this,
        summary = me.getSummary();

    if (!summary) {
      Ext.widget({
        xtype: 'nx-coreui-healthcheck-summary',
        x: x,
        y: y,
        height: status.get('iframeHeight') + 8,
        width: status.get('iframeWidth') + 8,
        statusModel: status
      });
    }
  },

  maybeAskToEnable: function(gridView, cell, row, col, event, record) {
    var me = this,
        list = me.getList(),
        status = me.getHealthCheckRepositoryStatusStore().getById(record.getId());

    if (status && !status.get('enabled') && NX.Permissions.check('nexus:healthcheck', 'update')) {
      list.healthCheckTooltip.hide();
      Ext.Msg.show({
        title: 'Analyze Repository',
        msg: 'Do you want to analyze the repository ' + Ext.util.Format.htmlEncode(record.get('name'))
            + ' and others for security vulnerabilities and license issues?',
        buttons: 7, // OKYESNO
        buttonText: { ok: 'Yes, all repositories', yes: 'Yes, only this repository' },
        icon: Ext.MessageBox.QUESTION,
        closeable: false,
        fn: function(buttonName) {
          if (buttonName === 'yes' || buttonName === 'ok') {
            if (status.get('eulaAccepted')) {
              me.enableAnalysis(buttonName === 'yes' ? status.getId() : undefined);
            }
            else {
              Ext.widget('nx-coreui-healthcheck-eula', {
                acceptFn: function() {
                  me.enableAnalysis(buttonName === 'yes' ? status.getId() : undefined);
                }
              });
            }
          }
        }
      });
    }
    return false;
  },

  enableAnalysis: function(repositoryId) {
    var me = this;

    if (repositoryId) {
      NX.direct.healthcheck_Status.update(
          { repositoryId: repositoryId, enabled: true, eulaAccepted: true },
          function(response) {
            if (Ext.isObject(response) && response.success) {
              me.getHealthCheckRepositoryStatusStore().load();
            }
          }
      );
    }
  },

  imageUrl: function(name) {
    return NX.util.Url.urlOf('static/rapture/resources/images/' + name);
  }

});