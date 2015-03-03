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
 * NuGet repository search contribution.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.SearchNuget', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.I18n',
  ],

  /**
   * @override
   */
  init: function() {
    var me = this,
        search = me.getController('NX.coreui.controller.Search');

    search.registerCriteria([
      {
        id: 'attributes.nuget.id',
        group: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_GROUP_NUGET'),
        config: {
          fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_NUGET_ID'),
          width: 300
        }
      },
      {
        id: 'attributes.nuget.tags',
        group: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_GROUP_NUGET'),
        config: {
          fieldLabel: NX.I18n.get('BROWSE_SEARCH_COMPONENTS_CRITERIA_NUGET_TAGS'),
          width: 300
        }
      }
    ], me);

    search.registerFilter({
      id: 'nuget',
      name: 'NuGet',
      text: NX.I18n.get('BROWSE_SEARCH_NUGET_TITLE'),
      description: NX.I18n.get('BROWSE_SEARCH_NUGET_SUBTITLE'),
      readOnly: true,
      criterias: [
        { id: 'format', value: 'nuget', hidden: true },
        { id: 'attributes.nuget.id' },
        { id: 'attributes.nuget.tags' }
      ]
    }, me);
  }

});
