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
 * Search Criteria store.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.store.SearchCriteria', {
  extend: 'Ext.data.Store',
  model: 'NX.coreui.model.SearchCriteria',

  autoLoad: true,

  proxy: {
    type: 'memory',
    reader: {
      type: 'json'
    }
  },

  data: [
    {
      id: 'format',
      config: {
        fieldLabel: 'Format'
      }
    },
    {
      id: 'keyword',
      config: {
        fieldLabel: 'Keyword',
        width: 250
      }
    },
    {
      id: 'version',
      config: {
        fieldLabel: 'Version'
      }
    },
    {
      id: 'groupid',
      config: {
        fieldLabel: 'Group ID',
        width: 250
      }
    },
    {
      id: 'artifactid',
      config: {
        fieldLabel: 'Artifact ID'
      }
    },
    {
      id: 'classifier',
      config: {
        fieldLabel: 'Classifier'
      }
    },
    {
      id: 'packaging',
      config: {
        fieldLabel: 'Packaging'
      }
    },
    {
      id: 'sha-1',
      config: {
        fieldLabel: 'SHA-1',
        width: 250
      }
    },
    {
      id: 'classname',
      config: {
        fieldLabel: 'Class name',
        width: 250
      }
    }
    // TODO Comment for now as we do not yet support symbolic name search
    //{
    //  id: 'symbolicname',
    //  config: {
    //    fieldLabel: 'Symbolic name'
    //  }
    //}
  ],

  sortOnLoad: true,
  sorters: { property: 'id', direction: 'ASC' }

});
