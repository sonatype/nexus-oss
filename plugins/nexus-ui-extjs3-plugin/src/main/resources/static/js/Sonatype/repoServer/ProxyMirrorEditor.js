/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global NX,Sonatype,Ext,Nexus*/
NX.define('Sonatype.repoServer.ProxyMirrorEditor', {
  extend : 'Sonatype.repoServer.AbstractMirrorPanel',
  requirejs : ['Sonatype/all'],

  constructor : function(config) {
    Ext.apply(this, config || {}, {});
    var ht = Sonatype.repoServer.resources.help.repoMirrors,
        self = this;

    this.mirrorRecordConstructor = Ext.data.Record.create([
      {
        name : 'id'
      },
      {
        name : 'url',
        sortType : Ext.data.SortTypes.asUCString
      }
    ]);

    this.mirrorReader = new Ext.data.JsonReader({
      root : 'data',
      id : 'id'
    }, this.mirrorRecordConstructor);

    this.predefinedMirrorDataStore = new Ext.data.Store({
      url : Sonatype.config.repos.urls.repoPredefinedMirrors + '/' + this.payload.data.id,
      reader : this.mirrorReader,
      sortInfo : {
        field : 'url',
        direction : 'ASC'
      }
    });

    Sonatype.repoServer.ProxyMirrorEditor.superclass.constructor.call(this, {
      items : [
        {
          xtype : 'panel',
          style : 'padding-top: 20px',
          layout : 'column',
          items : [
            {
              xtype : 'panel',
              layout : 'form',
              labelWidth : 150,
              width : 430,
              items : [
                {
                  xtype : 'combo',
                  fieldLabel : 'Mirror URL',
                  helpText : ht.mirrorUrl,
                  name : 'mirrorUrl',
                  width : 238,
                  listWidth : 238,
                  store : this.predefinedMirrorDataStore,
                  displayField : 'url',
                  valueField : 'id',
                  editable : true,
                  forceSelection : false,
                  mode : 'local',
                  triggerAction : 'all',
                  emptyText : 'Enter or Select URL...',
                  selectOnFocus : true,
                  allowBlank : true,
                  validator : function(v) {
                    if (v === '' || v.match(self.MIRROR_URL_REGEXP)) {
                      return true;
                    }

                    return 'Protocol must be http:// or https://';
                  }
                }
              ]
            },
            {
              xtype : 'panel',
              width : 120,
              items : [
                {
                  xtype : 'button',
                  text : 'Add',
                  style : 'padding-left: 7px',
                  minWidth : 100,
                  id : 'button-add',
                  handler : this.addNewMirrorUrl,
                  scope : this
                }
              ]
            }
          ]
        },
        {
          xtype : 'panel',
          layout : 'column',
          autoHeight : true,
          style : 'padding-left: 155px',
          items : [
            {
              xtype : 'treepanel',
              name : 'mirror-url-list',
              title : 'Mirror URLs',
              border : true,
              bodyBorder : true,
              bodyStyle : 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
              style : 'padding: 0 20px 0 0',
              width : 275,
              height : 300,
              animate : true,
              lines : false,
              autoScroll : true,
              containerScroll : true,
              rootVisible : false,
              ddScroll : true,
              enableDD : true,
              root : new Ext.tree.TreeNode({
                text : 'root',
                draggable : false
              })
            },
            {
              xtype : 'panel',
              width : 120,
              items : [
                {
                  xtype : 'button',
                  text : 'Remove',
                  style : 'padding-left: 6px',
                  minWidth : 100,
                  id : 'button-remove',
                  handler : this.removeMirrorUrl,
                  scope : this
                },
                {
                  xtype : 'button',
                  text : 'Remove All',
                  style : 'padding-left: 6px; margin-top: 5px',
                  minWidth : 100,
                  id : 'button-remove-all',
                  handler : this.removeAllMirrorUrls,
                  scope : this
                }
              ]
            }
          ]
        }
      ]
    });
  }
}, function() {
  Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
    var sp = Sonatype.lib.Permissions;
    if (rec.data.resourceURI && sp.checkPermission('nexus:repositorymirrors', sp.READ) &&  rec.data.userManaged)
    {
      if (rec.data.repoType === 'proxy')
      {
        cardPanel.add(new Sonatype.repoServer.ProxyMirrorEditor({
          payload : rec,
          tabTitle : 'Mirrors',
          name : 'mirrors'
        }));
      }
    }
  });
});


