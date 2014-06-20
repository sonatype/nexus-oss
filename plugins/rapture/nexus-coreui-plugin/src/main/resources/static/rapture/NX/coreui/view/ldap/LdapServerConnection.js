/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * LDAP Server "Connection" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.ldap.LdapServerConnection', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-ldapserver-connection',

  api: {
    submit: 'NX.direct.ldap_LdapServer.update'
  },
  settingsFormSuccessMessage: function (data) {
    return 'LDAP server updated: ' + data['name'];
  },
  editableCondition: NX.Conditions.isPermitted('security:ldapconfig', 'update'),
  editableMarker: 'You do not have permission to update LDAP servers',

  items: { xtype: 'nx-coreui-ldapserver-connection-fieldset' },

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

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
        { xtype: 'button', text: 'Verify connection', formBind: true, action: 'verifyconnection' }
    );
  }

});
