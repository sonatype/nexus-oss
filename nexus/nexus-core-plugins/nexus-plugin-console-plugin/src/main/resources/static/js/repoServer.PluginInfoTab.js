Sonatype.repoServer.PluginInfoTab = function( config ) {
  var config = config || {};
  var defaultConfig = {
    readOnly: true
  };
  Ext.apply( this, config, defaultConfig );
  var labelClass = 'font: bold 12px tahoma, arial, helvetica, sans-serif;';
  var textClass = 'font: normal 12px tahoma, arial, helvetica, sans-serif; padding: 0px 0px 0px 15px';
  
  Sonatype.repoServer.PluginInfoTab.superclass.constructor.call( this, {
    frame: true,
    items: [{
        xtype: 'panel',
        style: 'padding: 10px 0px 0px 10px;',
        layout: 'table',
      layoutConfig: {
        columns: 2
      },
      items: [
      { 
        xtype: 'label',
        html: 'Name',
        style: labelClass,
        width: 120
      },
      {
        xtype: 'label',
        name: 'name',
        style: textClass,
        width: 320
      },
      {
        xtype: 'label',
        html: 'Version',
        style: labelClass,
        width: 120
      },
      {
        xtype: 'label',
        name: 'version',
        style: textClass,
        width: 320
      },
      {
        xtype: 'label',
        html: 'Status',
        style: labelClass,
        width: 120
      },
      {
        xtype: 'label',
        name: 'status',
        style: textClass,
        width: 320
      },
      {
        xtype: 'label',
        html: 'Description',
        style: labelClass,
        width: 120
      },
      {
        xtype: 'label',
        name: 'description',
        style: textClass,
        width: 320
      },
      {
        xtype: 'label',
        html: 'SCM Version',
        style: labelClass,
        width: 120
      },
      {
        xtype: 'label',
        name: 'scmVersion',
        style: textClass,
        width: 320
      },
      {
        xtype: 'label',
        html: 'SCM Timestamp',
        style: labelClass,
        width: 120
      },
      {
        xtype: 'label',
        name: 'scmTimestamp',
        style: textClass,
        width: 320
      }
      ]
  }],
    listeners: {
       beforerender: {
         fn: this.beforerenderHandler,
         scope: this
       }
    }
  } );
};

Ext.extend( Sonatype.repoServer.PluginInfoTab, Ext.Panel, {
  beforerenderHandler: function( panel ){
    this.find('name', 'name')[0].setText( this.payload.data.name );
    this.find('name', 'version')[0].setText( this.payload.data.version );
    this.find('name', 'description')[0].setText( this.payload.data.description );
    this.find('name', 'status')[0].setText( this.payload.data.status );
    this.find('name', 'scmVersion')[0].setText( this.payload.data.scmVersion );
    this.find('name', 'scmTimestamp')[0].setText( this.payload.data.scmTimestamp );
    
    var pluginPropertiesPanel = this.items.get(0);
    
    var site = this.payload.data.site;
    pluginPropertiesPanel.add({
      xtype: 'label',
      html: 'Site',
      style: 'font: bold 12px tahoma, arial, helvetica, sans-serif;',
      width: 120
      });    
    if ( site ) {
      pluginPropertiesPanel.add({
        xtype: 'label',
        name: 'site',
        html: '<a href="' + site + '">' + site + '</a>',
        style: 'font: normal 12px tahoma, arial, helvetica, sans-serif; padding: 0px 0px 0px 15px'
      });
    }
    
    var failureReason = this.payload.data.failureReason;
    if ( failureReason ) {
      var html = '<h4 style="color:red;">This plugin was not able to be activated</h4><br/>';
      html = html + '<pre> ' + failureReason + '</pre><br/>';
      this.add ( {
        xtype: 'panel',
        frame: true,
        style: 'padding: 20px 0px 0px 10px;',
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

/**
 * Simply leave this class here for future usage
 * @param {} config
 */
Sonatype.ext.HelpIcon = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  Sonatype.ext.HelpIcon.superclass.constructor.call( this, {
    style: 'padding: 0px 0px 0px 10px',
    autoEl: {
      tag: 'img',
      src: Sonatype.config.resourcePath + '/images/icons/help.png',
      width: 16,
      height: 16
    },
    listeners: {
      'beforerender': {
        fn: this.beforerenderHandler,
        scope: this
      }
    }
  });
};

Ext.extend( Sonatype.ext.HelpIcon, Ext.BoxComponent, {
  beforerenderHandler: function( box ){
    if( this.helpText ){
      Ext.QuickTips.register({
          target:  box,
          title: '',
          text: this.helpText,
          enabled: true
      });      
    }
  }
} );