/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * LDAP Server model.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.model.LdapServer', {
  extend: 'Ext.data.Model',
  fields: [
    'id',
    'name',
    'order',
    'url',

    'protocol',
    'useTrustStore',
    'host',
    'port',
    'searchBase',

    'authScheme',
    'authRealm',
    'authUsername',
    'authPassword',

    'backupMirrorEnabled',
    'backupMirrorProtocol',
    'backupMirrorHost',
    'backupMirrorPort',

    'connectionTimeout',
    'connectionRetryDelay',
    'cacheTimeout',

    'userBaseDn',
    'userSubtree',
    'userObjectClass',
    'userLdapFilter',
    'userIdAttribute',
    'userRealNameAttribute',
    'userMemberOfAttribute',
    'userEmailAddressAttribute',
    'userPasswordAttribute',

    'ldapGroupsAsRoles',
    'groupType',
    'groupBaseDn',
    'groupSubtree',
    'groupIdAttribute',
    'groupMemberAttribute',
    'groupMemberFormat',
    'groupObjectClass'
  ]
});
