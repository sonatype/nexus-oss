/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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
 * Permissions management controller.
 *
 * @since 3.0
 */
Ext.define('NX.controller.Permissions', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.State',
    'NX.Permissions'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  stores: [
    'Permission'
  ],

  init: function () {
    var me = this;

    me.listen({
      controller: {
        '#State': {
          userchanged: me.fetchPermissions,
          commandfetchpermissions: me.fetchPermissions
        }
      },
      store: {
        '#Permission': {
          load: me.firePermissionsChanged,
          update: me.onUpdate,
          remove: me.firePermissionsChanged
        }
      }
    });

    me.addEvents(
        /**
         * @event changed
         * Fires when permissions change.
         * @param {NX.Permissions}
         */
        'changed'
    );
  },

  /**
   * Prime initial set of permissions from state.
   *
   * @override
   */
  onLaunch: function () {
    var me = this,
        rawData = NX.State.getValue('permissions');

    //<if debug>
    me.logTrace('Initial permissions:', Ext.encode(rawData));
    //</if>

    me.getStore('Permission').loadRawData(rawData, false);
    NX.Permissions.setPermissions(me.getPermissions());

    //<if debug>
    me.logInfo('Permissions primed');
    //</if>
  },

  /**
   * @private
   */
  onUpdate: function (store, record, operation) {
    if (operation === Ext.data.Model.COMMIT) {
      this.firePermissionsChanged();
    }
  },

  /**
   * @private
   */
  fetchPermissions: function () {
    var me = this;

    //<if debug>
    me.logDebug('Fetching permissions...');
    //</if>

    NX.Permissions.resetPermissions();
    me.getStore('Permission').load();
  },

  /**
   * @private
   */
  firePermissionsChanged: function () {
    var me = this;

    NX.Permissions.setPermissions(me.getPermissions());

    //<if debug>
    me.logDebug('Permissions changed; Firing event');
    //</if>

    me.fireEvent('changed', NX.Permissions);
  },

  /**
   * @private
   * @return {object} permissions
   */
  getPermissions: function () {
    var store = this.getStore('Permission'),
        perms = {};

    store.clearFilter();
    store.each(function (rec) {
      perms[rec.get('id')] = rec.get('permitted');
    });

    return perms;
  }

});
