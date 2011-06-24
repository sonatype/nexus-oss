/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
/*
 * Role Edit/Create panel layout and controller
 */

Sonatype.repoServer.RoleEditPanel = function(config) {
  var config = config || {};
  var defaultConfig = {
    title : 'Roles'
  };
  Ext.apply(this, config, defaultConfig);

  this.sp = Sonatype.lib.Permissions;

  this.externalMappingStore = new Ext.data.JsonStore({
        root : 'data',
        id : 'defaultRole.roleId',
        fields : [{
              name : 'defaultRole'
            }, {
              name : 'mappedRoles'
            }],
        url : Sonatype.config.repos.urls.externalRolesAll,
        autoLoad : false
      });

  this.sourceStore = new Ext.data.JsonStore({
        root : 'data',
        id : 'roleHint',
        autoLoad : false,
        url : Sonatype.config.repos.urls.userLocators,
        sortInfo : {
          field : 'description',
          direction : 'ASC'
        },
        fields : [{
              name : 'roleHint'
            }, {
              name : 'description',
              sortType : Ext.data.SortTypes.asUCString
            }],
        listeners : {
          load : {
            fn : function(store, records, options) {
              for (var i = 0; i < records.length; i++)
              {
                var rec = records[i];
                var v = rec.data.roleHint;
                if (v == 'allConfigured' || v == 'mappedExternal' || v == 'default')
                {
                  store.remove(rec);
                }
              }

              if (this.sp.checkPermission('security:roles', this.sp.CREATE) && store.getCount() > 0 && this.toolbarAddButton.menu.items.length == 1)
              {
                this.toolbarAddButton.menu.add({
                      text : 'External Role Mapping',
                      handler : this.mapExternalRoles,
                      scope : this
                    });
              }
            },
            scope : this
          }
        }
      });

  Sonatype.Events.on('roleAddMenuInit', this.onAddMenuInit, this);

  Sonatype.repoServer.RoleEditPanel.superclass.constructor.call(this, {
        addMenuInitEvent : 'roleAddMenuInit',
        deleteButton : this.sp.checkPermission('security:roles', this.sp.DELETE),
        rowClickEvent : 'roleViewInit',
        url : Sonatype.config.repos.urls.roles,
        dataAutoLoad : true,
        dataId : 'id',
        dataBookmark : 'id',
        dataStores : [this.sourceStore, this.externalMappingStore],
        columns : [{
              name : 'name',
              sortType : Ext.data.SortTypes.asUCString,
              header : 'Name',
              width : 200,
              renderer : function(value, meta, rec, index) {
                return rec.data.mapping ? ('<b>' + value + '</b') : value;
              }
            }, {
              name : 'id'
            }, {
              name : 'resourceURI'
            }, {
              name : 'mapping',
              header : 'Realm',
              width : 100,
              mapping : 'id',
              convert : this.convertMapping.createDelegate(this)
            }, {
              name : 'description',
              header : 'Description',
              width : 175,
              autoExpand : true
            }, {
              name : 'roles'
            }, {
              name : 'privileges'
            }, {
              name : 'userManaged'
            }],
        listeners : {
          beforedestroy : {
            fn : function() {
              Sonatype.Events.un('roleAddMenuInit', this.onAddMenuInit, this);
            },
            scope : this
          }
        }
      });
};

Ext.extend(Sonatype.repoServer.RoleEditPanel, Sonatype.panels.GridViewer, {
      convertMapping : function(value, parent) {
        var mappingRec = this.externalMappingStore.getById(value);
        if (mappingRec)
        {
          var mappings = mappingRec.data.mappedRoles;
          var s = '';
          for (var i = 0; i < mappings.length; i++)
          {
            if (s)
              s += ', ';
            s += mappings[i].source;
          }
          return s;
        }
        else
        {
          return 'Nexus';
        }
      },
      onAddMenuInit : function(menu) {
        menu.add('-');
        if (this.sp.checkPermission('security:roles', this.sp.CREATE))
        {
          menu.add({
                text : 'Nexus Role',
                autoCreateNewRecord : true,
                handler : function(container, rec, item, e) {
                  rec.beginEdit();
                  rec.set('source', 'default');
                  rec.commit();
                  rec.endEdit();
                },
                scope : this
              });
        }
      },

      mapExternalRoles : function() {
        new Sonatype.repoServer.ExternapRoleMappingPopup({
              hostPanel : this,
              sourceStore : this.sourceStore
            }).show();
      }
    });

Sonatype.repoServer.ExternapRoleMappingPopup = function(config) {
  var config = config || {};
  var defaultConfig = {
    title : 'Map External Role'
  };
  Ext.apply(this, config, defaultConfig);

  this.roleStore = new Ext.data.JsonStore({
        root : 'data',
        id : 'roleId',
        fields : [{
              name : 'roleId'
            }, {
              name : 'source'
            }, {
              name : 'name',
              sortType : Ext.data.SortTypes.asUCString
            }],
        sortInfo : {
          field : 'name',
          direction : 'asc'
        },
        url : Sonatype.config.repos.urls.plexusRolesAll,
        autoLoad : true
      });

  Sonatype.repoServer.ExternapRoleMappingPopup.superclass.constructor.call(this, {
        closable : true,
        autoWidth : false,
        width : 400,
        autoHeight : true,
        modal : true,
        constrain : true,
        resizable : false,
        draggable : false,
        items : [{
              xtype : 'form',
              layoutConfig : {
                labelSeparator : ''
              },
              labelWidth : 60,
              frame : true,
              defaultType : 'textfield',
              monitorValid : true,
              items : [{
                    xtype : 'combo',
                    fieldLabel : 'Realm',
                    itemCls : 'required-field',
                    helpText : 'Security realm to select roles from.',
                    name : 'source',
                    anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                    width : 200,
                    store : this.sourceStore,
                    displayField : 'description',
                    valueField : 'roleHint',
                    editable : false,
                    forceSelection : true,
                    mode : 'local',
                    triggerAction : 'all',
                    emptyText : 'Select...',
                    selectOnFocus : true,
                    allowBlank : false,
                    listeners : {
                      select : {
                        fn : this.onSourceSelect,
                        scope : this
                      }
                    }
                  }, {
                    xtype : 'combo',
                    fieldLabel : 'Role',
                    itemCls : 'required-field',
                    helpText : 'External role to map.',
                    name : 'roleId',
                    anchor : Sonatype.view.FIELD_OFFSET_WITH_SCROLL,
                    width : 200,
                    store : this.roleStore,
                    displayField : 'name',
                    valueField : 'roleId',
                    editable : true,
                    forceSelection : false,
                    mode : 'local',
                    triggerAction : 'all',
                    lastQuery : '',
                    emptyText : 'Select...',
                    selectOnFocus : true,
                    allowBlank : false
                  }],
              buttons : [{
                    text : 'Create Mapping',
                    formBind : true,
                    handler : this.validateRoleMapping,
                    scope : this,
                    disabled : true
                  }, {
                    text : 'Cancel',
                    formBind : false,
                    handler : function(button, e) {
                      this.close();
                    },
                    scope : this
                  }]
            }]
      });
};

Ext.extend(Sonatype.repoServer.ExternapRoleMappingPopup, Ext.Window, {
      onSourceSelect : function(combo, rec, index) {
        var roleCombo = this.find('name', 'roleId')[0];
        roleCombo.clearValue();
        roleCombo.store.filter('source', rec.data.roleHint);
      },

      validateRoleMapping : function(button, e) {
        var roleId = this.find('name', 'roleId')[0].getValue();
        var sourceId = this.find('name', 'source')[0].getValue();

        Ext.Ajax.request({
              url : Sonatype.config.servicePath + '/external_role_map/' + sourceId + '/' + roleId,
              callback : function(options, isSuccess, response) {
                if (isSuccess)
                {
                  this.createRoleMapping();
                }
                else
                {
                  this.find('name', 'roleId')[0].markInvalid('Role not found!');
                }
              },
              scope : this,
              method : 'GET',
              suppressStatus : '404'
            });
      },
      createRoleMapping : function() {
        if (this.hostPanel)
        {
          var roleId = this.find('name', 'roleId')[0].getValue();
          var roleRec = this.roleStore.getById(roleId);
          var sourceId = this.find('name', 'source')[0].getValue();
          var handler = this.hostPanel.addActionHandler.createDelegate(this.hostPanel, [function(rec, item, e) {
                    rec.beginEdit();
                    rec.set('source', sourceId);
                    rec.set('mapping', sourceId);
                    rec.commit();
                    rec.endEdit();
                  }, {
                    autoCreateNewRecord : true,
                    text : "Role Mapping"
                  }], 0);
          handler();

          var name = roleRec == null ? roleId :roleRec.data.name; 
          var defaultData = {
            id : roleId,
            name : name,
            description : 'External mapping for ' + name + ' (' + sourceId + ')'
          };

          this.hostPanel.cardPanel.getLayout().activeItem.find('name', 'id')[0].disable();
          this.hostPanel.cardPanel.getLayout().activeItem.items.get(0).presetData = defaultData;
          this.hostPanel.cardPanel.getLayout().activeItem.items.get(0).resetHandler();

          this.close();
        }
      }
    });

Sonatype.repoServer.DefaultRoleEditor = function(config) {
  var config = config || {};
  var defaultConfig = {
    uri : Sonatype.config.repos.urls.roles,
    labelWidth : 100,
    referenceData : Sonatype.repoServer.referenceData.roles,
    dataModifiers : {
      load : {
        id : function(value, srcObj, fpanel) {
          fpanel.find('name', 'roleManager')[0].setHiddenRoleIds(value, true);
          return value;
        },
        roles : function(arr, srcObj, fpanel) {
          fpanel.find('name', 'roleManager')[0].setSelectedRoleIds(arr, true);
          return arr;
        },
        privileges : function(arr, srcObj, fpanel) {
          fpanel.find('name', 'roleManager')[0].setSelectedPrivilegeIds(arr, true);
          return arr;
        }
      },
      submit : {
        roles : function(value, fpanel) {
          return fpanel.find('name', 'roleManager')[0].getSelectedRoleIds();
        },
        privileges : function(value, fpanel) {
          return fpanel.find('name', 'roleManager')[0].getSelectedPrivilegeIds();
        },
        sessionTimeout : function() {
          return 60;
        }
      }
    }
  };
  Ext.apply(this, config, defaultConfig);

  var ht = Sonatype.repoServer.resources.help.roles;

  this.COMBO_WIDTH = 300;

  this.checkPayload();

  var items = [{
        name : 'internalResourceHeader',
        xtype : 'panel',
        layout : 'table',
        hidden : true,
        style : 'font-size: 18px; padding: 5px 0px 5px 15px',
        items : [{
              html : '<b>This is an internal Nexus resource which cannot be edited or deleted.</b><br><hr/>'
            }]
      }, {
        xtype : 'textfield',
        fieldLabel : 'Role Id',
        itemCls : 'required-field',
        labelStyle : 'margin-left: 15px; width: 185px;',
        helpText : ht.id,
        name : 'id',
        allowBlank : false,
        width : this.COMBO_WIDTH
      }, {
        xtype : 'textfield',
        fieldLabel : 'Name',
        itemCls : 'required-field',
        labelStyle : 'margin-left: 15px; width: 185px;',
        helpText : ht.name,
        name : 'name',
        allowBlank : false,
        width : this.COMBO_WIDTH
      }, {
        xtype : 'textfield',
        fieldLabel : 'Description',
        labelStyle : 'margin-left: 15px; width: 185px;',
        helpText : ht.description,
        name : 'description',
        allowBlank : true,
        width : this.COMBO_WIDTH
      },{
        xtype : 'rolemanager',
        id : 'roleManagerId',
        name : 'roleManager',
        height : 200,
        width : 490,
        style : 'margin-left: 15px;margin-top: 10px;border: 1px solid #B5B8C8;'
      }];

  Sonatype.repoServer.DefaultUserEditor.superclass.constructor.call(this, {
        items : items,
        listeners : {
          submit : {
            fn : this.submitHandler,
            scope : this
          },
          load : {
            fn : this.loadHandler,
            scope : this
          }
        }
      });
};

Ext.extend(Sonatype.repoServer.DefaultRoleEditor, Sonatype.ext.FormPanel, {
      resetHandler : function(button, event) {
        this.el.mask('Loading...', 'x-mask-loading');
        Sonatype.repoServer.DefaultRoleEditor.superclass.resetHandler.call(this, button, event);

        if (this.presetData)
        {
          this.getForm().setValues(this.presetData);
        }

        this.el.unmask();
      },
      loadHandler : function() {
        if (!this.payload.data.userManaged)
        {
          this.find('name', 'internalResourceHeader')[0].setVisible(true);
          this.find('name', 'id')[0].disable();
          this.find('name', 'name')[0].disable();
          this.find('name', 'description')[0].disable();
          this.find('name', 'roleManager')[0].disable();
          for (var i = 0; i < this.buttons.length; i++)
          {
            this.buttons[i].disable();
          }
        }
      },
      loadData : function(form, action, receivedData) {
        Sonatype.repoServer.DefaultRoleEditor.superclass.loadData.call(this);

        if (this.presetData)
        {
          this.getForm().setValues(this.presetData);
        }
      },
      isValid : function() {
        return this.form.isValid() && this.find('name', 'roleManager')[0].validate();
      },
      submitHandler : function(form, action, receivedData) {
        receivedData.mapping = this.payload.data.mapping;
      },
      validationModifiers : { 'roles' : function(error,panel) { Ext.getCmp('roleManagerId').markInvalid(error.msg); } }
    });

Sonatype.Events.addListener('roleViewInit', function(cardPanel, rec, gridPanel) {
      var config = {
        payload : rec,
        tabTitle : 'Configuration'
      };

      if (rec.data.userManaged == false)
      {}
      else
      {}

      cardPanel.add(new Sonatype.repoServer.DefaultRoleEditor(config));
    });
