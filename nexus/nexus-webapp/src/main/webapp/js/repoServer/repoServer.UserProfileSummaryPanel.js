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
  var config = config || {};
  var defaultConfig = {
//    title : 'User Profile'
    border : false,
    frame : false
  };
  Ext.apply(this, config, defaultConfig);

  this.listeners = {
    'beforerender' : {
      fn : function() {
        this.loadUserProfile();
      },
      scope : this
    }
  };

  this.formPanel = new Ext.form.FormPanel({
        id : 'user-profile-summary-form',
        region : 'center',
        trackResetOnLoad : true,
        autoScroll : true,
        border : false,
        frame : false,
        width : '100%',
        collapsible : false,
        collapsed : false,
        labelWidth : 175,
        layoutConfig : {
          labelSeparator : ''
        },
        items : [{
          xtype : 'fieldset',
          title : 'Details',
          autoHeight : true,
          layoutConfig : {
            labelSeparator : ''
          },

          items : [{
              xtype : 'box',
              anchor : '',
              isFormField : true,
              fieldLabel : 'Picture',
              hideLabel : true,
              style : 'position: absolute; right: 15px',
              autoEl : {
                tag : 'div',
                children : [{
                      tag : 'img',
                      qtip : 'Profile picture from gravatar (http://www.gravatar.com/)',
                      id : 'user-picture'
                    }]
              }
            }, {
              xtype : 'textfield',
              fieldLabel : 'Username',
              name : 'userId',
              tabIndex : 1,
              width : 180,
              allowBlank : false,
              disabled : true
            }, {
              xtype : 'textfield',
              fieldLabel : 'Full Name',
              name : 'name',
              tabIndex : 2,
              width : 180,
              allowBlank : false,
              itemCls : 'required-field'
            }, {
              xtype : 'textfield',
              fieldLabel : 'Email',
              name : 'email',
              tabIndex : 3,
              width : 180,
              allowBlank : false,
              itemCls : 'required-field'
            }, {
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
            }]
          }],
        keys : {
          key : Ext.EventObject.ENTER,
          fn : this.saveUserProfile,
          scope : this
        },
        buttons : [{
              id : 'edit-account-btn',
              text : 'Save',
              tabIndex : 7,
              formBind : true,
              scope : this,
              handler : this.saveUserProfile
            }, {
              text : 'Reset',
              handler : this.resetHandler,
              scope : this
            }]
        }
  );

  Sonatype.repoServer.UserProfile.superclass.constructor.call(this, {
        autoScroll : false,
        layout : 'border',
        items : [this.formPanel]
      });

};

Ext.extend(Sonatype.repoServer.UserProfile, Ext.Panel, {
      resetHandler : function() {
        this.loadUserProfile();
      },
      saveUserProfile : function() {
        var accountIdVal = this.formPanel.find('name', 'userId')[0].getValue();
        var accountNameVal = this.formPanel.find('name', 'name')[0].getValue();
        var accountEmailVal = this.formPanel.find('name', 'email')[0].getValue();

        this.el.mask('Updating...');
        Ext.Ajax.request({
              callback : function(options, isSuccess, response) {
                if (!isSuccess)
                {
                  this.el.unmask();
                  Sonatype.utils.connectionError(response, 'Could not save your user details.');
                }
                else
                {
                  this.el.unmask();
                  Ext.getDom('user-picture').src = 'http://www.gravatar.com/avatar/' + Ext.util.MD5(Sonatype.utils.lowercase(this.formPanel.find('name', 'email')[0].getValue().trim()));
                }
              },
              scope : this,
              method : 'PUT',
              url : Sonatype.config.servicePath + '/user_account/' + accountIdVal,
              jsonData : '{"data":{"id":"' + accountIdVal + '","name":"' + accountNameVal + '","email":"' + accountEmailVal + '"}}',
              suppressStatus : 400
            });
      },
      loadUserProfile : function() {
        Ext.Ajax.request({
              callback : function(options, isSuccess, response) {
                if (!isSuccess)
                {
                  Sonatype.utils.connectionError(response, 'Unable to load user account information.', false, options.options);
                  return;
                }
                var result = Ext.util.JSON.decode(response.responseText);
                this.formPanel.find('name', 'userId')[0].setValue(result.data.id);
                this.formPanel.find('name', 'name')[0].setValue(result.data.name);
                this.formPanel.find('name', 'email')[0].setValue(result.data.email);

                Ext.getDom('user-picture').src = 'http://www.gravatar.com/avatar/' + Ext.util.MD5(Sonatype.utils.lowercase(result.data.email.trim()));
              },
              scope : this,
              method : 'GET',
              url : Sonatype.config.servicePath + '/user_account/' + Sonatype.user.curr.username
            });

        var field = this.formPanel.find('name', 'userId')[0];
        field.focus(true, 100);
      }
    });
