/*
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

Sonatype.repoServer.MirrorConfigPanel = function(config) {
  var config = config || {};

  this.mirrorStatusTask = {
    run : function() {
      Ext.Ajax.request( {
        url :Sonatype.config.repos.urls.repoMirrorStatus + '/' + this.payload.data.id,
        callback :this.statusCallback,
        scope :this
      });
    },
    interval :5000, // poll every 5 seconds
    scope :this
  };

  this.mirrorRecordConstructor = Ext.data.Record.create( [
      {
        name :'id'
      }, {
        name :'url',
        sortType :Ext.data.SortTypes.asUCString
      }
  ]);

  this.mirrorReader = new Ext.data.JsonReader( {
    root :'data',
    id :'id'
  }, this.mirrorRecordConstructor);

  this.mirrorDataStore = new Ext.data.Store( {
    url :Sonatype.config.repos.urls.repoMirrors + '/' + config.payload.data.id,
    reader :this.mirrorReader,
    sortInfo : {
      field :'url',
      direction :'ASC'
    },
    autoLoad :false
  });

  this.predefinedMirrorDataStore = new Ext.data.Store( {
    url :Sonatype.config.repos.urls.repoPredefinedMirrors + '/' + config.payload.data.id,
    reader :this.mirrorReader,
    sortInfo : {
      field :'url',
      direction :'ASC'
    },
    autoLoad :false
  });

  var defaultConfig = {
    uri :Sonatype.config.repos.urls.repoMirrors + '/' + config.payload.data.id,
    referenceData :Sonatype.repoServer.referenceData.repoMirrors,
    dataStores : [
        this.mirrorDataStore, this.predefinedMirrorDataStore
    ],
    dataModifiers : {
      load : {
        'rootData' :this.loadMirrors.createDelegate(this)
      },
      submit : {
        'rootData' :this.saveMirrors.createDelegate(this)
      }
    },
    listeners : {
      submit : {
        fn :this.submitHandler,
        scope :this
      },
      activate : {
        fn :this.activateHandler,
        scope :this
      },
      deactivate : {
        fn :this.deactivateHandler,
        scope :this
      },
      destroy : {
        fn :this.destroyHandler,
        scope :this
      }
    }
  };

  Ext.apply(this, config, defaultConfig);

  var ht = Sonatype.repoServer.resources.help.repoMirrors;

  Sonatype.repoServer.MirrorConfigPanel.superclass.constructor.call(this, {
    items : [
        {
          xtype :'panel',
          style :'padding-top: 20px',
          layout :'column',
          items : [
              {
                xtype :'panel',
                layout :'form',
                labelWidth :150,
                width :430,
                items : [
                  {
                    xtype :'combo',
                    fieldLabel :'Mirror URL',
                    helpText :ht.mirrorUrl,
                    name :'mirrorUrl',
                    width :238,
                    listWidth :238,
                    store :this.predefinedMirrorDataStore,
                    displayField :'url',
                    valueField :'id',
                    editable :true,
                    forceSelection :false,
                    mode :'local',
                    triggerAction :'all',
                    emptyText :'Enter or Select URL...',
                    selectOnFocus :true,
                    allowBlank :true
                  }
                ]
              }, {
                xtype :'panel',
                width :120,
                items : [
                  {
                    xtype :'button',
                    text :'Add',
                    style :'padding-left: 7px',
                    minWidth :100,
                    id :'button-add',
                    handler :this.addNewMirrorUrl,
                    scope :this
                  }
                ]
              }
          ]
        }, {
          xtype :'panel',
          layout :'column',
          autoHeight :true,
          style :'padding-left: 155px',
          items : [
              {
                xtype :'treepanel',
                name :'mirror-url-list',
                title :'Mirror URLs',
                border :true,
                bodyBorder :true,
                bodyStyle :'background-color:#FFFFFF; border: 1px solid #B5B8C8',
                style :'padding: 0 20px 0 0',
                width :275,
                height :300,
                animate :true,
                lines :false,
                autoScroll :true,
                containerScroll :true,
                rootVisible :false,
                enableDD :false,
                root :new Ext.tree.TreeNode( {
                  text :'root'
                })
              }, {
                xtype :'panel',
                width :120,
                items : [
                    {
                      xtype :'button',
                      text :'Remove',
                      style :'padding-left: 6px',
                      minWidth :100,
                      id :'button-remove',
                      handler :this.removeMirrorUrl,
                      scope :this
                    }, {
                      xtype :'button',
                      text :'Remove All',
                      style :'padding-left: 6px; margin-top: 5px',
                      minWidth :100,
                      id :'button-remove-all',
                      handler :this.removeAllMirrorUrls,
                      scope :this
                    }
                ]
              }
          ]
        }
    ]
  });
};

Ext.extend(Sonatype.repoServer.MirrorConfigPanel, Sonatype.ext.FormPanel, {
  addNewMirrorUrl : function() {
    var treePanel = this.find('name', 'mirror-url-list')[0];
    var urlField = this.find('name', 'mirrorUrl')[0];
    var url = urlField.getRawValue();

    if (url) {
      var nodes = treePanel.root.childNodes;
      for ( var i = 0; i < nodes.length; i++) {
        if (url == nodes[i].attributes.payload.url) {
          urlField.markInvalid('This URL already exists');
          return;
        }
      }

      urlField.clearInvalid();

      this.addUrlNode(treePanel, url, url);
      urlField.setRawValue('');
      urlField.setValue('');
    }
  },

  addUrlNode : function(treePanel, url, id) {
    var validId;
    var manualUrl;
    if (url == id) {
      validId = Ext.id();
      manualUrl = true;
    } else {
      validId = id;
      manualUrl = false;
    }
    treePanel.root.appendChild(new Ext.tree.TreeNode( {
      id :id,
      text :url,
      payload : {
        id :manualUrl ? '' : id,
        url :url
      },
      allowChildren :false,
      draggable :false,
      leaf :true,
      icon :Sonatype.config.extPath + '/resources/images/default/tree/leaf.gif'
    }));
  },

  removeMirrorUrl : function() {
    var treePanel = this.find('name', 'mirror-url-list')[0];

    var selectedNode = treePanel.getSelectionModel().getSelectedNode();
    if (selectedNode) {
      treePanel.root.removeChild(selectedNode);
    }
  },

  removeAllMirrorUrls : function() {
    var treePanel = this.find('name', 'mirror-url-list')[0];
    var treeRoot = treePanel.root;

    while (treeRoot.lastChild) {
      treeRoot.removeChild(treeRoot.lastChild);
    }
  },

  loadMirrors : function(arr, srcObj, fpanel) {
    var treePanel = this.find('name', 'mirror-url-list')[0];

    this.removeAllMirrorUrls();

    for ( var i = 0; i < arr.length; i++) {
      this.addUrlNode(treePanel, arr[i].url, arr[i].id);
    }

    return arr;
  },

  saveMirrors : function(val, fpanel) {
    var treePanel = this.find('name', 'mirror-url-list')[0];

    var outputArr = [];
    var nodes = treePanel.root.childNodes;

    for ( var i = 0; i < nodes.length; i++) {
      outputArr[i] = nodes[i].attributes.payload;
    }

    return outputArr;
  },

  getActionURL : function() {
    return this.uri;
  },

  getSaveMethod : function() {
    return 'POST';
  },

  statusCallback : function(options, success, response) {
    if (success) {
      var statusResp = Ext.decode(response.responseText);
      if (statusResp.data) {
        var data = statusResp.data;
        if (data && data.length) {
          for ( var i = 0; i < data.length; i++) {
            var item = data[i];
            var treePanel = this.find('name', 'mirror-url-list')[0];
            var childNodes = treePanel.getRootNode().childNodes;
            if (childNodes && childNodes.length) {
              for ( var j = 0; j < childNodes.length; j++) {
                if (item.id == childNodes[j].id) {
                  var newNode = new Ext.tree.TreeNode( {
                    id :item.id,
                    text :item.url,
                    payload : {
                      id :item.id,
                      url :item.url
                    },
                    allowChildren :false,
                    draggable :false,
                    leaf :true,
                    icon :item.status == 'Blacklisted' ? (Sonatype.config.extPath + '/resources/images/default/tree/drop-no.gif')
                        : (Sonatype.config.extPath + '/resources/images/default/tree/drop-yes.gif')
                  });

                  treePanel.getRootNode().replaceChild(newNode, childNodes[j]);
                }
              }
            }
          }
        }
      }
    } else {
      Ext.TaskMgr.stop(this.mirrorStatusTask);
      Sonatype.MessageBox.alert('Status retrieval failed');
    }
  },

  submitHandler : function(form, action, receivedData) {
    this.loadMirrors(receivedData, null, this);
  },

  activateHandler : function( panel ) {
    Ext.TaskMgr.start(this.mirrorStatusTask);
  },
  
  deactivateHandler : function( panel ) {
    Ext.TaskMgr.stop(this.mirrorStatusTask);
  },
  
  destroyHandler : function( component ) {
    Ext.TaskMgr.stop(this.mirrorStatusTask);
  }
});

Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
  var sp = Sonatype.lib.Permissions;
  if (rec.data.resourceURI && sp.checkPermission('nexus:repositorymirrors', sp.READ) && rec.data.repoType == 'proxy') {
    cardPanel.add(new Sonatype.repoServer.MirrorConfigPanel( {
      payload :rec,
      tabTitle :'Mirrors'
    }));
  }
});
