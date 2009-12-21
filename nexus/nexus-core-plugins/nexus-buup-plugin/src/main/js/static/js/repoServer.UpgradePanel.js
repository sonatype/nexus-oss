Sonatype.repoServer.UpgradePanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  this.wizard = new Sonatype.utils.WizardPanel({
    steps: ['License Agreement', 'User Information', 'JVM Memory', 'Activation', 'Download', 'Upgrade'],
    activeStep: 0
  });

  this.countryStore = new Ext.data.SimpleStore({
    fields: ['value', 'display'],
    data: [
['United States', 'United States'],
['Canada', 'Canada'],
['Australia', 'Australia'],
['Austria', 'Austria'],
['Belgium', 'Belgium'],
['Brazil', 'Brazil'],
['Bulgaria', 'Bulgaria'],
['China', 'China'],
['Croatia', 'Croatia'],
['Cyprus', 'Cyprus'],
['Czech Republic', 'Czech Republic'],
['Denmark', 'Denmark'],
['Estonia', 'Estonia'],
['Finland', 'Finland'],
['France', 'France'],
['Germany', 'Germany'],
['Greece', 'Greece'],
['Hungary', 'Hungary'],
['Iceland', 'Iceland'],
['India', 'India'],
['Indonesia', 'Indonesia'],
['Ireland', 'Ireland'],
['Israel', 'Israel'],
['Italy', 'Italy'],
['Japan', 'Japan'],
['Latvia', 'Latvia'],
['Lithuania', 'Lithuania'],
['Luxembourg', 'Luxembourg'],
['Malaysia', 'Malaysia'],
['Malta', 'Malta'],
['Mexico', 'Mexico'],      
['Netherlands', 'Netherlands'],
['New Zealand', 'New Zealand'],
['Norway', 'Norway'],
['Phillipines', 'Phillipines'],
['Poland', 'Poland'],                   
['Portugal', 'Portugal'],      
['Romania', 'Romania'],
['Singapore', 'Singapore'],
['Slovakia', 'Slovakia'],
['Slovenia', 'Slovenia'],
['South Korea', 'South Korea'],
['Spain', 'Spain'],             
['Sweden', 'Sweden'],
['Switzerland', 'Switzerland'],
['Taiwan', 'Taiwan'],
['Thailand', 'Thailand'],
['UK', 'UK'],
['Vietnam', 'Vietnam']
    ]
  });
  
  this.stepLicense = new Ext.Panel({
      id: 'step-0',
      items: [
      {
        id: 'eula-content',
        xtype: 'panel',
        title: 'Sonatype Nexus Professional End User License Agreement (EULA)',
        width: 'auto',
        height: 290,
        autoScroll: true,
        style: 'margin: 15px 0px 0px 15px; background-color: white; border-width: 0px 1px 1px 1px',
        bodyStyle: 'padding: 0px 10px 0px 10px'
      },
      {
        xtype: 'panel',
        layout: 'column',
        style: 'margin: 10px 0px 0px 15px',
        items: [
        {
          xtype: 'checkbox',
          name: 'acceptLicense',
          listeners: {
            'check': function ( cmpt, checked ) {
               var stepNextBtn = Ext.getCmp('wizardBtnNext');
               if ( checked ) {
                 stepNextBtn.enable();
               }
               else {
                 stepNextBtn.disable();
               }
            }
          }
        },
        {
          html: 'I have read and agree to all terms in the above EULA',
          style: 'margin: 3px 0px 0px 6px'
        }
        ]
      }
      ],
      listeners: {
        'beforeshow' : function( cmpt ){
          var stepBackBtn = Ext.getCmp('wizardBtnBack');
          stepBackBtn.disable();
          var licenseCheckbox = this.find('name', 'acceptLicense')[0];
          var stepNextBtn = Ext.getCmp('wizardBtnNext');
          if ( licenseCheckbox.getValue() ) {
            stepNextBtn.enable();
          }
          else {
            stepNextBtn.disable();
          }
        }
      }
  });

  Ext.Ajax.request({
    method: 'GET',
    url: Sonatype.config.resourcePath + '/html/license.html',
    headers: {
      accept: 'text/html' 
    },
    success: function ( response, options ) {
      var licenseHtml = response.responseText;
      Ext.getCmp('eula-content').body.dom.innerHTML = licenseHtml;
    }
  }); 
  
  this.stepUser = new Ext.Panel({
    id: 'step-1',
    layout: 'column',
    bodyStyle: 'margin: 10px',
    hideMode: 'offsets',
    items: [
    {
      xtype: 'fieldset',
      title: 'Contact Information',
      collapsible: false,
      autoHeight: true,
      bodyStyle: 'margin: 10px',
      items: [
      {
        xtype: 'textfield',
        width: 160,
        fieldLabel: 'First Name',
        name: 'firstName'
      },
      {
        xtype: 'textfield',
        width: 160,
        fieldLabel: 'Last Name',
        name: 'lastName'
      },
      {
        xtype: 'textfield',
        width: 160,
        fieldLabel: 'Title',
        name: 'title'
      },
      {
        xtype: 'textfield',
        width: 160,
        fieldLabel: 'Email',
        name: 'email'
      },
      {
        xtype: 'textfield',
        width: 160,
        fieldLabel: 'Phone',
        name: 'phone'
      }
      ]
    },
    {
      html: '&nbsp',
      width: 50
    },
    {
      xtype: 'fieldset',
      title: 'Address Information',
      collapsible: false,
      autoHeight: true,
      bodyStyle: 'margin: 10px',
      items: [
      {
        xtype: 'textfield',
        width: 168,
        fieldLabel: 'Organization',
        name: 'organization'
      },
      {
        xtype: 'textfield',
        width: 168,
        fieldLabel: 'Street Address',
        name: 'streetAddress'
      }, 
      {
        xtype: 'textfield',
        width: 168,
        fieldLabel: 'City',
        name: 'city'
      }, 
      {
        xtype: 'combo',
        width: 185,
        fieldLabel: 'State/Province',
        name: 'state',
        emptyText: 'Selete One(US/Canada Only)'
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Zip Code',
        width: 168,
        name: 'zipCode'
      }, 
      {
        xtype: 'combo',
        fieldLabel: 'Country',
        name: 'country',
        emptyText: 'Select One',
        store: this.countryStore,
        displayField: 'display',
        valueField: 'value',
        triggerAction: 'all',
        mode: 'local',
        width: 185,
        editable: false
      }
      ]
    } 
    ],
    listeners: {
      'beforeshow' : function( cmpt ){
          var stepBackBtn = Ext.getCmp('wizardBtnBack');
          stepBackBtn.enable();
          var stepNextBtn = Ext.getCmp('wizardBtnNext');
          stepNextBtn.enable();
      }
    }
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