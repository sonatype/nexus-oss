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
      width: 100,
      renderer: function( value ){
        if ( Ext.isEmpty(value) ){
          return value;
        }
        return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
      }
    },
    { name: 'scmVersion' },
    { name: 'scmTimestamp' },
    { name: 'failureReason' },
    { name: 'site'},
    { name: 'restInfos'}
    ],
    rowClickEvent: 'pluginInfoInit'
  });
};

Ext.extend( Sonatype.repoServer.PluginConsolePanel, Sonatype.panels.GridViewer, {
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