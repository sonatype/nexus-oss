/**
 * A RoleSelectorGrid is used to display the roles and privileges (optional) in a grid with checkboxes for simple selection
 * The grid supports pagination
 *  
 * showPrivileges - boolean flag, if true will show the privileges along with the roles 
 * selectedRoleIds - role ids that should show as selected 
 * selectedPrivilegeIds - privilege ids that should show as selected
 */
RoleSelectorGrid = function(config) {
  var config = config || {};

  var defaultConfig = {
    title : 'Manage Roles' + (this.showPrivileges ? ' and Privileges' : ''),
    trackMouseOver : false,
    loadMask : true,
    showPrivileges : true,
    externalRoleFilter : false,
    externalUserId : null,
    selectedRoleIds : [],
    selectedPrivilegeIds : [],
    viewConfig : {
      forceFit : true
    }
  };
  //apply the config and defaults to 'this'
  Ext.apply(this, config, defaultConfig);

  //we have a predefined proxy here, so that we can post json data
  //and it is stored in this. so that we can easily change the json
  //params
  this.storeProxy = new Ext.data.HttpProxy({
        method : 'POST',
        url : Sonatype.config.servicePath + '/rolesAndPrivs',
        jsonData : {
          data : {
            name : null,
            noPrivileges : !this.showPrivileges,
            noRoles : false,
            selectedRoleIds : [],
            selectedPrivilegeIds : [],
            userId : this.externalUserId
          }
        }
      });

  //our remote resource, that will be supplying the paginated content
  this.store = new Ext.data.JsonStore({
        root : 'data',
        id : 'id',
        totalProperty : 'totalCount',
        remoteSort : true,
        proxy : this.storeProxy,
        fields : [{
              name : 'id'
            }, {
              name : 'type'
            }, {
              name : 'name',
              sortType : Ext.data.SortTypes.asUCString
            }, {
              name : 'description'
            }, {
              name : 'external'
            }],
        listeners : {
          load : {
            fn : function(store, records, options) {
              this.applySelection();
            },
            scope : this
          }
        }
      });

  if (this.showPrivileges)
  {
    this.store.setDefaultSort('type', 'desc');
  }
  else
  {
    this.store.setDefaultSort('name', 'asc');
  }

  //add a checkbox selection model to the grid
  this.sm = new Ext.grid.CheckboxSelectionModel({
        listeners : {
          rowselect : {
            fn : function(sm, idx, rec) {
              if (rec.get('type') == 'role')
              {
                //external roles, we dont want to add to our mem list, as they aren't saved that way
                if (rec.get('external') == true)
                {
                  return;
                }

                var found = false;
                for (var i = 0; i < this.selectedRoleIds.length; i++)
                {
                  if (this.selectedRoleIds[i] == rec.get('id'))
                  {
                    found = true;
                    break;
                  }
                }

                if (!found)
                {
                  this.selectedRoleIds.push(rec.get('id'));
                }
              }
              else
              {
                var found = false;
                for (var i = 0; i < this.selectedPrivilegeIds.length; i++)
                {
                  if (this.selectedPrivilegeIds[i] == rec.get('id'))
                  {
                    found = true;
                    break;
                  }
                }

                if (!found)
                {
                  this.selectedPrivilegeIds.push(rec.get('id'));
                }
              }

              this.clearValidation();
            },
            scope : this
          },
          rowdeselect : {
            fn : function(sm, idx, rec) {
              if (rec.get('type') == 'role')
              {
                if (rec.get('external') == true)
                {
                  sm.selectRecords([rec], true);
                }
                for (var i = 0; i < this.selectedRoleIds.length; i++)
                {
                  if (this.selectedRoleIds[i] == rec.get('id'))
                  {
                    this.selectedRoleIds.remove(this.selectedRoleIds[i]);
                    break;
                  }
                }
              }
              else
              {
                var found = false;
                for (var i = 0; i < this.selectedPrivilegeIds.length; i++)
                {
                  if (this.selectedPrivilegeIds[i] == rec.get('id'))
                  {
                    this.selectedPrivilegeIds.remove(this.selectedPrivilegeIds[i])
                    break;
                  }
                }
              }

              this.validate();
            },
            scope : this
          }
        }
      });

  var columns = [this.sm];

  if (this.showPrivileges)
  {
    columns.push({
          id : 'type',
          width : 80,
          header : 'Type',
          dataIndex : 'type',
          sortable : true,
          renderer : Sonatype.utils.upperFirstCharLowerRest
        });
  }

  columns.push({
        id : 'name',
        width : 200,
        header : 'Name',
        dataIndex : 'name',
        sortable : true,
        renderer : this.externalRoleRenderer
      }, {
        id : 'description',
        width : 400,
        header : 'Description',
        dataIndex : 'description',
        sortable : true,
        renderer : this.externalRoleRenderer
      });

  //columns in the grid
  this.cm = new Ext.grid.ColumnModel({
        columns : columns
      });

  this.textFilter = new Ext.form.TextField({
        emptyText : 'Enter filter text...'
      });

  this.selectedFilter = new Ext.Button({
        text : 'Selected Only',
        enableToggle : true,
        tooltip : 'Add filter that will only show items selected',
        pressed : false
      });

  this.rolesFilter = new Ext.Button({
        text : 'Roles',
        enableToggle : true,
        tooltip : 'Add filter that will show roles',
        pressed : true
      });
      
  this.externalRolesFilter = new Ext.Button({
        text : 'External Roles',
        enableToggle : true,
        tooltip : 'Add filter that will show external roles',
        pressed : true
      });

  this.privilegesFilter = new Ext.Button({
        text : 'Privileges',
        enableToggle : true,
        tooltip : 'Add filter that will show privileges',
        pressed : true
      });    

  // toolbar at top
  this.tbar = ['Filter: ', ' ', this.textFilter, '-', this.selectedFilter, '-'];

  if (this.includePrivileges)
  {
    this.tbar.push(this.rolesFilter, '-');
  }

  if (this.externalRoleFilter)
  {
    this.tbar.push(this.externalRolesFilter, '-');
  }

  if (this.includePrivileges)
  {
    this.tbar.push(this.privilegesFilter, '-');
  }

  this.tbar.push('->', '-', {
        text : 'Apply Filter',
        tooltip : 'Apply the filter parameter(s)',
        handler : this.applyFilter,
        scope : this
      }, '-', {
        text : 'Reset Filter',
        tooltip : 'Reset the filter to default selections',
        handler : this.resetFilter,
        scope : this
      }, '-');

  // paging bar on the bottom
  this.bbar = new Ext.PagingToolbar({
        pageSize : 25,
        store : this.store,
        displayInfo : true,
        displayMsg : 'Displaying roles' + (this.showPrivileges ? ' and privileges' : '') + ' {0} - {1} of {2}',
        emptyMsg : 'No roles' + (this.showPrivileges ? ' or privileges' : '') + ' to display'
      });

  //constructor call, adding the panel setup here
  RoleSelectorGrid.superclass.constructor.call(this, {});
};

Ext.extend(RoleSelectorGrid, Ext.grid.GridPanel, {
      //implement local onRender to load the first page of store
      onRender : function() {
        RoleSelectorGrid.superclass.onRender.apply(this, arguments);
        this.store.load({
              params : {
                start : 0,
                limit : 25
              }
            });
      },
      externalRoleRenderer : function(value, metadata, record, rowIndex, colIndex, store) {
        if (record.get('external'))
        {
          return '<b>' + value + '</b>';
        }

        return value;
      },
      applySelection : function() {
        //suspend the events here, we dont want to update our selected items in mem, only in the grid
        this.getSelectionModel().suspendEvents();
        this.getSelectionModel().clearSelections();
        this.getSelectionModel().resumeEvents();
        var records = this.store.getRange();

        for (var i = 0; i < records.length; i++)
        {
          if (records[i].get('type') == 'role' && (records[i].get('external') == true || this.selectedRoleIds.indexOf(records[i].get('id')) != -1))
          {
            this.getSelectionModel().selectRecords([records[i]], true);
          }
          else if (records[i].get('type') == 'privilege' && this.selectedPrivilegeIds.indexOf(records[i].get('id')) != -1)
          {
            this.getSelectionModel().selectRecords([records[i]], true);
          }
        }
      },
      setSelectedRoleIds : function(roleIds, reload) {
        this.selectedRoleIds = [];

        if (roleIds != null)
        {
          if (!Ext.isArray(roleIds))
          {
            roleIds = [roleIds];
          }

          for (var i = 0; i < roleIds.length; i++)
          {
            if (typeof(roleIds[i]) != 'string')
            {
              if (!roleIds[i].source || roleIds[i].source == 'default')
              {
                if (roleIds[i].id)
                {
                  this.selectedRoleIds.push(roleIds[i].id);
                }
                else if (roleIds[i].roleId)
                {
                  this.selectedRoleIds.push(roleIds[i].roleId);
                }
              }
            }
            else
            {
              this.selectedRoleIds.push(roleIds[i]);
            }
          }
        }

        if (this.selectedRoleIds.length == 0)
        {
          this.noRolesOnStart = true;
        }
        else
        {
          this.noRolesOnStart = false;
        }

        if (reload)
        {
          this.applyFilter();
        }
        else
        {
          this.applySelection();
        }
      },
      setSelectedPrivilegeIds : function(privilegeIds, reload) {
        this.selectedPrivilegeIds = [];

        if (privilegeIds != null)
        {
          if (!Ext.isArray(privilegeIds))
          {
            privilegeIds = [privilegeIds];
          }

          for (var i = 0; i < privilegeIds.length; i++)
          {
            if (typeof(privilegeIds[i]) != 'string')
            {
              this.selectedPrivilegeIds.push(privilegeIds[i].id);
            }
            else
            {
              this.selectedPrivilegeIds.push(privilegeIds[i]);
            }
          }
        }

        if (reload)
        {
          this.applyFilter();
        }
        else
        {
          this.applySelection();
        }
      },
      getSelectedRoleIds : function() {
        return this.selectedRoleIds;
      },
      getSelectedPrivilegeIds : function() {
        return this.selectedPrivilegeIds;
      },
      applyFilter : function() {
        this.storeProxy.conn.jsonData.data.name = this.textFilter.getValue();

        if (this.selectedFilter.pressed)
        {
          this.storeProxy.conn.jsonData.data.selectedRoleIds = this.selectedRoleIds;
          this.storeProxy.conn.jsonData.data.selectedPrivilegeIds = this.selectedPrivilegeIds;
        }
        else
        {
          this.storeProxy.conn.jsonData.data.selectedRoleIds = [];
          this.storeProxy.conn.jsonData.data.selectedPrivilegeIds = [];
        }

        if (!this.rolesFilter.pressed)
        {
          this.storeProxy.conn.jsonData.data.noRoles = true;
        }
        else
        {
          this.storeProxy.conn.jsonData.data.noRoles = false;
        }

        if (!this.externalRolesFilter.pressed)
        {
          this.storeProxy.conn.jsonData.data.noExternalRoles = true;
        }
        else
        {
          this.storeProxy.conn.jsonData.data.noExternalRoles = false;
        }

        if (!this.privilegesFilter.pressed || !this.showPrivileges)
        {
          this.storeProxy.conn.jsonData.data.noPrivileges = true;
        }
        else
        {
          this.storeProxy.conn.jsonData.data.noPrivileges = false;
        }

        this.store.load({
              params : {
                start : 0,
                limit : 25
              }
            });
      },
      resetFilter : function() {
        this.textFilter.setValue(null);
        this.selectedFilter.toggle(false);

        this.storeProxy.conn.jsonData.data.name = null;
        this.storeProxy.conn.jsonData.data.selectedRoleIds = [];
        this.storeProxy.conn.jsonData.data.selectedPrivilegeIds = [];
        this.storeProxy.conn.jsonData.data.noRoles = false;
        this.storeProxy.conn.jsonData.data.noExternalRoles = false;
        this.storeProxy.conn.jsonData.data.noPrivileges = !this.showPrivileges;

        this.store.load({
              params : {
                start : 0,
                limit : 25
              }
            });
      },
      getRoleNameFromId : function(id) {
        var rec = this.store.getById(id);

        if (rec)
        {
          return rec.get('name');
        }

        return id;
      },
      validate : function() {
        if (this.selectedRoleIds.length == 0 && this.selectedPrivilegeIds.length == 0)
        {
          if (!this.validationError)
          {
            this.validationError = new Ext.Toolbar.TextItem('<b><font color="red">&nbsp;&nbsp;You must select at least 1 role' + (this.showPrivileges ? ' or privilege' : '') + '!</font></b>');
            this.getBottomToolbar().add(this.validationError);
          }
          this.getEl().child('.x-panel-body').setStyle({
                border : '1px solid #dd7870'
              });
          this.validationError.setVisible(true);
          return false;
        }

        if (this.validationError)
        {
          this.validationError.setVisible(false);
        }
        return true;
      },
      clearValidation : function() {
        if (this.validationError)
        {
          this.validationError.setVisible(false);
        }

        this.getEl().child('.x-panel-body').setStyle({
              border : '1px solid #B5B8C8'
            });
      },
      setExternalUserId : function(userId, reload) {
        this.externalUserId = userId;
        this.storeProxy.conn.jsonData.data.userId = this.externalUserId;
        if (reload)
        {
          this.applyFilter();
        }
      }
    });

Ext.reg('roleselector', RoleSelectorGrid);