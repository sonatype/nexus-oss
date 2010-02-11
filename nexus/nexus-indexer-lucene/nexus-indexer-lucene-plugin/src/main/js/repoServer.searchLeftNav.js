var SEARCH_FIELD_CONFIG = {
  xtype: 'trigger',
  triggerClass: 'x-form-search-trigger',
  listeners: {
    'specialkey': {
      fn: function(f, e){
        if(e.getKey() == e.ENTER){
          this.onTriggerClick();
        }
      }
    }
  },
  onTriggerClick: function(a,b,c){
    var v = this.getRawValue();
    if ( v.length > 0 ) {
      var panel = Sonatype.view.mainTabPanel.addOrShowTab(
          'nexus-search', Sonatype.repoServer.SearchPanel, { title: 'Search' } );
      panel.startQuickSearch( v );
    }
  }
};

Sonatype.Events.addListener( 'nexusNavigationInit', function( nexusPanel ) {
  if ( Sonatype.lib.Permissions.checkPermission( 'nexus:index', Sonatype.lib.Permissions.READ ) ) {
    nexusPanel.add({
      title: 'Artifact Search',
      id: 'st-nexus-search',
      items: [
        Ext.apply( {
          repoPanel: this,
          id: 'quick-search--field',
          width: 140
        }, SEARCH_FIELD_CONFIG ),
        {
          title: 'Advanced Search',
          tabCode: Sonatype.repoServer.SearchPanel,
          tabId: 'nexus-search',
          tabTitle: 'Search'
        }
      ]
    });
  }
});

Sonatype.Events.addListener( 'welcomePanelInit', function( repoServer, welcomePanelConfig ) {
  if ( Sonatype.lib.Permissions.checkPermission( 'nexus:index', Sonatype.lib.Permissions.READ ) ) {
    welcomePanelConfig.items.push( {
      layout: 'form',
      border: false,
      frame: false,
      labelWidth: 10,
      items: [
        {
          border: false,
          html: '<div class="little-padding">' +
            'Type in the name of a project, class, or artifact into the text box ' +
            'below, and click Search. Use "Advanced Search" on the left for more options.' +
            '</div>'
        },
        Ext.apply( {
          repoPanel: repoServer,
          id: 'quick-search-welcome-field',
          anchor: '-10',
          labelSeparator: ''
        }, SEARCH_FIELD_CONFIG )
      ]
    } );
  }
});

Sonatype.Events.addListener( 'welcomeTabRender', function() {
  var c = Ext.getCmp( 'quick-search-welcome-field' );
  if ( c ) {
    c.focus( true, 100 );
  }
});