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
Ext.namespace('Sonatype.panels');

Sonatype.panels.TreePanel = function(config) {
  var config = config || {};
  var defaultConfig = {
    titleColumn : 'name',
    nodeIconClass : null,
    useNodeIconClassParam : null,
    nodeClass : null,
    useNodeClassParam : null,
    nodePathPrepend : '',
    appendPathToRoot : true,
    leafClickEvent : null,
    resetRootNodeText : true,
    autoExpandRoot : true,
    appendAttributeToId : null
  };
  Ext.apply(this, config, defaultConfig);

  this.tbar = [{
    text : 'Refresh',
    icon : Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
    cls : 'x-btn-text-icon',
    scope : this,
    handler : this.refreshHandler
  }];

  if (this.toolbarInitEvent)
  {
    Sonatype.Events.fireEvent(this.toolbarInitEvent, this, this.tbar);
  }

  Sonatype.panels.TreePanel.superclass.constructor.call(this, {
    anchor : '0 -2',
    bodyStyle : 'background-color:#FFFFFF',
    animate : true,
    lines : false,
    autoScroll : true,
    containerScroll : true,
    rootVisible : true,
    enableDD : false,
    loader : new Ext.tree.TreeLoader({
      nodePathPrepend : this.nodePathPrepend,
      appendPathToRoot : this.appendPathToRoot,
      nodeIconClass : this.nodeIconClass,
      useNodeIconClassParam : this.useNodeIconClassParam,
      nodeClass : this.nodeClass,
      useNodeClassParam : this.useNodeClassParam,
      appendAttributeToId : this.appendAttributeToId,
      requestMethod : 'GET',
      url : this.url,
      listeners : {
        loadexception : this.treeLoadExceptionHandler,
        scope : this
      },
      requestData : function(node, callback) {
        if (this.fireEvent("beforeload", this, node, callback) !== false)
        {
          this.transId = Ext.Ajax.request({
            method : this.requestMethod,
            // Sonatype: nodes contain a relative request path
            url : this.url + ((this.appendPathToRoot || node.attributes.path != '/') ? (this.nodePathPrepend + node.attributes.path) : ''),
            success : this.handleResponse,
            params : this.baseParams,
            failure : this.handleFailure,
            scope : this,
            argument : {
              callback : callback,
              node : node
            }
          });
        }
        else
        {
          if (typeof callback == "function")
          {
            callback();
          }
        }
      },
      createNode : function(attr) {
        if (this.baseAttrs)
        {
          Ext.applyIf(attr, this.baseAttrs);
        }
        if (this.applyLoader !== false)
        {
          attr.loader = this;
        }
        if (typeof attr.uiProvider == 'string')
        {
          attr.uiProvider = this.uiProviders[attr.uiProvider] || eval(attr.uiProvider);
        }

        // Sonatype: node name is supplied as 'nodeName' instead of
        // 'text'
        if (!attr.text && attr.nodeName)
        {
          attr.text = attr.nodeName;
        }
        if (!attr.id)
        {
          attr.id = (this.url + attr.path).replace(/\//g, '_');
          if ( this.appendAttributeToId )
          {
            attr.id += attr[this.appendAttributeToId];
          }
        }

        if (!attr.singleClickExpand)
        {
          attr.singleClickExpand = true;
        }

        if (this.nodeIconClass != null)
        {
          if (this.useNodeIconClassParam == null || attr[this.useNodeIconClassParam])
          {
            attr.iconCls = this.nodeIconClass;
          }
        }

        if (this.nodeClass != null)
        {
          if (this.useNodeClassParam == null || attr[this.useNodeClassParam])
          {
            attr.cls = this.nodeClass;
          }
        }

        attr.rootUrl = this.url;

        if (attr.nodeType)
        {
          return new Ext.tree.TreePanel.nodeTypes[attr.nodeType](attr);
        }
        else
        {
          return attr.leaf ? new Ext.tree.TreeNode(attr) : new Ext.tree.AsyncTreeNode(attr);
        }
      },
      processResponse : function(response, node, callback) {
        var json = response.responseText;
        try
        {
          var o = eval("(" + json + ")");
          if (o.data)
          {
            o = o.data;
            node.beginUpdate();

            // Sonatype:
            // - tree response contains the current node, not just an
            // array of children
            // - node name is supplied as 'nodeName' instead of 'text'
            if (!node.isRoot)
            {
              node.setText(o.nodeName);
              Ext.apply(node.attributes, o, {});
            }
            for (var i = 0, len = o.children.length; i < len; i++)
            {
              var n = this.createNode(o.children[i]);
              if (n)
              {
                node.appendChild(n);
              }
            }

            node.endUpdate();
          }

          if (typeof callback == "function")
          {
            callback(this, node);
          }
        }
        catch (e)
        {
          this.handleFailure(response);
        }
      }
    }),
    listeners : {
      click : {
        fn : this.nodeClickHandler,
        scope : this
      },
      contextMenu : {
        fn : this.nodeContextMenuHandler,
        scope : this
      }
    }
  });

  new Ext.tree.TreeSorter(this, {
    folderSort : true
  });

  if (!this.getRootNode())
  {

    var root = new Ext.tree.AsyncTreeNode({
      text : this.payload ? this.payload.get(this.titleColumn) : '/',
      path : '/',
      singleClickExpand : true,
      expanded : this.autoExpandRoot
    });

    this.setRootNode(root);
  }
};

Ext.extend(Sonatype.panels.TreePanel, Ext.tree.TreePanel, {
  nodeClickHandler : function(node, e) {
    if (e.target.nodeName == 'A')
      return; // no menu on links

    if (this.nodeClickEvent)
    {
      Sonatype.Events.fireEvent(this.nodeClickEvent, node, this.nodeClickPassthru);
    }
    else if (this.leafClickEvent)
    {
      Sonatype.Events.fireEvent(this.leafClickEvent, node, this.leafClickPassthru);
    }
  },

  nodeContextMenuHandler : function(node, e) {
    if (e.target.nodeName == 'A')
      return; // no menu on links

    if (this.nodeContextMenuEvent)
    {

      var menu = new Sonatype.menu.Menu({
        id : 'tree-context-menu',
        payload : node,
        scope : this,
        items : []
      });

      Sonatype.Events.fireEvent(this.nodeContextMenuEvent, menu, node);
      if (!menu.items.first())
        return;

      e.stopEvent();
      menu.showAt(e.getXY());
    }
  },

  refreshHandler : function(button, e) {
    if (this.resetRootNodeText)
    {
      this.root.setText(this.payload ? this.payload.get(this.titleColumn) : '/');
    }
    this.root.attributes.localStorageUpdated = false;
    if (this.root.reload)
    {
      this.root.reload();
    }
  },

  treeLoadExceptionHandler : function(treeLoader, node, response) {
    if (response.status == 503)
    {
      if (Sonatype.MessageBox.isVisible())
      {
        Sonatype.MessageBox.hide();
      }
      node.setText(node.text + ' (Out of Service)');
    }
    else if (response.status == 404)
    {
      if (Sonatype.MessageBox.isVisible())
      {
        Sonatype.MessageBox.hide();
      }
      node.setText(node.text + (node.isRoot ? ' (Not Available)' : ' (Not Found)'));
    }
    else if (response.status == 401)
    {
      if (Sonatype.MessageBox.isVisible())
      {
        Sonatype.MessageBox.hide();
      }
      node.setText(node.text + ' (Access Denied)');
    }
  }
});
