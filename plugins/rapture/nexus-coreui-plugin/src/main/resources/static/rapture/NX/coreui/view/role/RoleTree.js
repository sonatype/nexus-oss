/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
 * Role tree panel.
 *
 * @since 2.8
 */
Ext.define('NX.coreui.view.role.RoleTree', {
  extend: 'Ext.tree.Panel',
  alias: 'widget.nx-coreui-role-tree',

  rootVisible: false,
  lines: false,

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
      fields: ['text', 'weight'],
      root: {
        expanded: true,
        text: 'Roles',
        children: []
      }
    });

    me.listeners = {
      activate: function () {
        me.active = true;
        me.buildTree();
      },
      deactivate: function () {
        me.active = false;
      }
    };

    me.callParent(arguments);
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
    var me = this,
        children = [];

    if (me.active) {
      Ext.suspendLayouts();
      me.getStore().getRootNode().removeAll();
      if (Ext.isDefined(me.model) && me.roleStore.getCount() > 0 && me.privilegeStore.getCount() > 0) {
        me.pushIf(children, me.expandRoles(me.model.roles));
        me.pushIf(children, me.expandPrivileges(me.model.privileges));
        if (children.length) {
          me.getStore().getRootNode().appendChild(children);
          me.getStore().sort([
            { property: 'weight', direction: 'ASC' },
            { property: 'text', direction: 'ASC' }
          ]);
        }
      }
      Ext.resumeLayouts(true);
    }
  },

  /**
   * @private
   */
  expandRoles: function (roleIds) {
    var me = this,
        children = [];

    if (roleIds) {
      Ext.each(Ext.Array.from(roleIds), function (roleId) {
        var role = me.roleStore.getById(roleId),
            expanded = [];

        if (role) {
          me.pushIf(expanded, me.expandRoles(role.get('roles')));
          me.pushIf(expanded, me.expandPrivileges(role.get('privileges')));
          children.push({
            text: role.get('name'),
            children: expanded.length ? expanded : undefined,
            leaf: expanded.length === 0,
            iconCls: NX.Icons.cls('role-default', 'x16'),
            weight: 0
          });
        }
      });
    }

    return children.length ? children : undefined;
  },

  /**
   * @private
   */
  expandPrivileges: function (privilegeIds) {
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
    }

    return children.length ? children : undefined;
  },

  /**
   * @private
   */
  pushIf: function (array, entries) {
    if (array && entries && entries.length) {
      Ext.Array.push(array, entries);
    }
  }

});
