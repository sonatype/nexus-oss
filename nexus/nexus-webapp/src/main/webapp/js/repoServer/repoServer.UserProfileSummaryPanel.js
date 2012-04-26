/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
Sonatype.repoServer.UserProfile = function(config) {
  var ht = Sonatype.repoServer.resources.help.users;

  var config = config || {};

  if ( !config.username ){
    throw 'No username in config';
  }

  this.FIELD_WIDTH = 250;

  var items = [
    {
      xtype : 'fieldset',
      title : 'Details',
      autoHeight : true,
      layoutConfig : {
        labelSeparator : ''
      },
      items : [
        {
          xtype : 'box',
          style : 'position: absolute; left: 500px; top: 33px;',
          hideLabel : true,
          autoEl : {
            tag : 'div',
            children : [
              {
                tag : 'img',
                qtip : 'Profile picture from gravatar (http://www.gravatar.com/)',
                id : 'user-picture-' + config.username
              }
            ]
          }
        },
        {
          xtype : 'textfield',
          fieldLabel : 'User ID',
          itemCls : 'required-field',
          labelStyle : 'margin-left: 15px; width: 185px;',
          helpText : ht.userId,
          name : 'userId',
          disabled : true,
          allowBlank : false,
          width : this.FIELD_WIDTH
        },
        {
          xtype : 'textfield',
          fieldLabel : 'First Name',
          labelStyle : 'margin-left: 15px; width: 185px;',
          helpText : ht.firstName,
          name : 'firstName',
          allowBlank : true,
          width : this.FIELD_WIDTH,
          validator : function(v) {
            if (v && v.length != 0 && v.match(WHITE_SPACE_REGEX)) {
              return true;
            }
            else {
              return 'First Name cannot start with whitespace.';
            }
          }
        },
        {
          xtype : 'textfield',
          fieldLabel : 'Last Name',
          labelStyle : 'margin-left: 15px; width: 185px;',
          helpText : ht.lastName,
          name : 'lastName',
          allowBlank : true,
          width : this.FIELD_WIDTH,
          validator : function(v) {
            if (v && v.length != 0 && v.match(WHITE_SPACE_REGEX)) {
              return true;
            }
            else {
              return 'Last Name cannot start with whitespace.';
            }
          }
        },
        {
          xtype : 'textfield',
          fieldLabel : 'Email',
          itemCls : 'required-field',
          labelStyle : 'margin-left: 15px; width: 185px;',
          helpText : ht.email,
          name : 'email',
          allowBlank : false,
          width : this.FIELD_WIDTH
        },
        {
          xtype : 'label',
          // make this label's font look like the others
          cls : 'x-form-item',
          text : 'Change Password',
          style : 'text-decoration: underline; color: blue; cursor: pointer;',
          listeners : {
            'render' : function(component) {
              component.getEl().on('click', function() {Sonatype.utils.changePassword()});
            }
          }
        }
      ]
    }
  ];

  var defaultConfig = {
    minWidth : 650,
    labelWidth : 50,
    listeners : {
      submit : {
        fn : this.loadGravatarPicture,
        scope : this
      },
      load : {
        fn : this.loadGravatarPicture,
        scope : this
      }
    },
    items : items
  };

  Ext.apply(this, config, defaultConfig);


  // URI template, payload.id will be appended
  this.uri = Sonatype.config.servicePath + '/user_account';

  // this is a template for the data to be sent from the form fields
  this.referenceData = {
    userId : '',
    firstName : '',
    lastName : '',
    email : ''
  };

  // defining payload.id is a must, see Sonatype.ext.FormPanel#getActionUrl
  // in short, `payload.id` will be used to extend `this.uri` if `payload.data.resourceUri` is not set.
  // otherwise, `payload.data.resourceUri` is used as is.
  this.payload = {
    id : this.username
  }

  this.checkPayload();

  Sonatype.repoServer.UserProfile.superclass.constructor.call(this, config);
};

Ext.extend(Sonatype.repoServer.UserProfile, Sonatype.ext.FormPanel, {
  isValid : function() {
    return this.form.isValid();
  },

  loadGravatarPicture : function(form, action, receivedData) {
    Ext.getDom('user-picture-' + receivedData.userId).src = 'http://www.gravatar.com/avatar/' + Ext.util.MD5(Sonatype.utils.lowercase(receivedData.email.trim()));
  }
});

Sonatype.Events.addListener('userAdminViewInit', function(views) {
  views.push({
    name : 'Summary',
    item : Sonatype.repoServer.UserProfile
  });
});
Sonatype.Events.addListener('userProfileInit', function(views) {
  views.push({
    name : 'Summary',
    item : Sonatype.repoServer.UserProfile
  });
});

