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
 * LDAP Server "User & Group" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerUserAndGroup', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-ldapserver-userandgroup',
  requires: [
    'NX.Conditions'
  ],

  api: {
    submit: 'NX.direct.ldap_LdapServer.update'
  },
  settingsFormSuccessMessage: function (data) {
    return 'LDAP server updated: ' + data['name'];
  },

  editableMarker: 'You do not have permission to update LDAP servers',

  items: { xtype: 'nx-coreui-ldapserver-userandgroup-fieldset' },

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.editableCondition = NX.Conditions.isPermitted('security:ldapconfig', 'update');

    me.callParent(arguments);

    Ext.override(me.getForm(), {
      /**
       * @override
       * Override model with form values.
       */
      getValues: function () {
        var me = this,
            modelData = me.getRecord().getData(false);

        me.getFields().each(function (field) {
          delete modelData[field.getName()];
        });
        return Ext.apply(modelData, me.callParent(arguments));
      }

    });

    me.getDockedItems('toolbar[dock="bottom"]')[0].add(
        { xtype: 'button', text: 'Verify user mapping', formBind: true, action: 'verifyusermapping' },
        { xtype: 'button', text: 'Verify login', formBind: true, action: 'verifylogin' }
    );
  }

});
