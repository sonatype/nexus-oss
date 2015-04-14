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
Ext.define('NX.coreui.service.ProxyFacet',  {
  extends: 'NX.coreui.service.RepositoryFacet',

  fields: function() {
    return [
      {
        id: 'proxy.remoteUrl',
        label: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_REMOTE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_REMOTE_HELP'),
        required: true,
        vtype: 'url'
      },
      {
        id: 'proxy.artifactMaxAge',
        type: 'number',
        label: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ARTIFACT_AGE'),
        helpText: NX.I18n.get('ADMIN_REPOSITORIES_SETTINGS_ARTIFACT_AGE_HELP'),
        required: true,
        minValue: -1,
        maxValue: 3600
      }
    ]
  },
  toMap: function(properties, attributes) {
    Ext.apply(attributes, {
      proxy: {
        remoteUrl: properties['proxy.remoteUrl'],
        artifactMaxAge: parseInt(properties['proxy.artifactMaxAge'])
      }
    });
  },
  toProperties: function(attributes, properties) {
    Ext.apply(properties, {
      'proxy.remoteUrl': attributes.proxy.remoteUrl,
      'proxy.artifactMaxAge': attributes.proxy.artifactMaxAge
    });
  }
  ,
  defaultProperties: function(properties){
    throw 'Not implemented';
  },
  getName: function() {
   return 'proxy';
  }
});
