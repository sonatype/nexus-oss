/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
Sonatype.repoServer.UserBrowsePanel = function(config) {
  var config = config || {};
  var defaultConfig = {
    titleColumn : 'name',
    isRole : false
  };
  Ext.apply(this, config, defaultConfig);

  this.roleTreeDataStore = new Ext.data.JsonStore({
        root : 'data',
        id : 'id',
        baseParams : {
          isRole : this.isRole
        },
        proxy : new Ext.data.HttpProxy({
              url : Sonatype.config.servicePath + '/role_tree/' + this.parentId,
              method : 'GET'
            }),
        sortInfo : {
          field : 'name',
          direction : 'ASC'
        },
        autoLoad : true,
        fields : [{
              name : 'id'
            }, {
              name : 'type'
            }, {
              name : 'name'
            }, {
              name : 'children'
            }],
        listeners : {
          load : {
            fn : this.roleTreeLoadHandler,
            scope : this
          }
        }
      });

  Sonatype.repoServer.UserBrowsePanel.superclass.constructor.call(this, {
        anchor : '0 -2',
        bodyStyle : 'background-color:#FFFFFF',
        animate : true,
        lines : false,
        autoScroll : true,
        containerScroll : true,
        rootVisible : true,
        enableDD : false,
        root : new Ext.tree.TreeNode({
              text : this.parentName,
              draggable : false
            }),
        tbar : [{
              text : 'Refresh',
              icon : Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
              cls : 'x-btn-text-icon',
              scope : this,
              handler : this.refreshHandler
            }]
      });

  new Ext.tree.TreeSorter(this, {
        folderSort : true
      });
};

Ext.extend(Sonatype.repoServer.UserBrowsePanel, Ext.tree.TreePanel, {
      refreshHandler : function(button, e) {
        this.roleTreeDataStore.reload();
      },
      roleTreeLoadHandler : function(store, records, options) {
        this.idCounter = 0;
        while (this.getRootNode().lastChild)
        {
          this.getRootNode().removeChild(this.getRootNode().lastChild);
        }
        for (var i = 0; i < records.length; i++)
        {
          this.loadItemIntoTree(records[i].data.name, records[i].data.type, this.getRootNode(), records[i].data.children);
        }
      },
      loadItemIntoTree : function(name, type, parentNode, children) {
        var childNode = new Ext.tree.TreeNode({
              id : this.idCounter++,
              text : name,
              allowChildren : (children && children.length > 0) ? true : false,
              draggable : false,
              leaf : (children && children.length > 0) ? false : true,
              icon : (type == 'role') ? (Sonatype.config.extPath + '/resources/images/default/tree/folder.gif') : (Sonatype.config.extPath + '/resources/images/default/tree/leaf.gif')
            });

        parentNode.appendChild(childNode);

        if (children)
        {
          for (var i = 0; i < children.length; i++)
          {
            this.loadItemIntoTree(children[i].name, children[i].type, childNode, children[i].children)
          }
        }
      }
    });

Sonatype.Events.addListener('userViewInit', function(cardPanel, rec, gridPanel) {
      if (rec.data.resourceURI)
      {
        var parentName = null;

        if (rec.data.firstName)
        {
          parentName = rec.data.firstName;
        }

        if (rec.data.lastName)
        {
          parentName += ' ' + rec.data.lastName;
        }

        if (parentName == null)
        {
          parentName = rec.data.userId;
        }
        cardPanel.add(new Sonatype.repoServer.UserBrowsePanel({
              payload : rec,
              tabTitle : 'Role Tree',
              parentId : rec.data.userId,
              parentName : parentName
            }));
      }
    });

Sonatype.Events.addListener('roleViewInit', function(cardPanel, rec, gridPanel) {
      if (rec.data.resourceURI)
      {
        cardPanel.add(new Sonatype.repoServer.UserBrowsePanel({
              payload : rec,
              tabTitle : 'Role Tree',
              parentId : rec.data.id,
              isRole : true,
              parentName : rec.data.name
            }));
      }
    });