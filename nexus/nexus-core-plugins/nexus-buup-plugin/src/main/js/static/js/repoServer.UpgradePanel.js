Sonatype.repoServer.UpgradePanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  this.wizard = new Sonatype.utils.WizardPanel({
    steps: ['License Agreement', 'User Information', 'JVM Memory', 'Activation', 'Download', 'Upgrade'],
    activeStep: 0
  });
  
  this.stepLicense = new Ext.Panel({
      id: 'step-0',
      items: [
      {
        xtype: 'panel',
        html: '<h2 style="font: bold 16px tahoma, arial, helvetica, sans-serif; padding: 15px 15px 15px 15px;">Sonatype Nexus Professional End User License Agreement (EULA)</h2>'
      },
      {
        xtype: 'textarea'
      },
      {
        xtype: 'checkbox',
        fieldLabel: 'I accpet',
        name: 'acceptEULA'
      }
      ],
      listeners: {
        'beforeshow' : function( cmpt ){
            var stepBackBtn = Ext.getCmp('wizardBtnBack');
            stepBackBtn.disable();
        }
      
      }
  });
  
  this.stepUser = new Ext.Panel({
    id: 'step-1',
    html: 'User Information'
  });
  
  this.stepJVM = new Ext.Panel({
    id: 'step-2',
    html: 'JVM'
  });

  Sonatype.repoServer.UpgradePanel.superclass.constructor.call( this, {
    frame: true,
    autoScroll: true,
    items: [
      this.wizard,
      {
        xtype: 'panel',
        html: '<hr width="98%"/>'
      },
      {
        xtype: 'panel',
        id: 'cardWizard',
        layout: 'card',
        activeItem: 0,
        items: [ this.stepLicense, this.stepUser, this.stepJVM ]
      },
      {
        xtype: 'panel',
        buttonAlign: 'center',
        buttons: [
        {
          id: 'wizardBtnBack',
          text: '< Back',
          handler: this.stepBack,
          scope: this
        },
        {
          id: 'wizardBtnNext',
          text: 'Next >',
          handler: this.stepNext,
          scope: this
        }
        ]
      }
    ]
  } );
};

Ext.extend( Sonatype.repoServer.UpgradePanel, Ext.Panel, {
  stepNext: function( btn ) {
    var layout = Ext.getCmp('cardWizard').getLayout();
    var activeStepIndex = layout.activeItem.id.split('step-')[1];
    var next = parseInt(activeStepIndex) + 1;
    if ( next < Ext.getCmp('cardWizard').items.length ) {
      layout.setActiveItem(next); 
      this.wizard.setActiveStep(next);
    }
  },
  stepBack: function( btn ) {
    var layout = Ext.getCmp('cardWizard').getLayout();
    var activeStepIndex = layout.activeItem.id.split('step-')[1];
    var back = parseInt(activeStepIndex) - 1;
    if ( back >= 0 ) {
      layout.setActiveItem(back); 
      this.wizard.setActiveStep(back);
    }
  }
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
  //    'beforerender': { 
  //      fn: this.onBeforerender,
  //      scope: this 
  //    }
    }
  });
 
  this.initSteps(this);
}

Ext.extend( Sonatype.utils.WizardPanel, Ext.Panel, {
  initSteps: function( component ) {
    for( var i = 0 ; i < this.steps.length ; i++ ) {
      var text = (i+1) + '. ' + this.steps[i];
      this.add({
        xtype: 'label',
        id: this.id + (i + 1),
        html: text,
        style: 'font: 14px tahoma, arial, helvetica, sans-serif; padding: 0px 15px 0px 0px; font-weight: bold'
      });
    }
    this.setActiveStep( this.activeStep );
  },
  setActiveStep: function( index ) {
    //TODO: more nice-looking style the the active label?
    var activeClass = '';
    for ( var i=0; i<this.items.length; i++ ) {
      var item = this.items.get(i);
      item.disable();
      item.removeClass(activeClass);
      if ( i == index ) {
        item.enable();
        item.addClass(activeClass);
      }
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