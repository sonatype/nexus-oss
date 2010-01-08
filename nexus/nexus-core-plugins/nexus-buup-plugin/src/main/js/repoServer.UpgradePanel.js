Sonatype.repoServer.UpgradePanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  this.wizard = new Sonatype.utils.WizardPanel({
    steps: ['License Agreement', 'User Information', 'JVM Memory', 'Activation', 'Download', 'Upgrade'],
    activeStep: 0
  });

  this.stepLicense = new Sonatype.repoServer.UpgradeLicensePanel();
  this.stepUser = new Sonatype.repoServer.UpgradeUserPanel(); 
  this.stepJVM = new Sonatype.repoServer.UpgradeJVMPanel();
  this.stepEmail = new Sonatype.repoServer.UpgradeEmailPanel();
  this.stepDownload = new Sonatype.repoServer.UpgradeDownloadPanel();
  this.stepUpgrade = new Sonatype.repoServer.UpgradeUpgradePanel();

  Sonatype.repoServer.UpgradePanel.superclass.constructor.call( this, {
    id: 'upgradePanel',
    frame: true,
    autoScroll: true,
    buttonAlign: 'center',
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
        items: [  this.stepLicense, this.stepUser, this.stepJVM, this.stepEmail, this.stepDownload, this.stepUpgrade ]
      }
    ],
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
  } );

  Ext.Ajax.request({
    url: Sonatype.config.servicePath + '/buup/upgradeStatus',
    success: function( response, options ){
      var resp = Ext.decode( response.responseText );
      var upgradeStatus = resp.data.upgradeStatus;
      var upgradePanel = Ext.getCmp('upgradePanel');
      if ( upgradeStatus == 'UNUSED' ) {
        upgradePanel.goToStep(0);
      }
      else if ( upgradeStatus == 'WAIT_FOR_ACTIVATION' ){
        upgradePanel.goToStep(3);
      }
      else if ( upgradeStatus == 'DOWNLOADING' ){
        upgradePanel.goToStep(4);
      }
      else if ( upgradeStatus == 'DOWNLOADED' ){
        upgradePanel.goToStep(5);
      }
    }
  });

};

Ext.extend( Sonatype.repoServer.UpgradePanel, Ext.Panel, {
  isPanelValid : function( panel ) {
    if (!panel.items){
      return true;
    }
    var items = panel.items.getRange();
    for ( var i = 0 ; i < items.length ; i++ ) {
      if ( items[i].xtype == 'fieldset' 
        && !this.isPanelValid( items[i] ) ) {
         return false;
      }
      else if ( items[i].isValid 
          && !items[i].isValid() ) {
        return false;
      }
    }
    return true;
  },
  stepNext: function( btn ) {
    var layout = Ext.getCmp('cardWizard').getLayout();
    if ( !this.isPanelValid( layout.activeItem ) ){
      return;
    }
    if ( layout.activeItem.onNextHandler ) {
      layout.activeItem.onNextHandler( this );
    }
    else {
      this.doStepNext();
    }
  },
  doStepNext: function(){
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
    if( layout.activeItem.onBackHandler ){
      layout.activeItem.onBackHandler( this );
    }
    else{
      this.doStepBack();
    }
  },
  doStepBack: function( btn ) {
    var layout = Ext.getCmp('cardWizard').getLayout();
    var activeStepIndex = layout.activeItem.id.split('step-')[1];
    var back = parseInt(activeStepIndex) - 1;
    if ( back >= 0 ) {
      layout.setActiveItem(back); 
      this.wizard.setActiveStep(back);
    }
  },
  goToStep: function( index ) {
    var layout = Ext.getCmp('cardWizard').getLayout();
    layout.setActiveItem(index);
    this.wizard.setActiveStep(index);
  }
});

Sonatype.repoServer.UpgradeLicensePanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );
  Sonatype.repoServer.UpgradeLicensePanel.superclass.constructor.call( this, {
      id: 'step-0',
      autoWidth: true,
      autoHeight: true,
      autoScroll: true,
      hideMode: 'offsets',
      items: [
      {
        id: 'eula-content',
        xtype: 'panel',
        title: 'Sonatype Nexus Professional End User License Agreement (EULA)',
        height: 290,
        autoWidth: true,
        autoScroll: true,
        autoLoad: {
          url: Sonatype.config.resourcePath + '/html/license.html',
          headers: {
            accept: 'text/html' 
          }
        },
        style: 'margin: 15px 15px 0px 15px; background-color: white; border-width: 0px 1px 1px 1px',
        bodyStyle: 'padding: 0px 10px 0px 10px'
      },
      {
        xtype: 'panel',
        layout: 'column',
        style: 'margin: 10px 0px 0px 15px',
        items: [
        {
          html: 'I have read and agree to all terms in the above EULA',
          style: 'margin: 3px 8px 0px 0px',
	  width: 260
        },
        {
          xtype: 'checkbox',
          name: 'acceptLicense',
	  width: 30,
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
        }
        ]
      }
      ],
      listeners: {
        beforeshow: { 
          fn: this.beforeShowHandler,
          scope: this
        }
      }  
  });
};

Ext.extend( Sonatype.repoServer.UpgradeLicensePanel, Ext.Panel, {
  beforeShowHandler: function(){
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
});

Sonatype.repoServer.UpgradeUserPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );
 
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

  this.stateStore = new Ext.data.GroupingStore({
    reader: new Ext.data.ArrayReader({}, [
      {name: 'value'},
      {name: 'display'},
      {name: 'country'}
    ]),
    data: [
      ['AL', 'AL', 'US'],
      ['AK', 'AK', 'US'],
      ['AS', 'AS', 'US'],
      ['AZ', 'AZ', 'US'],
      ['AR', 'AR', 'US'],
      ['CA', 'CA', 'US'],
      ['CO', 'CO', 'US'],
      ['CT', 'CT', 'US'],
      ['DE', 'DE', 'US'],
      ['DC', 'DC', 'US'],
      ['FM', 'FM', 'US'],
      ['FL', 'FL', 'US'],
      ['GA', 'GA', 'US'],
      ['GU', 'GU', 'US'],
      ['HI', 'HI', 'US'],
      ['ID', 'ID', 'US'],
      ['IL', 'IL', 'US'],
      ['IN', 'IN', 'US'],
      ['IA', 'IA', 'US'],
      ['KS', 'KS', 'US'],
      ['KY', 'KY', 'US'],
      ['LA', 'LA', 'US'],
      ['ME', 'ME', 'US'],
      ['MH', 'MH', 'US'],
      ['MD', 'MD', 'US'],
      ['MA', 'MA', 'US'],
      ['MI', 'MI', 'US'],
      ['MN', 'MN', 'US'],
      ['MS', 'MS', 'US'],
      ['MO', 'MO', 'US'],
      ['MT', 'MT', 'US'],
      ['NE', 'NE', 'US'],
      ['NV', 'NV', 'US'],
      ['NH', 'NH', 'US'],
      ['NJ', 'NJ', 'US'],
      ['NM', 'NM', 'US'],
      ['NY', 'NY', 'US'],
      ['NC', 'NC', 'US'],
      ['ND', 'ND', 'US'],
      ['MP', 'MP', 'US'],
      ['OH', 'OH', 'US'],
      ['OK', 'OK', 'US'],
      ['OR', 'OR', 'US'],
      ['PW', 'PW', 'US'],
      ['PA', 'PA', 'US'],
      ['PR', 'PR', 'US'],
      ['RI', 'RI', 'US'],
      ['SC', 'SC', 'US'],
      ['SD', 'SD', 'US'],
      ['TN', 'TN', 'US'],
      ['TX', 'TX', 'US'],
      ['UT', 'UT', 'US'],
      ['VT', 'VT', 'US'],
      ['VI', 'VI', 'US'],
      ['VA', 'VA', 'US'],
      ['WA', 'WA', 'US'],
      ['WV', 'WV', 'US'],
      ['WI', 'WI', 'US'],
      ['WY', 'WY', 'US'],
      ['AB', 'AB', 'Canada'],
      ['BC', 'BC', 'Canada'],
      ['MB', 'MB', 'Canada'],
      ['NB', 'NB', 'Canada'],
      ['NL', 'NL', 'Canada'],
      ['NS', 'NS', 'Canada'],
      ['ON', 'ON', 'Canada'],
      ['PE', 'PE', 'Canada'],
      ['SK', 'SK', 'Canada'],
      ['QC', 'QC', 'Canada']
    ],
    groupField: 'country',
    sortInfo:{field: 'display', direction: "ASC"}
  });

  Sonatype.repoServer.UpgradeLicensePanel.superclass.constructor.call( this, {
    id: 'step-1',
    layout: 'column',
    autoWidth: true,
    autoScroll: true,
    bodyStyle: 'margin: 10px',
    hideMode: 'offsets',
    items: [
    {
      xtype: 'fieldset',
      title: 'Contact Information',
      collapsible: false,
      autoHeight: true,
      width: 340,
      bodyStyle: 'margin: 10px',
      items: [
      {
        xtype: 'textfield',
        width: 140,
        fieldLabel: 'First Name',
        name: 'firstName',
        allowBlank: false,
        itemCls: 'required-field',
        helpText: 'Your first name.'
      },
      {
        xtype: 'textfield',
        width: 140,
        fieldLabel: 'Last Name',
        name: 'lastName',
        allowBlank: false,
        itemCls: 'required-field',
        helpText: 'Your last name.'
      },
      {
        xtype: 'textfield',
        width: 140,
        fieldLabel: 'Title',
        name: 'title',
        helpText: 'Your title.'
      },
      {
        xtype: 'textfield',
        width: 140,
        fieldLabel: 'Email',
        name: 'email',
        allowBlank: false,
        itemCls: 'required-field',
        regex: /^.+@.+/,
        helpText: 'Your email address.'
      },
      {
        xtype: 'textfield',
        width: 140,
        fieldLabel: 'Phone',
        name: 'phone',
        helpText: 'Your phone number.'
      }
      ]
    },
    {
      html: '&nbsp',
      width: 20,
      autoHeight: true
    },
    {
      xtype: 'fieldset',
      title: 'Address Information',
      collapsible: false,
      autoHeight: true,
      width: 400,
      bodyStyle: 'margin: 10px',
      items: [
      {
        xtype: 'textfield',
        width: 185,
        fieldLabel: 'Organization',
        name: 'organization',
        helpText: 'Your organization name.'

      },
      {
        xtype: 'textfield',
        width: 185,
        fieldLabel: 'Street Address',
        name: 'streetAddress',
        helpText: 'Your street address.'
      }, 
      {
        xtype: 'textfield',
        width: 185,
        fieldLabel: 'City',
        name: 'city',
        helpText: 'Your city.'
      }, 
      {
        xtype: 'uxgroupcombo',
        width: 185,
        fieldLabel: 'State/Province',
        name: 'state',
        emptyText: 'Selete One(US/Canada Only)',
        displayField: 'display',
        valueField: 'value',
        store: this.stateStore,
        showGroupName: false,
        triggerAction: 'all',
        mode: 'local',
        editable: false,
        helpText: 'Your state/province.'
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Zip Code',
        width: 185,
        name: 'zipCode',
        helpText: 'The zip code of your address.'
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
        editable: false,
        helpText: 'Your country.'
      }
      ]
    } 
    ],
    listeners: {
      'beforeshow' : {
        fn: this.beforeshowHandler,
	scope: this
      } 
    }
  });
};

Ext.extend( Sonatype.repoServer.UpgradeUserPanel, Ext.Panel, {
  beforeshowHandler: function ( cmpt ) {
    var stepBackBtn = Ext.getCmp('wizardBtnBack');
    stepBackBtn.enable();
    var stepNextBtn = Ext.getCmp('wizardBtnNext');
    stepNextBtn.enable();
  }
});

Sonatype.repoServer.UpgradeJVMPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  this.memoryStore = new Ext.data.SimpleStore({
    fields: ['value', 'display'],
    data: [
      ['128m', '128m'],
      ['256m', '256m'],
      ['512m', '512m'],
      ['768m', '768m'],
      ['1024m', '1024m'],
      ['1536m', '1536m'],
      ['2048m', '2048m']
    ]
  });

  Sonatype.repoServer.UpgradeJVMPanel.superclass.constructor.call( this, {
    id: 'step-2',
    hideMode: 'offsets',
    bodyStyle: 'margin: 10px',
    items: [
    {
      xtype: 'fieldset',
      title: 'JVM Memory Configuration',
      collapsible: false,
      autoHeight: true,
      width: 350,
      bodyStyle: 'margin: 10px',
      items: [
        {
          xtype: 'panel',
          html: 'These 2 values will be passed to Java Virtual Machine when the new Nexus is beging started.<br/>' +
	        '<ul><li><em>-Xms</em> sets the initial heap size of the JVM.</li>' +
		'<li><em>-Xmx</em> sets the maximal heap size of the JVM.</li></ul>' +
		'Note that the value of -Xms must be less than or equal to the -Xmx value.',
          bodyStyle: 'margin-bottom: 20px'
        },
        {
          xtype: 'combo',
          name: 'xms',
          fieldLabel: '-Xms',
          store: this.memoryStore,
          displayField: 'display',
          valueField: 'value',
          regex: /^\d+m$/,
          triggerAction: 'all',
          mode: 'local',
          itemCls: 'required-field',
          allowBlank: false,
          width: 120,
          helpText: 'The initial heap size of the JVM which will be used by Nexus.',
          validator: function ( value ){
            var xmxCombo = this.ownerCt.find('name', 'xmx')[0];
            xmxCombo.clearInvalid();
            var xmxValue = xmxCombo.getValue();
            if ( parseInt( value.split('m')[0] ) > parseInt( xmxValue.split('m')[0] ) ) {
              return 'The -Xms value must not be larger than the -Xmx value.';
            }
            return true;
          }
        },
        {
          xtype: 'combo',
          name: 'xmx',
          fieldLabel: '-Xmx',
          store: this.memoryStore,
          displayField: 'display',
          valueField: 'value',
          regex: /^\d+m$/,
          triggerAction: 'all',
          mode: 'local',
          itemCls: 'required-field',
          allowBlank: false,
          width: 120,
          helpText: 'The maximum heap size of the JVM which will be used by Nexus.',
          validator: function ( value ){
            var xmsCombo = this.ownerCt.find('name', 'xms')[0];
            xmsCombo.clearInvalid();
            var xmsValue = xmsCombo.getValue();
            if ( parseInt( value.split('m')[0] ) < parseInt( xmsValue.split('m')[0] ) ) {
              return 'The -Xmx value must not be smaller than the -Xms value.';
            }
            return true;
          }
        }
      ],
      listeners: {
        'afterlayout': function( cmpt ){
          this.find('name', 'xms')[0].setValue('256m');
          this.find('name', 'xmx')[0].setValue('512m');
        }      
      }
    }
    ],
    listeners: {
      'beforeshow' : {
        fn: this.beforeshowHandler,
	scope: this
      }
    }     
  });
};

Ext.extend( Sonatype.repoServer.UpgradeJVMPanel, Ext.Panel, {
  beforeshowHandler: function ( cmpt ) {
    var stepBackBtn = Ext.getCmp('wizardBtnBack');
    stepBackBtn.enable();
    var stepNextBtn = Ext.getCmp('wizardBtnNext');
    stepNextBtn.enable();
  },
  onNextHandler : function( upgradePanel ) {
    var licensePanel = Ext.getCmp('step-0');
    var userPanel = Ext.getCmp('step-1');
    var jvmPanel = Ext.getCmp('step-2');
    var json = { data : {} };
    json.data.agreeLicenseAgreement = licensePanel.find('name', 'acceptLicense')[0].getValue();
    json.data.firstName = userPanel.find('name', 'firstName')[0].getValue();
    json.data.lastName = userPanel.find('name', 'lastName')[0].getValue();
    json.data.title = userPanel.find('name', 'title')[0].getValue();
    json.data.email = userPanel.find('name', 'email')[0].getValue();
    json.data.phone = userPanel.find('name', 'phone')[0].getValue();
    json.data.organization = userPanel.find('name', 'organization')[0].getValue();
    json.data.streetAddress = userPanel.find('name', 'streetAddress')[0].getValue();
    json.data.city = userPanel.find('name', 'city')[0].getValue();
    json.data.state = userPanel.find('name', 'state')[0].getValue();
    json.data.zipCode = userPanel.find('name', 'zipCode')[0].getValue();
    json.data.country = userPanel.find('name', 'country')[0].getValue();
    json.data.xmxInMbs = parseInt(jvmPanel.find('name', 'xmx')[0].getValue());
    json.data.xmsInMbs = parseInt(jvmPanel.find('name', 'xmx')[0].getValue());
    
    Ext.Ajax.request({
      url: Sonatype.config.servicePath + '/buup/prepareUpgrade',
      method: 'POST',
      jsonData: json,
      success: function( response, options ){
        upgradePanel.doStepNext();
      }
    });
  }
});

Sonatype.repoServer.UpgradeEmailPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );
  Sonatype.repoServer.UpgradeEmailPanel.superclass.constructor.call( this, {
    id: 'step-3',
    hideMode: 'offsets',
    bodyStyle: 'margin: 10px; font-size: 13px',
    html: 'A download activation email has been sent to juven@sonatype.com.</br>' + 
          'Please check the email box and click the activation link.</br>' +
	  '<br/>' +
          'To start the download, press the <b>Next</b> button. Note that the download process may take a while.<br/>' +
	  'You can also restart the upgrade process by press the <b>Back</b> button if you want to use another email address.',
    listeners: {
      beforeshow: {
        fn : this.beforeshowHandler,
	scope: this
      }
    }
  });
}  

Ext.extend( Sonatype.repoServer.UpgradeEmailPanel, Ext.Panel, {
  beforeshowHandler: function ( cmpt ) {
    var stepBackBtn = Ext.getCmp('wizardBtnBack');
    stepBackBtn.enable();
    var stepNextBtn = Ext.getCmp('wizardBtnNext');
    stepNextBtn.enable();
  },
  onNextHandler: function( upgradePanel ){
    Ext.Ajax.request({
      url: Sonatype.config.servicePath + '/buup/initiateDownload',
      method: 'POST',
      jsonData: {data:{}},
      success: function( response, options ){
        upgradePanel.doStepNext();
      }
    }); 
  },
  onBackHandler: function( upgradePanel ){
    //TODO update server status to initial
    upgradePanel.goToStep(0);
  }
});

Sonatype.repoServer.UpgradeDownloadPanel = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );
  Sonatype.repoServer.UpgradeDownloadPanel.superclass.constructor.call( this, {
    id: 'step-4',
    bodyStyle: 'margin: 20px 10px 20px 10px; font-size: 13px',
    hideMode: 'offsets',
    items: [
    {
      xtype: 'panel',
      html: 'Nexus is downloading the Professional Bundle. This may take a while, it depends on the speed of your connection to the Internet.' 
    },
    {
      xtype: 'progress',
      style: 'margin-top: 40px',
      id: 'pBar',
      width: 600
    }
    ],
    listeners: {
      'beforeshow' : {
        fn: this.beforeshowHandler,
	scope: this
      }
    }
  });
};

Ext.extend( Sonatype.repoServer.UpgradeDownloadPanel, Ext.Panel, {
  beforeshowHandler: function( cmpt ) {
    var stepBackBtn = Ext.getCmp('wizardBtnBack');
    stepBackBtn.disable();
    var stepNextBtn = Ext.getCmp('wizardBtnNext');
    stepNextBtn.enable();
    var progressBar = Ext.getCmp('pBar');
    progressBar.wait({
      text: 'Downloading...',
      fn: function() {
      //do something simple
      }
    });
  }
});

Sonatype.repoServer.UpgradeUpgradePanel = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );
  Sonatype.repoServer.UpgradeUpgradePanel.superclass.constructor.call( this, {
    id: 'step-5',
    bodyStyle: 'margin: 20px 10px 20px 10px; font-size: 13px',
    hideMode: 'offsets',
    html: 'The Profesional Bundle has been downloaded.<br/><br/>' +
          'To compelete the upgrade, press the <em>Next</em> button below.<br/><br/>' +
	  'Note that Nexus will be shutdown for a while and restarted.<br/>'
  });
};

Ext.extend( Sonatype.repoServer.UpgradeUpgradePanel, Ext.Panel, {
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
    }
  });
 
  this.initSteps(this);
};

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
  if ( sp.checkPermission('nexus:buup', sp.READ) && sp.checkPermission('nexus:buup', sp.CREATE) ){
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

