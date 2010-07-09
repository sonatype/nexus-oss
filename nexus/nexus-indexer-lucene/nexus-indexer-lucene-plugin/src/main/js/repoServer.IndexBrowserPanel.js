Sonatype.repoServer.IndexBrowserPanel = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  Sonatype.repoServer.IndexBrowserPanel.superclass.constructor.call(this, {
        nodeIconClass : 'x-tree-node-nexus-icon',
        useNodeIconClassParam : 'locallyAvailable',
        url : '',
        root : new Ext.tree.TreeNode({
              text : '(Not Available)',
              id : '/',
              singleClickExpand : true,
              expanded : true
            })
      });
};

Ext.extend(Sonatype.repoServer.IndexBrowserPanel, Sonatype.panels.TreePanel, {
      refreshHandler : function(button, e) {
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
      },

      updatePayload : function(payload) {
        this.payload = payload;
        this.refreshHandler();
      }
    });