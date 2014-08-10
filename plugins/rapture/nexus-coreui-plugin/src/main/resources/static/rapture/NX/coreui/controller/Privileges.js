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
 * Privilege controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Privileges', {
  extend: 'NX.controller.MasterDetail',

  list: 'nx-coreui-privilege-list',

  stores: [
    'Privilege'
  ],
  views: [
    'privilege.PrivilegeFeature',
    'privilege.PrivilegeList',
    'privilege.PrivilegeAddRepositoryTarget',
    'privilege.PrivilegeTrace'
  ],
  refs: [
    {
      ref: 'feature',
      selector: 'nx-coreui-privilege-feature'
    },
    {
      ref: 'list',
      selector: 'nx-coreui-privilege-list'
    },
    {
      ref: 'info',
      selector: 'nx-coreui-privilege-feature nx-info-panel'
    }
  ],
  icons: {
    'privilege-default': {
      file: 'medal_gold_1.png',
      variants: ['x16', 'x32']
    },
    'privilege-method': {
      file: 'medal_gold_1.png',
      variants: ['x16', 'x32']
    },
    'privilege-repository': {
      file: 'database.png',
      variants: ['x16', 'x32']
    },
    'privilege-target': {
      file: 'target.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/Security/Privileges',
    description: 'Manage privileges',
    view: { xtype: 'nx-coreui-privilege-feature' },
    iconConfig: {
      file: 'medal_gold_1.png',
      variants: ['x16', 'x32']
    },
    visible: function () {
      return NX.Permissions.check('security:privileges', 'read');
    },
    weight: 10
  },
  permission: 'security:privileges',

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.callParent();

    me.listen({
      component: {
        'nx-coreui-privilege-list menuitem[action=newrepositorytarget]': {
          click: me.showAddWindowRepositoryTarget
        },
        'nx-coreui-privilege-add-repositorytarget form': {
          submitted: me.onSettingsSubmitted
        },
        'nx-coreui-privilege-add-repositorytarget #repositoryId': {
          change: me.filterRepositoryTargets
        }
      }
    });
  },

  getDescription: function (model) {
    return model.get('name');
  },

  onSelection: function (list, model) {
    var me = this,
        info;

    if (Ext.isDefined(model)) {
      me.getFeature().setDescriptionIconName('privilege-' + model.get('type'));
      info = {
        'Id': model.get('id'),
        'Name': model.get('name'),
        'Description': model.get('description'),
        'Method': model.get('method')
      };
      if (model.get('permission')) {
        info['Permission'] = model.get('permission');
      }
      if (model.get('repositoryTargetName')) {
        info['Repository Target'] = model.get('repositoryTargetName');
      }
      if (model.get('repositoryName')) {
        info['Repository'] = model.get('repositoryName');
      }
      me.getInfo().showInfo(info);
    }
  },

  /**
   * @private
   */
  showAddWindowRepositoryTarget: function () {
    Ext.widget('nx-coreui-privilege-add-repositorytarget');
  },

  /**
   * @private
   */
  filterRepositoryTargets: function (repositoryIdCombo) {
    var targetCombo = repositoryIdCombo.up('form').down('#repositoryTargetId'),
        repositoryId = repositoryIdCombo.getValue(),
        format;

    targetCombo.setValue(undefined);
    if (repositoryId) {
      format = repositoryIdCombo.getStore().getById(repositoryId).get('format');
      targetCombo.getStore().filterByFormat(format);
    }
    else {
      targetCombo.getStore().removeFilterByFormat();
    }
  },

  /**
   * @protected
   * Enable 'Delete' when user has 'delete' permission and privilege is not read only.
   */
  bindDeleteButton: function (button) {
    var me = this;
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted(me.permission, 'delete'),
            NX.Conditions.gridHasSelection(me.list, function (model) {
              return !model.get('readOnly');
            })
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   */
  onSettingsSubmitted: function (form, action) {
    var me = this,
        win = form.up('nx-coreui-privilege-add-repositorytarget');

    if (win) {
      win.close();
      me.loadStoreAndSelect(action.result.data[0].id);
      Ext.Array.each(action.result.data, function (privilege) {
        NX.Messages.add({
          text: 'Privilege created: ' + privilege.name,
          type: 'success'
        });
      });
    }
    else {
      me.loadStore();
    }
  },

  /**
   * @private
   * @override
   * Deletes a privilege.
   * @param model privilege to be deleted
   */
  deleteModel: function (model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_Privilege.delete_(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Privilege deleted: ' + description, type: 'success'
        });
      }
    });
  }

});