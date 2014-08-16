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
 * Privilege trace panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.privilege.PrivilegeTrace', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-privilege-trace',
  requires: [
    'Ext.data.TreeStore',
    'Ext.util.MixedCollection',
    'NX.Icons',
    'NX.coreui.store.Role',
    'NX.coreui.store.Privilege'
  ],

  layout: {
    type: 'hbox',
    align: 'stretch'
  },

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.privilegeTreeStore = Ext.create('Ext.data.TreeStore', {
      fields: ['text', 'roleTree'],
      root: {
        expanded: true,
        text: 'Privileges',
        children: [
          { text: 'x'}
        ]
      }
    });

    me.roleTreeStore = Ext.create('Ext.data.TreeStore', {
      root: {
        expanded: true,
        text: 'Roles',
        children: []
      }
    });

    me.items = [
      {
        xtype: 'treepanel',
        itemId: 'privilegeTree',
        title: 'Privileges',
        rootVisible: false,
        lines: false,
        width: '50%',
        padding: '0 1 0 0',
        store: me.privilegeTreeStore,
        listeners: {
          select: me.buildRoleTree,
          scope: me
        }
      },
      {
        xtype: 'treepanel',
        itemId: 'roleTree',
        title: 'Role Containment',
        tools: [
          { type: 'collapse', tooltip: 'Collapse all', callback: function(panel) {
            panel.collapseAll();
          }},
          { type: 'expand', tooltip: 'Expand all', callback: function(panel) {
            panel.expandAll();
          }}
        ],
        rootVisible: false,
        lines: false,
        region: 'center',
        width: '50%',
        padding: '0 0 0 1',
        store: me.roleTreeStore
      }
    ];

    me.roleStore = Ext.create('NX.coreui.store.Role');
    me.mon(me.roleStore, 'load', me.buildTree, me);
    me.roleStore.load();

    me.privilegeStore = Ext.create('NX.coreui.store.Privilege');
    me.mon(me.privilegeStore, 'load', me.buildTree, me);
    me.privilegeStore.load();

    me.callParent(arguments);
  },

  listeners: {
    activate: function () {
      var me = this;
      me.active = true;
      me.buildTree();
    },
    deactivate: function () {
      var me = this;
      me.active = false;
    }
  },

  /**
   * @public
   * Loads the roles / privileges of specified model into the privilege tree.
   * @param {Object} model an object containing roles/privileges
   * @param {Object/String[]} model.roles a role id/array of role ids
   * @param {Object/String[]} model.privileges a privilege id/array of privilege ids
   */
  loadRecord: function (model) {
    var me = this;

    me.model = model;
    me.buildTree();
  },

  /**
   * @private
   * Builds the tree for current model.
   */
  buildTree: function () {
    var me = this,
        privileges = Ext.create('Ext.util.MixedCollection');

    if (me.active) {
      Ext.suspendLayouts();
      if (me.model.privileges) {
        Ext.Array.each(Ext.Array.from(me.model.privileges), function (privilegeId) {
          me.pushPrivilege(privileges, privilegeId);
        });
      }
      me.extractPrivileges(privileges, [], me.model.roles);
      me.privilegeTreeStore.getRootNode().removeAll();
      me.roleTreeStore.getRootNode().removeAll();
      privileges.each(function (entry) {
        var privilege = me.privilegeStore.getById(entry.id);
        if (privilege) {
          me.privilegeTreeStore.getRootNode().appendChild({
            text: privilege.get('name'),
            roleTree: entry.roleTree,
            leaf: true,
            iconCls: NX.Icons.cls('privilege-' + privilege.get('type'), 'x16')
          });
        }
      });
      me.privilegeTreeStore.sort({ property: 'text', direction: 'ASC' });
      Ext.resumeLayouts(true);
    }
  },

  /**
   * @private
   * Builds role tree for selected privilege.
   */
  buildRoleTree: function (privilegeTree, privilegeNode) {
    var me = this;

    Ext.suspendLayouts();
    me.roleTreeStore.getRootNode().removeAll();
    me.addRoles(me.roleTreeStore.getRootNode(), privilegeNode.get('roleTree'));
    me.roleTreeStore.sort({ property: 'text', direction: 'ASC' });
    Ext.resumeLayouts(true);
  },

  /**
   * @private
   * Adds roles to parent node recursively.
   */
  addRoles: function (parentNode, roleTree) {
    var me = this,
        createdAnyNode = false;

    if (roleTree) {
      Ext.Object.each(roleTree, function (roleId, roles) {
        var role = me.roleStore.getById(roleId),
            node;

        if (role) {
          node = parentNode.appendChild({
            text: role.get('name'),
            leaf: false,
            iconCls: NX.Icons.cls('role-default', 'x16')
          });
          createdAnyNode = true;
          node.set('leaf', !me.addRoles(node, roles));
        }
      });
    }
    return createdAnyNode;
  },

  /**
   * @private
   * Extracts privileges from roles.
   */
  extractPrivileges: function (privileges, parentRolePath, roleIds) {
    var me = this;

    if (roleIds) {
      Ext.each(Ext.Array.from(roleIds), function (roleId) {
        var role = me.roleStore.getById(roleId),
            rolePath = Ext.Array.push(Ext.Array.clone(parentRolePath), roleId),
            privilegeIds;

        if (role) {
          privilegeIds = role.get('privileges');
          if (privilegeIds) {
            Ext.Array.each(Ext.Array.from(privilegeIds), function (privilegeId) {
              me.pushPrivilege(privileges, privilegeId, rolePath);
            });
          }
          me.extractPrivileges(privileges, rolePath, role.get('roles'));
        }
      });
    }
  },

  /**
   * @private
   */
  pushPrivilege: function (privileges, privilegeId, rolePath) {
    var entry = privileges.get(privilegeId),
        tree;

    if (!entry) {
      privileges.add(entry = {
        id: privilegeId,
        roleTree: {}
      });
    }
    if (rolePath) {
      tree = entry.roleTree;
      Ext.Array.each(Ext.Array.from(rolePath), function (entry) {
        var children = tree[entry];
        if (!children) {
          tree[entry] = {};
        }
        tree = tree[entry];
      });
    }
  }

});
