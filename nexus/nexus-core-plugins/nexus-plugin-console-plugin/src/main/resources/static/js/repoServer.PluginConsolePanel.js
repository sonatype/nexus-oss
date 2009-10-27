Sonatype.repoServer.PluginConsolePanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Plugin Console'
  };
  Ext.apply( this, config, defaultConfig );
    
  Sonatype.repoServer.PluginConsolePanel.superclass.constructor.call( this, {
    url: Sonatype.config.servicePath + '/plugin_console/plugin_infos',
    dataAntoLoad: true,
    tabbedChildren: true,
    dataSortInfo: { field: 'name', direction: 'asc' },
    tbar: [],
    columns: [
    { name: 'name',
      header: 'Name',
      width: 300
    },
    { name: 'version',
      header: 'Version',
      width: 150
    },    
    { name: 'description',
      header: 'Description',
      width: 300
    },
    { name: 'status',
      header: 'Status',
      width: 100
    },
    { name: 'scmVersion' },
    { name: 'failureReason' },
    { name: 'site'}
    ],
    rowClickEvent: 'pluginInfoInit'
  });
};

Ext.extend( Sonatype.repoServer.PluginConsolePanel, Sonatype.panels.GridViewer, {
  doSomething: function(){}
} );

Sonatype.Events.addListener( 'nexusNavigationInit', function( nexusPanel ) {
  var sp = Sonatype.lib.Permissions;
  if ( sp.checkPermission( 'nexus:pluginconsoleplugininfos', sp.READ) ){
    nexusPanel.add( {
      enabled: true,
      sectionId: 'st-nexus-config',
      title: 'Plugin Console',
      tabId: 'plugin_console',
      tabTitle: 'Plugin Console',
      tabCode: Sonatype.repoServer.PluginConsolePanel
    } );
  }
} );