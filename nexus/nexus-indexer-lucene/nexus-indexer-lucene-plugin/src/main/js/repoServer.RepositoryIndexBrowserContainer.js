// This container will host both the repository browser and the artifact information panel
Sonatype.repoServer.RepositoryIndexBrowserContainer = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );
  
  var items = [];
  
  this.repositoryBrowser = new Sonatype.repoServer.IndexBrowserPanel({
    payload: this.payload,
    tabTitle: this.tabTitle,
    region: 'center',
    nodeClickEvent: 'indexNodeClickedEvent',
    nodeClickPassthru: {
      container: this
    }
  });
  
  this.artifactContainer = new Sonatype.repoServer.ArtifactContainer({
    collapsible: true,
    collapsed: true,
    region: 'east',
    split: true,
    width: 500,
    halfSize: true
  });
  
  items.push(this.repositoryBrowser);
  items.push(this.artifactContainer);
  
  Sonatype.repoServer.RepositoryIndexBrowserContainer.superclass.constructor.call( this, {
    layout: 'border',
    //this hideMode causes the tab to properly render when coming back from hidden
    hideMode: 'offsets',
    items: items
  });
};

Ext.extend( Sonatype.repoServer.RepositoryIndexBrowserContainer, Ext.Panel, {
  
});

// Add the browse storage and browse index panels to the repo
Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  if ( rec.data.resourceURI 
      && rec.data.repoType != 'virtual' 
      && rec.data.format == 'maven2' ) {
    var panel = new Sonatype.repoServer.RepositoryIndexBrowserContainer( { 
      payload: rec,
      name: 'browseindex',
      tabTitle: 'Browse Index'
    } );
    
    if ( cardPanel.items.getCount() > 0 ) {
      cardPanel.insert( 1, panel );
    }
    else {
      cardPanel.add( panel );
    }
  }
} );

Sonatype.Events.addListener( 'indexNodeClickedEvent', function( node, passthru ) {
  if ( passthru 
      && passthru.container ) {
    if ( node 
        && node.isLeaf() ) {
      passthru.container.artifactContainer.updateArtifact({
        groupId: node.attributes.groupId,
        artifactId: node.attributes.artifactId,
        version: node.attributes.version,
        repoId: node.attributes.repositoryId,
        classifier: node.attributes.classifier,
        extension: node.attributes.extension,
        artifactLink: node.attributes.artifactUri,
        pomLink: node.attributes.pomUri,
        nodeName: node.attributes.nodeName
      });
    }
    else {
      passthru.container.artifactContainer.collapse();
    }
  }
});