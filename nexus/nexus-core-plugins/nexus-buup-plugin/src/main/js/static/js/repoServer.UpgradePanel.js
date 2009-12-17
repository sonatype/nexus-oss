Sonatype.repoServer.UpgradePanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  var wizard = new Sonatype.utils.WizardPanel({
    steps: ['License Agreement', 'User Information', 'JVM Memory', 'Activation', 'Download', 'Upgrade'],
    activeStep: 1
  });

  Sonatype.repoServer.UpgradePanel.superclass.constructor.call( this, {
    frame: true,
    autoScroll: true,
    items: [
      wizard,
      {
        xtype: 'panel',
        html: '<hr width="98%"/>'
      },
      {
        xtype: 'panel',
        html: '<h2 style="font: bold 16px tahoma, arial, helvetica, sans-serif; padding: 15px 15px 15px 15px;">Sonatype Nexus Professional End User License Agreement (EULA)</h2>'
      },
      {
        xtype: 'panel',
        buttonAlign: 'center',
        items: [
        {
          xtype: 'textarea'
        }
        ],
        buttons: [
        {
          text: '< Back',
          disabled: true
        },
        {
          text: 'Next >'
        }
        ]
      }
    ]
  } );
};

Ext.extend( Sonatype.repoServer.UpgradePanel, Ext.Panel, {
});

Sonatype.utils.WizardPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    layout: 'table',
    style: 'padding: 10px 10px 10px 10px;',
    activeStep: 0,
    border: true,
    margins: '5, 5, 5, 5',
    layoutConfig: {
      columns: 3
    }
  };
  Ext.apply( this, config, defaultConfig );  

  Sonatype.utils.WizardPanel.superclass.constructor.call( this, {
    listeners: {
      'beforerender': { 
        fn: this.onBeforerender,
        scope: this 
      }
    }
  });
}

Ext.extend( Sonatype.utils.WizardPanel, Ext.Panel, {
  onBeforerender: function( component ) {
    for( var i = 0 ; i < this.steps.length ; i++ ) {
      var text;
      var disabled = true;
      if ( this.activeStep - 1 == i ) {
        text = '<b>' + (i+1) + '. ' + this.steps[i] + '</b>';
        disabled = false;
      }
      else {
        text = (i+1) + '. ' + this.steps[i];
      }
      this.add({
        xtype: 'label',
        id: this.id + (i + 1),
        html: text,
        disabled: disabled,
        style: 'font: normal 16px tahoma, arial, helvetica, sans-serif; padding: 0px 15px 0px 0px;'
      });
    }
  }
});

Sonatype.Events.addListener( 'nexusNavigationInit', function( nexusPanel ) {
  var sp = Sonatype.lib.Permissions;
  if ( true ){
    nexusPanel.add( {
      enabled: true,
      sectionId: 'st-nexus-config',
      title: 'Upgrade',
      tabId: 'upgrade',
      tabTitle: 'Upgrade',
      tabCode: Sonatype.repoServer.UpgradePanel
    } );
  }
} );