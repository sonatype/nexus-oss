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
        cardPanel.add(new Sonatype.repoServer.UserBrowsePanel({
              payload : rec,
              tabTitle : 'Role Tree',
              parentId : rec.data.userId,
              parentName : rec.data.firstName + ' ' + rec.data.lastName
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