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
/*global Ext*/

/**
 * Permissions helper.
 *
 * @since 3.0
 */
Ext.define('NX.Permissions', {
  singleton: true,

  /**
   * @private
   * Map between permissions id and value.
   */
  permissions: undefined,

  /**
   * @public
   * @returns {boolean} True, if permissions had been set (loaded from server)
   */
  available: function() {
    var me = this;
    return Ext.isDefined(me.permissions);
  },

  /**
   * @public
   * Sets permissions.
   */
  setPermissions: function(permissions) {
    var me = this,
        perms = permissions;

    if (Ext.isArray(permissions)) {
      perms = {};
      Ext.each(permissions, function(entry) {
        if (entry.id && entry.value) {
          perms[entry.id] = entry.value;
        }
      });
    }

    me.permissions = Ext.apply({}, perms);
  },

  /**
   * @public
   * Resets all permissions.
   */
  resetPermissions: function() {
    delete this.permissions;
  },

  /**
   * @public
   * @returns {boolean} True if user is authorized for expected permission.
   */
  check: function(name, perm) {
    var me = this,
        expectedPermission = name + ':' + perm,
        hasPermission = false;

    if (!me.available()) {
      return false;
    }

    if (me.permissions[expectedPermission] === true) {
      return true;
    }

    Ext.Object.each(me.permissions, function(permission, given) {
      if (given && me.implies(permission, expectedPermission)) {
        hasPermission = true;
        return false;
      }
      return true;
    });

    return hasPermission;
  },

  /**
   * @private
   *
   * Returns true if permission 1 implies permission 2 using logic similar to Shiro wildcard permission.
   */
  implies: function(permission1, permission2){
    var parts1 = permission1.split(':'),
        parts2 = permission2.split(':'),
        part1, part2, i;

    for (i = 0; i < parts2.length; i++) {
      // If this permission has less parts than the other permission, everything after the number of parts contained
      // in this permission is automatically implied, so return true
      if (parts1.length - 1 < i) {
        return true;
      }
      else {
        part1 = parts1[i];
        part2 = parts2[i];
        if (part1.indexOf('*') === -1 && part1 !== part2) {
          return false;
        }
      }
    }

    // If this permission has more parts than the other parts, only imply it if all of the other parts are wildcards
    for (; i < parts1.length; i++) {
      part1 = parts1[i];
      if (part1.indexOf('*') === -1) {
        return false;
      }
    }

    return true;
  },

  /**
   * @public
   * @returns {boolean} True if user is authorized for at least one permission that starts with expected string.
   */
  checkAny: function(perm) {
    var me = this,
        hasAny = false;

    if (!me.available()) {
      return false;
    }

    Ext.Object.each(me.permissions, function(key, value) {
      if (Ext.String.startsWith(key, perm) && value === true) {
        hasAny = true;
        return false;
      }
      return true;
    });

    return hasAny;
  }

});
