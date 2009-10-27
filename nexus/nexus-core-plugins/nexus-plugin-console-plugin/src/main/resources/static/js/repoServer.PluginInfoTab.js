Sonatype.repoServer.PluginInfoTab = function( config ) {
  var config = config || {};
  var defaultConfig = {
    readOnly: true
  };
  Ext.apply( this, config, defaultConfig );
  
  Sonatype.repoServer.PluginInfoTab.superclass.constructor.call( this, {
    labelWidth: 120,
    items: [
    { xtype: 'textfield',
      fieldLabel: 'Name',
      name: 'name',
      readOnly: true,
      width: '320',
      helpText: 'The name of the plugin.'
    },
    { xtype: 'textfield',
      fieldLabel: 'Version',
      name: 'version',
      readOnly: true,
      width: '320',
      helpText: 'The version of the plugin.'
    },    
    { xtype: 'textfield',
      fieldLabel: 'Description',
      name: 'description',
      readOnly: true,
      width: '320',
      helpText: 'The description of the plugin.'
    },
    { xtype: 'textfield',
      fieldLabel: 'Status',
      name: 'status',
      readOnly: true,
      width: '320',
      helpText: 'The status of the plugin.'
    },    
    { xtype: 'textfield',
      fieldLabel: 'SCM Version',
      name: 'scmVersion',
      readOnly: true,
      width: '320',
      helpText: 'The SCM version of the plugin.'
    },
    {
      xtype: 'textfield',
      fieldLabel: 'Site',
      name: 'site',
      readOnly: true,
      width: '600',
      helpText: 'The Site URL of the plugin.'
    }
    ]
  } );
};

Ext.extend( Sonatype.repoServer.PluginInfoTab, Sonatype.ext.FormPanel, {
    checkPayload : function(){},
    loadData: function(){
      this.find('name', 'name')[0].setValue( this.payload.data.name );
      this.find('name', 'description')[0].setValue( this.payload.data.description );
      this.find('name', 'version')[0].setValue( this.payload.data.version );
      this.find('name', 'status')[0].setValue( this.payload.data.status );
      this.find('name', 'scmVersion')[0].setValue( this.payload.data.scmVersion );
      this.find('name', 'site')[0].setValue( this.payload.data.site );
      
      var failureReason = this.payload.data.failureReason;
      if ( failureReason ) {
        var html = '<h4 style="color:red">This plugin was not able to be activated</h4><br/>';
        html = html + '<pre> ' + failureReason + '</pre><br/>';
        this.add ( {
            frame: true,
            xtype: 'panel',
            autoScroll: true,
            html: html
        } );
      }

    }
} );


Sonatype.Events.addListener( 'pluginInfoInit', function( cardPanel, rec, gridPanel ) {
  var sp = Sonatype.lib.Permissions;
  cardPanel.add( new Sonatype.repoServer.PluginInfoTab( {
    name: 'info',
    tabTitle: 'Info',
    payload: rec 
  } ) );
} );