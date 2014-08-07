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
/**
 * Search panel.
 * TODO implement
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.search.SearchFeature', {
  extend: 'Ext.Panel',
  alias: 'widget.nx-searchfeature',

  layout: 'border',

  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'panel',
        itemId: 'criteria',

        region: 'north',
        header: false,

        layout: {
          type: 'hbox',
          align: 'bottom'
        },
        bodyPadding: 10

        // disable saving for now
        //tbar: [
        //  { xtype: 'button', text: 'Save', glyph: 'xf0c7@FontAwesome' /* fa-save */, action: 'save' },
        //],
      },
      {
        xtype: 'panel',
        region: 'center',
        layout: 'border',
        items: [
          {
            xtype: 'nx-coreui-search-component-list',
            region: 'center',
            flex: 0.3
          },
          {
            xtype: 'panel',
            itemId: 'rightPanel',
            region: 'east',
            layout: 'border',
            flex: 0.7,
            header: false,
            split: true,
            collapsible: true,
            collapsed: false,
            collapseMode: 'mini',
            hidden: true,
            items: [
              {
                xtype: 'panel',
                region: 'center',
                flex: 0.5,
                items: [
                  {
                    type: 'panel',
                    region: 'center',
                    layout: {
                      type: 'vbox',
                      align: 'stretch',
                      pack: 'start'
                    },
                    items: [
                      {
                        xtype: 'nx-coreui-search-component-details'
                      },
                      {
                        xtype: 'nx-coreui-search-component-version-list'
                      }
                    ]
                  }
                ]
              },
              {
                xtype: 'nx-coreui-repositorybrowse-storagefilecontainer',
                region: 'south',
                split: true,
                flex: 0.5,
                header: false,
                collapsible: true,
                collapsed: false,
                collapseMode: 'mini',
                hidden: true
              }
            ]
          }
        ]
      }
    ];

    me.callParent(arguments);
  }

});