/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * A {@link NX.util.condition.Condition} that is satisfied when user has ALL of the specified permissions.
 *
 * @since 3.0
 */
Ext.define('NX.util.condition.IsPermittedAll', {
  extend: 'NX.util.condition.Condition',
  requires: [
    'NX.Permissions'
  ],

  /**
   * @cfg {String[]} permissions to check
   */
  permissions: undefined,

  /**
   * @override
   * @returns {NX.util.condition.IsPermittedAll}
   */
  bind: function () {
    var me = this,
        controller;

    if (!me.bounded) {
      controller = NX.getApplication().getController('Permissions');
      me.mon(controller, {
        changed: me.evaluate,
        scope: me
      });
      me.callParent();
      me.evaluate();
    }

    return me;
  },

  /**
   * @private
   * @returns {boolean}
   */
  evaluate: function () {
    var me = this,
        granted = true;

    if (me.bounded) {
      // check if user has ALL of the given permissions
      Ext.Array.each(me.permissions, function(permission) {
        if (!NX.Permissions.check(permission)) {
          granted = false;
          return false; // break
        }
      });

      me.setSatisfied(granted);
    }
  },

  /**
   * @override
   * @returns {string}
   */
  toString: function () {
    var me = this;
    return me.self.getName() + '{ permissions=' + me.permissions + ' }';
  }
});