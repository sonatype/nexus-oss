Sonatype.repoServer.IndexBrowserPanel = function(config) {
  var config = config || {};
  var defaultConfig = {
    url : '',
    root : new Ext.tree.TreeNode({
          text : '(Not Available)',
          id : '/',
          singleClickExpand : true,
          expanded : true
        }),
    showRepositoryDropDown : false
  };
  Ext.apply(this, config, defaultConfig);

  if (this.showRepositoryDropDown)
  {
    this.toolbarInitEvent = 'indexBrowserToolbarInit';
  }

  Sonatype.repoServer.IndexBrowserPanel.superclass.constructor.call(this, {
        nodeIconClass : 'x-tree-node-nexus-icon',
        useNodeIconClassParam : 'locallyAvailable'
      });
};

Sonatype.Events.addListener('indexBrowserToolbarInit', function(treepanel, toolbar) {
      if (treepanel.showRepositoryDropDown)
      {
        var store = new Ext.data.SimpleStore({
              fields : ['id', 'name']
            });
        treepanel.repocombo = new Ext.form.ComboBox({
              width : 200,
              store : store,
              valueField : 'id',
              displayField : 'name',
              editable : false,
              mode : 'local',
              triggerAction : 'all',
              listeners : {
                select : {
                  fn : function(combo, record, index) {
                    for (var i = 0; i < treepanel.payload.data.hits.length; i++)
                    {
                      if (record.data.id == treepanel.payload.data.hits[i].repositoryId)
                      {
                        treepanel.updatePayload({
                              data : {
                                id : treepanel.payload.data.hits[i].repositoryId,
                                name : treepanel.payload.data.hits[i].repositoryName,
                                resourceURI : treepanel.payload.data.hits[i].repositoryURL,
                                format : treepanel.payload.data.hits[i].repositoryContentClass,
                                repoType : treepanel.payload.data.hits[i].repositoryKind,
                                expandPath : treepanel.payload.data.expandPath,
                                showCtx : treepanel.payload.data.showCtx,
                                useHints : treepanel.payload.data.useHints,
                                hits : treepanel.payload.data.hits,
                                rec : treepanel.payload.data.rec,
                                getDefaultPath : treepanel.payload.data.getDefaultPath,
                                hitIndex : treepanel.payload.data.hitIndex
                              }
                            }, true);
                      }
                    }
                  },
                  scope : treepanel
                }
              }
            });

        toolbar.push(' ', '-', ' ', 'Viewing Repository:', ' ', treepanel.repocombo);
      }
    });

Ext.extend(Sonatype.repoServer.IndexBrowserPanel, Sonatype.panels.TreePanel, {
      nodeExpandHandler : function() {
        var parentContainer = this.parentContainer;

        if (this.payload.data.expandPath)
        {
          this.selectPath(this.getDefaultPathFromPayload(), 'text', function(success, node) {
                if (success)
                {
                  if (node.ownerTree.nodeClickEvent)
                  {
                    Sonatype.Events.fireEvent(node.ownerTree.nodeClickEvent, node, node.ownerTree.nodeClickPassthru);
                  }
                }
                else if (parentContainer != null)
                {
                  parentContainer.loadComplete();
                }
              });
        }
      },
      getDefaultPathFromPayload : function() {
        var rec = this.payload.data.rec;
        var hitIndex = this.payload.data.hitIndex;

        var basePath = '/' + rec.data.artifactHits[hitIndex].repositoryName + '/' + rec.data.groupId.replace(/\./g, '/') + '/' + rec.data.artifactId + '/' + rec.data.version + '/' + rec.data.artifactId + '-' + rec.data.version;

        for (var i = 0; i < rec.data.artifactHits[hitIndex].artifactLinks.length; i++)
        {
          var link = rec.data.artifactHits[hitIndex].artifactLinks[i];

          if (Ext.isEmpty(link.classifier))
          {
            if (link.extension != 'pom')
            {
              return basePath + '.' + link.extension;
            }
          }
        }

        var link = rec.data.artifactHits[hitIndex].artifactLinks[0];
        return basePath + (link.classifier ? ('-' + link.classifier) : '') + '.' + link.extension;
      },
      refreshHandler : function(button, e) {
        Sonatype.Events.fireEvent(this.nodeClickEvent, null, this.nodeClickPassthru);
        if (this.root)
        {
          this.root.destroy();
        }
        if (this.payload)
        {
          this.loader.url = this.payload.data.resourceURI + '/index_content';
          if (this.payload.data.useHints)
          {
            this.loader.baseParams = {
              groupIdHint : this.payload.data.rec.data.groupId,
              artifactIdHint : this.payload.data.rec.data.artifactId
            }
          }
          else
          {
            this.loader.baseParams = null;
          }

          this.setRootNode(new Ext.tree.AsyncTreeNode({
                text : this.payload.data[this.titleColumn],
                path : '/',
                singleClickExpand : true,
                expanded : true,
                listeners : {
                  expand : {
                    fn : this.nodeExpandHandler,
                    scope : this
                  }
                }
              }));
        }
        else
        {
          this.setRootNode(new Ext.tree.TreeNode({
                text : '(Not Available)',
                id : '/',
                singleClickExpand : true,
                expanded : true
              }));
        }

        if (this.innerCt)
        {
          this.innerCt.update('');
          this.afterRender();
        }
      },

      updatePayload : function(payload, onlyPayload) {
        this.oldPayload = this.payload;
        this.payload = payload;

        if (!onlyPayload && this.repocombo)
        {
          var store = this.repocombo.store;
          store.removeAll();
          if (this.payload)
          {
            for (var i = 0; i < this.payload.data.hits.length; i++)
            {
              var record = new Ext.data.Record.create({
                    name : 'id'
                  }, {
                    name : 'name'
                  });

              store.add(new record({
                    id : this.payload.data.hits[i].repositoryId,
                    name : this.payload.data.hits[i].repositoryName
                  }));
            }

            this.repocombo.setValue(this.payload.data.id);
          }
        }

        this.refreshHandler();
      }
    });