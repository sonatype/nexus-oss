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
 * Role tree panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.role.RoleTree', {
  extend: 'Ext.panel.Panel',
  alias: 'widget.nx-coreui-role-tree',
  requires: [
    'Ext.data.TreeStore',
    'NX.Icons',
    'NX.coreui.store.Role',
    'NX.coreui.store.Privilege'
  ],

  layout: 'fit',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.roleStore = Ext.create('NX.coreui.store.Role');
    me.mon(me.roleStore, 'load', me.buildTree, me);
    me.roleStore.load();

    me.privilegeStore = Ext.create('NX.coreui.store.Privilege');
    me.mon(me.privilegeStore, 'load', me.buildTree, me);
    me.privilegeStore.load();

    me.store = Ext.create('Ext.data.TreeStore', {
      fields: ['text', 'weight', 'roles', 'privileges'],
      root: {
        expanded: true,
        text: 'Roles',
        children: []
      }
    });

    me.items = {
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
      store: me.store,
      listeners: {
        beforeitemexpand: function (node) {
          if (!node.processed) {
            Ext.suspendLayouts();
            me.addRoles(node, node.get('roles'));
            me.addPrivileges(node, node.get('privileges'));
            me.store.sort([
              { property: 'weight', direction: 'ASC' },
              { property: 'text', direction: 'ASC' }
            ]);
            node.processed = true;
            Ext.resumeLayouts(true);
          }
        }
      }
    };

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
   * Loads the roles / privileges of specified model into the role tree.
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
    var me = this;

    if (me.active) {
      Ext.suspendLayouts();
      me.store.getRootNode().removeAll();
      if (Ext.isDefined(me.model) && (me.roleStore.getCount() || me.privilegeStore.getCount())) {
        me.addRoles(me.store.getRootNode(), me.model.roles);
        me.addPrivileges(me.store.getRootNode(), me.model.privileges);
        me.store.sort([
          { property: 'weight', direction: 'ASC' },
          { property: 'text', direction: 'ASC' }
        ]);
      }
      Ext.resumeLayouts(true);
    }
  },

  /**
   * @private
   * Add roles to node.
   */
  addRoles: function (node, roleIds) {
    var me = this,
        children = [];

    if (roleIds) {
      Ext.each(Ext.Array.from(roleIds), function (roleId) {
        var role = me.roleStore.getById(roleId);
        if (role) {
          children.push({
            text: role.get('name'),
            roles: role.get('roles'),
            privileges: role.get('privileges'),
            leaf: false,
            iconCls: NX.Icons.cls('role-default', 'x16'),
            weight: 0
          });
        }
      });
      if (children.length) {
        node.appendChild(children);
      }
    }
  },

  /**
   * @private
   * Add privileges to node.
   */
  addPrivileges: function (node, privilegeIds) {
    var me = this,
        children = [];

    if (privilegeIds) {
      Ext.each(Ext.Array.from(privilegeIds), function (privilegeId) {
        var privilege = me.privilegeStore.getById(privilegeId);
        if (privilege) {
          children.push({
            text: privilege.get('name'),
            leaf: true,
            iconCls: NX.Icons.cls('privilege-' + privilege.get('type'), 'x16'),
            weight: 1
          });
        }
      });
      if (children.length) {
        node.appendChild(children);
      }
    }
  }
});
