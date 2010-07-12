// This container will host both the repository browser and the artifact
// information panel
Sonatype.repoServer.RepositoryIndexBrowserContainer = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  var items = [];

  this.repositoryBrowser = new Sonatype.repoServer.IndexBrowserPanel({
        payload : this.payload,
        tabTitle : this.tabTitle,
        region : 'center',
        nodeClickEvent : 'indexNodeClickedEvent',
        nodeClickPassthru : {
          container : this
        }
      });

  this.artifactContainer = new Sonatype.repoServer.ArtifactContainer({
        collapsible : true,
        collapsed : true,
        region : 'east',
        split : true,
        width : 500
      });

  items.push(this.repositoryBrowser);
  items.push(this.artifactContainer);

  Sonatype.repoServer.RepositoryIndexBrowserContainer.superclass.constructor.call(this, {
        layout : 'border',
        // this hideMode causes the tab to properly render when coming back from
        // hidden
        hideMode : 'offsets',
        items : items
      });
};

Ext.extend(Sonatype.repoServer.RepositoryIndexBrowserContainer, Ext.Panel, {
      updatePayload : function(payload) {
        if (payload == null)
        {
          this.collapse();
          this.repositoryBrowser.updatePayload(null);
          this.artifactContainer.collapsePanel();
        }
        else
        {
          this.expand();
          this.repositoryBrowser.updatePayload(payload);
          this.artifactContainer.updateArtifact(payload);
        }
      }
    });

// Add the browse storage and browse index panels to the repo
Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
      if (rec.data.resourceURI && rec.data.repoType != 'virtual' && rec.data.format == 'maven2')
      {
        var panel = new Sonatype.repoServer.RepositoryIndexBrowserContainer({
              payload : rec,
              name : 'browseindex',
              tabTitle : 'Browse Index'
            });

        if (cardPanel.items.getCount() > 0)
        {
          cardPanel.insert(1, panel);
        }
        else
        {
          cardPanel.add(panel);
        }
      }
    });

Sonatype.Events.addListener('indexNodeClickedEvent', function(node, passthru) {
      if (passthru && passthru.container)
      {
        if (node && node.isLeaf())
        {
          Ext.Ajax.request({
                scope : this,
                method : 'GET',
                options : {
                  dontForceLogout : true
                },
                cbPassThru : {
                  node : node,
                  container : passthru.container
                },
                callback : function(options, isSuccess, response) {
                  if (isSuccess)
                  {
                    var json = Ext.decode(response.responseText);

                    var resourceURI = Sonatype.config.servicePath + '/repositories/' + options.cbPassThru.node.attributes.repositoryId + '/archive' + json.data.repositoryPath;

                    options.cbPassThru.container.artifactContainer.updateArtifact({
                          leaf : true,
                          resourceURI : resourceURI,
                          groupId : options.cbPassThru.node.attributes.groupId,
                          artifactId : options.cbPassThru.node.attributes.artifactId,
                          version : options.cbPassThru.node.attributes.version,
                          repoId : options.cbPassThru.node.attributes.repositoryId,
                          classifier : options.cbPassThru.node.attributes.classifier,
                          extension : options.cbPassThru.node.attributes.extension,
                          artifactLink : options.cbPassThru.node.attributes.artifactUri,
                          pomLink : options.cbPassThru.node.attributes.pomUri,
                          nodeName : options.cbPassThru.node.attributes.nodeName
                        });
                  }
                },
                url : Sonatype.config.servicePath + '/artifact/maven/resolve?r=' + node.attributes.repositoryId + '&g=' + node.attributes.groupId + '&a=' + node.attributes.artifactId + '&v=' + node.attributes.version
                    + (Ext.isEmpty(node.attributes.classifier) ? '' : ('&c=' + node.attributes.classifier)) + '&e=' + node.attributes.extension
              });
          // var resourceURI = node.ownerTree.loader.url.substring(0,
          // node.ownerTree.loader.url.length - 'index_content'.length) +
          // 'content' + node.attributes.path;
        }
        else
        {
          passthru.container.artifactContainer.collapse();
          passthru.container.artifactContainer.updateArtifact(null);
        }
      }
    });