Sonatype.repoServer.IndexBrowserPanel = function( config ) {
  var config = config || {};
  var defaultConfig = { };
  Ext.apply( this, config, defaultConfig );

  Sonatype.repoServer.IndexBrowserPanel.superclass.constructor.call( this, {
    nodeIconClass: 'x-tree-node-nexus-icon',
    useNodeIconClassParam: 'locallyAvailable',
    autoExpandRoot: false,
    url: this.payload.data.resourceURI + '/index_content'
  } );
};

Ext.extend( Sonatype.repoServer.IndexBrowserPanel, Sonatype.panels.TreePanel, {
} );