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
NX.define('Sonatype.repoServer.AbstractMirrorPanel', {
  extend : 'Nexus.ext.FormPanel',

  require : ['Sonatype.repoServer.referenceData'],

  requirejs : ['Nexus/config'],

  MIRROR_URL_REGEXP : /^(?:http|https):\/\//i,

  constructor : function(config) {
    this.mirrorStatusTask = {
      run : function() {
        Ext.Ajax.request({
          url : Sonatype.config.repos.urls.repoMirrorStatus + '/' + this.payload.data.id,
          callback : this.statusCallback,
          scope : this
        });
      },
      interval : 5000, // poll every 5 seconds
      scope : this
    };

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

    this.mirrorDataStore = new Ext.data.Store({
      url : Sonatype.config.repos.urls.repoMirrors + '/' + this.payload.data.id,
      reader : this.mirrorReader,
      sortInfo : {
        field : 'url',
        direction : 'ASC'
      },
      autoLoad : false
    });

    var defaultConfig = {
      uri : Sonatype.config.repos.urls.repoMirrors + '/' + this.payload.data.id,
      referenceData : Sonatype.repoServer.referenceData.repoMirrors,
      dataStores : [this.mirrorDataStore],
      dataModifiers : {
        load : {
          'rootData' : this.loadMirrors.createDelegate(this)
        },
        submit : {
          'rootData' : this.saveMirrors.createDelegate(this)
        }
      },
      listeners : {
        submit : {
          fn : this.submitHandler,
          scope : this
        },
        activate : {
          fn : this.activateHandler,
          scope : this
        },
        deactivate : {
          fn : this.deactivateHandler,
          scope : this
        },
        destroy : {
          fn : this.destroyHandler,
          scope : this
        }
      }
    };

    Ext.apply(this, config || {}, defaultConfig);

    Sonatype.repoServer.AbstractMirrorPanel.superclass.constructor.call(this, {});
  },

  addNewMirrorUrl : function() {
    var
          i,
          treePanel = this.find('name', 'mirror-url-list')[0],
          nodes = treePanel.root.childNodes,
          urlField = this.find('name', 'mirrorUrl')[0],
          url = urlField.getRawValue();

    if (urlField.isValid() && url) {
      for (i = 0; i < nodes.length; i += 1) {
        if (url === nodes[i].attributes.payload.url) {
          urlField.markInvalid('This URL already exists');
          return;
        }
      }

      urlField.clearInvalid();

      this.addUrlNode(treePanel, url, url, Sonatype.config.extPath + '/resources/images/default/tree/leaf.gif');
      urlField.setRawValue('');
      urlField.setValue('');
    }
  },

  addUrlNode : function(treePanel, url, id, icon) {
    var validId, manualUrl;
    if (url === id) {
      validId = Ext.id();
      manualUrl = true;
    }
    else {
      validId = id;
      manualUrl = false;
    }
    treePanel.root.appendChild(new Ext.tree.TreeNode({
      id : id,
      text : url,
      href : url,
      hrefTarget : '_new',
      payload : {
        id : manualUrl ? '' : id,
        url : url
      },
      allowChildren : false,
      draggable : true,
      leaf : true,
      icon : icon
    }));
  },

  removeMirrorUrl : function() {
    var
          treePanel = this.find('name', 'mirror-url-list')[0],
          selectedNode = treePanel.getSelectionModel().getSelectedNode();

    if (selectedNode) {
      treePanel.root.removeChild(selectedNode);
    }
  },

  removeAllMirrorUrls : function() {
    var
          treePanel = this.find('name', 'mirror-url-list')[0],
          treeRoot = treePanel.root;

    while (treeRoot.lastChild) {
      treeRoot.removeChild(treeRoot.lastChild);
    }
  },

  loadMirrors : function(arr, srcObj, fpanel) {
    var
          i, j, childNodes, found,
          treePanel = this.find('name', 'mirror-url-list')[0],
          mirrorArray = [];

    for (i = 0; i < arr.length; i += 1) {
      childNodes = treePanel.getRootNode().childNodes;
      found = false;
      if (childNodes && childNodes.length) {
        for (j = 0; j < childNodes.length; j += 1) {
          if (arr[i].id === childNodes[j].id) {
            mirrorArray[i] = {
              id : arr[i].id,
              url : arr[i].url,
              icon : childNodes[j].ui.iconNode.src
            };
            found = true;
            break;
          }
        }
      }
      if (!found) {
        mirrorArray[i] = {
          id : arr[i].id,
          url : arr[i].url,
          icon : Sonatype.config.extPath + '/resources/images/default/tree/leaf.gif'
        };
      }
    }

    this.removeAllMirrorUrls();

    for (i = 0; i < arr.length; i += 1) {
      this.addUrlNode(treePanel, mirrorArray[i].url, mirrorArray[i].id, mirrorArray[i].icon);
    }

    return arr;
  },

  saveMirrors : function(val, fpanel) {
    var
          i,
          treePanel = this.find('name', 'mirror-url-list')[0],
          outputArr = [],
          nodes = treePanel.root.childNodes;

    for (i = 0; i < nodes.length; i += 1) {
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
      var
            i, j, data, item,
            statusResp = Ext.decode(response.responseText),
            treePanel = this.find('name', 'mirror-url-list')[0],
            childNodes = treePanel.getRootNode().childNodes;

      if (statusResp.data) {
        data = statusResp.data;
        if (data && data.length) {
          for (i = 0; i < data.length; i += 1) {
            item = data[i];
            if (childNodes && childNodes.length) {
              for (j = 0; j < childNodes.length; j += 1) {
                if (item.id === childNodes[j].id) {
                  childNodes[j].getUI().getIconEl().src = item.status === 'Blacklisted' ? (Sonatype.config.extPath
                        + '/resources/images/default/tree/drop-no.gif') : (Sonatype.config.extPath
                        + '/resources/images/default/tree/drop-yes.gif');
                  break;
                }
              }
            }
          }
        }
      }
    }
    else {
      // don't add icons, no need to bother the user with this
      Ext.TaskMgr.stop(this.mirrorStatusTask);
    }
  },

  submitHandler : function(form, action, receivedData) {
    this.loadMirrors(receivedData, null, this);
  },

  activateHandler : function(panel) {
    // HACK: this.predefinedMirrorDataStore is only defined in ProxyMirrorEditor class, only attempt to call load() if the field exists
    // HACK: Fix for NEXUS-5060, probably better way to fix however.
    if (this.predefinedMirrorDataStore) {
      this.predefinedMirrorDataStore.load();
    }

    if (panel.payload.data.repoType === 'proxy') {
      Ext.TaskMgr.start(this.mirrorStatusTask);
    }
  },

  deactivateHandler : function(panel) {
    if (panel.payload.data.repoType === 'proxy') {
      Ext.TaskMgr.stop(this.mirrorStatusTask);
    }
  },

  destroyHandler : function(component) {
    if (component.payload.data.repoType === 'proxy') {
      Ext.TaskMgr.stop(this.mirrorStatusTask);
    }
  }
});
