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
 * Search feature.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.search.SearchFeature', {
  extend: 'NX.view.drilldown.Drilldown',
  alias: 'widget.nx-searchfeature',

  // TODO: Needed?
  itemId: 'searchfeature',

  iconName: 'search-default',

  masters: [
    {
      xtype: 'panel',
      layout: 'border',
      items: [
        {
          xtype: 'panel',
          itemId: 'criteria',

          region: 'north',
          header: false,

          style: {
            'border-bottom': '1px solid #000000'
          },

          layout: {
            type: 'hbox',
            align: 'bottom'
          },
          bodyPadding: 10

          // disable saving for now
          //tbar: [
          //  { xtype: 'button', text: 'Save', glyph: 'xf0c7@FontAwesome', action: 'save' },
          //],
        },
        {
          xtype: 'nx-coreui-search-result-list',
          region: 'center',
          header: false
        }
      ]
    },
    {
      xtype: 'panel',
      region: 'center',
      layout: {
        type: 'vbox',
        align: 'stretch',
        pack: 'start'
      },
      items: [
        {
          itemId: 'searchResultDetails',
          xtype: 'nx-coreui-search-result-details'
        },
        {
          xtype: 'nx-drilldown-actions'
        },
        {
          xtype: 'nx-coreui-search-result-version-list',
          flex: 1
        }
      ]
    }
  ],

  detail: {
    xtype: 'panel',

    layout: {
      type: 'vbox',
      align: 'stretch',
      pack: 'start'
    },

    items: [
      {
        xtype: 'nx-drilldown-actions'
      },
      {
        xtype: 'nx-coreui-repositorybrowse-storagefilecontainer',
        header: false,
        flex: 1
      }
    ]
  },

  initComponent: function() {
    var me = this;

    me.callParent(arguments);

    // Set default icon for the component version list
    var item = me.down('nx-coreui-search-result-version-list').up('nx-drilldown-item');
    item.setItemClass('nx-icon-search-component-detail-x16');
  }
});
