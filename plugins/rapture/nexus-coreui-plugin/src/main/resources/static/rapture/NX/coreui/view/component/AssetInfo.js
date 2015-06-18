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
 * Asset info panel.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.component.AssetInfo', {
  extend: 'NX.view.info.Panel',
  alias: 'widget.nx-coreui-component-assetinfo',
  requires: [
    'NX.I18n'
  ],

  /**
   * model to display 
   */
  assetModel: null,
  
  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    var attributeGrid = Ext.create('Ext.grid.Panel', {
          title: 'Attributes',
          itemId: 'attributeGrid',
          store: Ext.create('Ext.data.Store', {
            fields: ['facet', 'label', 'value'],
            groupField: 'facet'
          }),
          columns: {
            items: [
              {
                text: 'Facet',
                hidden: true,
                dataIndex: 'facet'
              },
              {
                text: 'label',
                flex: 1,
                dataIndex: 'label'
              },
              {
                text: 'Value',
                flex: 2,
                dataIndex: 'value'
              }
            ]
          },
          hideHeaders: true,
          features: [
            {
              ftype: 'grouping',
              groupHeaderTpl: '{name:capitalize}'
            }
          ],
          autoScroll: true,
          padding: '10 10 5 10',
        }
    );
    me.callParent(arguments);

    me.add([
      attributeGrid
    ]);
    me.setTitle(NX.I18n.get('Component_AssetInfo_Info_Title'));
  },

  /**
   * @public
   * @param {object} assetModel
   */
  setAssetModel: function(assetModel) {
    var me = this, panel = me.down('#attributeGrid'), store = panel.getStore(), info = {};
    me.assetModel = assetModel;

    // display common data
    info[NX.I18n.get('Assets_Info_Path')] = NX.util.Url.asRepositoryLink(assetModel.get('repositoryName'),
        assetModel.get('name'));
    info[NX.I18n.get('Assets_Info_ContentType')] = assetModel.get('contentType');
    info[NX.I18n.get('Assets_Info_FileSize')] = Ext.util.Format.fileSize(assetModel.get('size'));
    info[NX.I18n.get('Assets_Info_Last_Updated')] = new Date(assetModel.get('lastUpdated')) ;

    me.showInfo(info);
    
    // update the grid attribute data
    store.data.clear();
    store.removeAll();
    Ext.iterate(me.assetModel.get('attributes'), function(facet, facetValues) {
      Ext.iterate(facetValues, function(key, value) {
        store.add({facet: facet, label: key, value: value});
      })
    });
  }

});
