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
 * Repository target settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.repositorytarget.RepositoryTargetSettings', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-repositorytarget-settings',
  requires: [
    'NX.Conditions'
  ],

  api: {
    submit: 'NX.direct.coreui_RepositoryTarget.update'
  },
  settingsFormSuccessMessage: function (data) {
    return 'Repository target updated: ' + data['name'];
  },

  editableMarker: 'You do not have permission to update targets',

  initComponent: function () {
    var me = this;

    me.editableCondition = NX.Conditions.isPermitted('nexus:targets', 'update');

    me.items = [
      {
        xtype: 'hiddenfield',
        name: 'id'
      },
      {
        xtype: 'textfield',
        name: 'name',
        itemId: 'name',
        fieldLabel: 'Name',
        helpText: 'The name of the repository target.',
        emptyText: 'enter a target name'
      },
      {
        xtype: 'combo',
        name: 'format',
        fieldLabel: 'Repository Type',
        helpText: 'The content class of the repository target. It will be matched only against repositories with the same content class.',
        emptyText: 'select a repository type',
        editable: false,
        store: 'RepositoryFormat',
        queryMode: 'local',
        displayField: 'name',
        valueField: 'id'
      },
      {
        xtype: 'nx-valueset',
        name: 'patterns',
        itemId: 'patterns',
        fieldLabel: 'Patterns',
        helpText: 'Enter a pattern expression and click "Add" to add it to the list. Regular expressions are used to match the artifact path. ".*" is used to specify all paths. ".*/com/some/company/.*" will match any artifact with "com.some.company" as the group id or artifact id. "^/com/some/company/.*" will match any artifact starting with com/some/company.',
        emptyText: 'enter a pattern expression',
        input: {
          xtype: 'nx-regexp'
        },
        sorted: true
      }
    ];

    me.callParent(arguments);
  }
});
