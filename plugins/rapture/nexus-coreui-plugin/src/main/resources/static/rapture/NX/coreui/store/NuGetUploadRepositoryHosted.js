/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * NuGet Upload Hosted Repository store.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.store.NuGetUploadRepositoryHosted', {
  extend: 'NX.coreui.store.RepositoryReference',

  remoteFilter: true,
  filters: [
    { property: 'type', value: 'hosted' },
    { property: 'format', value: 'nuget' }
  ]

});
