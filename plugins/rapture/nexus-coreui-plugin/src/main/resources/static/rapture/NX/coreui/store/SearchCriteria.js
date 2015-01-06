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
 * Search Criteria store.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.store.SearchCriteria', {
  extend: 'Ext.data.Store',
  model: 'NX.coreui.model.SearchCriteria',
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
      id: 'format',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_FORMAT')
      }
    },
    {
      id: 'keyword',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_KEYWORD'),
        width: 250
      }
    },
    {
      id: 'version',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_VERSION')
      }
    },
    {
      id: 'groupid',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_GROUP_ID'),
        width: 250
      }
    },
    {
      id: 'artifactid',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_ARTIFACT_ID')
      }
    },
    {
      id: 'classifier',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_CLASSIFIER')
      }
    },
    {
      id: 'packaging',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_PACKAGING')
      }
    },
    {
      id: 'sha-1',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_SHA_1'),
        width: 250
      }
    },
    {
      id: 'classname',
      config: {
        fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_CLASS_NAME'),
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
