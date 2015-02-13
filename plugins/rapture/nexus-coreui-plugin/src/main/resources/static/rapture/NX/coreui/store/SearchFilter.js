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
 * Search Filter store.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.store.SearchFilter', {
  extend: 'Ext.data.Store',
  model: 'NX.coreui.model.SearchFilter',
  requires: [
    'NX.I18n',
  ],

  autoLoad: true,

  proxy: {
    type: 'memory',
    reader: {
      type: 'json'
    }
  },

  data: [
    {
      id: 'maven2',
      name: 'Maven',
      text: NX.I18n.get('BROWSE_SEARCH_MAVEN_TITLE'),
      description: NX.I18n.get('BROWSE_SEARCH_MAVEN_SUBTITLE'),
      readOnly: true,
      criterias: [
        { id: 'format', value: 'maven2', hidden: true },
        { id: 'group.raw' },
        { id: 'name.raw' },
        { id: 'version' },
        { id: 'attributes.maven.classifier'},
        { id: 'attributes.maven.packaging' }
      ]
    },
    {
      id: 'raw',
      name: 'Raw',
      text: NX.I18n.get('BROWSE_SEARCH_RAW_TITLE'),
      description: NX.I18n.get('BROWSE_SEARCH_RAW_SUBTITLE'),
      readOnly: true,
      criterias: [
        { id: 'format', value: 'raw', hidden: true },
        { id: 'attributes.raw.path.tree' }
      ]
    },
    {
      id: 'keyword',
      name: 'Keyword',
      text: 'Keyword',
      description: 'Search for components by keyword',
      readOnly: true,
      criterias: [
        { id: 'keyword' }
      ]
    },
    {
      id: 'custom',
      name: 'Custom',
      text: NX.I18n.get('BROWSE_SEARCH_CUSTOM_TITLE'),
      description: NX.I18n.get('BROWSE_SEARCH_CUSTOM_SUBTITLE'),
      readOnly: true
    }
  ]

});
