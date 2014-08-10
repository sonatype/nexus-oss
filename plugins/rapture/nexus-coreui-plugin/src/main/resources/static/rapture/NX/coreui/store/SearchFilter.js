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
 * Search Filter store.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.store.SearchFilter', {
  extend: 'Ext.data.Store',
  model: 'NX.coreui.model.SearchFilter',

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
      description: 'Search for components by Maven coordinates',
      readOnly: true,
      criterias: [
        { id: 'format', value: 'maven2', hidden: true },
        { id: 'groupid' },
        { id: 'artifactid' },
        { id: 'version' },
        { id: 'classifier'},
        { id: 'packaging' }
      ]
    },
    // TODO Comment for now as we do not yet support P2 search
    //{
    //  id: 'p2',
    //  name: 'P2',
    //  description: 'Search for components P2 symbolic-name',
    //  readOnly: true,
    //  criterias: [
    //    { id: 'format', value: 'p2', hidden: true },
    //    { id: 'symbolicname' },
    //    { id: 'version' }
    //  ]
    //},
    {
      id: 'keyword',
      name: 'Keyword',
      description: 'Search for components by keyword',
      readOnly: true,
      criterias: [
        { id: 'keyword' }
      ]
    },
    {
      id: 'sha-1',
      name: 'SHA-1',
      description: 'Search for components by SHA-1',
      readOnly: true,
      criterias: [
        { id: 'sha-1' }
      ]
    },
    {
      id: 'classname',
      name: 'Class Name',
      description: 'Search for components by class-name',
      readOnly: true,
      criterias: [
        { id: 'classname' }
      ]
    },
    {
      id: 'custom',
      name: 'Custom',
      description: 'Search for components by custom criteria',
      readOnly: true
    }
  ]

});
