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
                                expandPath : treepanel.payload.data.getDefaultPath(treepanel.payload.data.rec, i),
                                showCtx : true,
                                hits : treepanel.payload.data.hits,
                                rec : treepanel.payload.data.rec,
                                getDefaultPath : treepanel.payload.data.getDefaultPath
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
      refreshHandler : function(button, e) {
        var loadMask = null;
        if (this.parentContainer && this.parentContainer.parentContainer && this.parentContainer.parentContainer.loadMask)
        {
          loadMask = this.parentContainer.parentContainer.loadMask;
        }
        Sonatype.Events.fireEvent(this.nodeClickEvent, null, this.nodeClickPassthru);
        // if we are dealing w/ refresh call (but not from refresh button) with
        // same repo, simply relocate to new path
        if (button == undefined && this.oldPayload && this.payload && this.oldPayload.data.resourceURI == this.payload.data.resourceURI)
        {
          if (this.payload.data.expandPath)
          {
            this.selectPath(this.payload.data.expandPath, 'text', function(success, node) {
                  if (success)
                  {
                    if (node.ownerTree.nodeClickEvent)
                    {
                      Sonatype.Events.fireEvent(node.ownerTree.nodeClickEvent, node, node.ownerTree.nodeClickPassthru);
                    }
                  }
                  else if (loadMask != null)
                  {
                    loadMask.hide();
                  }
                });
          }
        }
        else
        {
          if (this.root)
          {
            this.root.destroy();
          }
          if (this.payload)
          {
            this.loader.url = this.payload.data.resourceURI + '/index_content';

            this.setRootNode(new Ext.tree.AsyncTreeNode({
                  text : this.payload.data[this.titleColumn],
                  path : '/',
                  singleClickExpand : true,
                  expanded : true,
                  listeners : {
                    expand : {
                      fn : function() {
                        if (this.payload.data.expandPath)
                        {
                          this.selectPath(this.payload.data.expandPath, 'text', function(success, node) {
                                if (success)
                                {
                                  if (node.ownerTree.nodeClickEvent)
                                  {
                                    Sonatype.Events.fireEvent(node.ownerTree.nodeClickEvent, node, node.ownerTree.nodeClickPassthru);
                                  }
                                }
                                else if (loadMask != null)
                                {
                                  loadMask.hide();
                                }
                              });
                        }
                      },
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