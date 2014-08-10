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
 * Repository model.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.model.Repository', {
  extend: 'Ext.data.Model',
  fields: [
    'id',
    'name',
    'type',
    'provider',
    'providerName',
    'format',
    'formatName',
    'exposed',
    'localStatus',
    'url',
    'defaultLocalStorageUrl',
    'overrideLocalStorageUrl',
    'userManaged',
    'browseable',                         // hosted/proxy
    'writePolicy',                        // hosted
    'indexable',                          // hosted maven
    'repositoryPolicy',                   // hosted/proxy maven
    'proxyMode',                          // proxy
    'remoteStatus',                       // proxy
    'remoteStatusReason',                 // proxy
    'remoteStorageUrl',                   // proxy
    'useTrustStoreForRemoteStorageUrl',   // proxy
    'autoBlockActive',                    // proxy
    'fileTypeValidation',                 // proxy
    'authEnabled',                        // proxy
    'authUsername',                       // proxy
    'authPassword',                       // proxy
    'authNtlmHost',                       // proxy
    'authNtlmDomain',                     // proxy
    'httpRequestSettings',                // proxy
    'userAgentCustomisation',             // proxy
    'urlParameters',                      // proxy
    'timeout',                            // proxy
    'retries',                            // proxy
    'notFoundCacheTTL',                   // proxy
    'itemMaxAge',                         // proxy
    'downloadRemoteIndexes',              // proxy maven
    'checksumPolicy',                     // proxy maven
    'artifactMaxAge',                     // proxy maven
    'metadataMaxAge',                     // proxy maven
    'memberRepositoryIds',                // group,
    'synchronizeAtStartup',               // virtual
    'shadowOf'                            // virtual
  ]
});
