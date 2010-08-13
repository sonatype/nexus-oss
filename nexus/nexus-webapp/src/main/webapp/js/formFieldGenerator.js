FormFieldGenerator = function(panelId, fieldSetName, fieldNamePrefix, typeStore, repoStore, groupStore, repoOrGroupStore) {
  var allTypes = [];

  allTypes[0] = {
    xtype : 'fieldset',
    id : panelId + '_emptyItem',
    checkboxToggle : false,
    title : fieldSetName,
    anchor : Sonatype.view.FIELDSET_OFFSET,
    collapsible : false,
    autoHeight : true,
    layoutConfig : {
      labelSeparator : ''
    }
  };

  // Now add the dynamic content
  typeStore.each(function(item, i, len) {
        var items = [];
        if (item.data.formFields.length > 0)
        {
          for (var j = 0; j < item.data.formFields.length; j++)
          {
            var curRec = item.data.formFields[j];
            // Note that each item is disabled initially, this is because
            // the select handler for the capabilityType
            // combo box handles enabling/disabling as necessary, so each
            // inactive card isn't also included in the form
            if (curRec.type == 'string')
            {
              items[j] = {
                xtype : 'textfield',
                fieldLabel : curRec.label,
                itemCls : curRec.required ? 'required-field' : '',
                helpText : curRec.helpText,
                name : fieldNamePrefix + curRec.id,
                allowBlank : curRec.required ? false : true,
                disabled : true,
                width : this.COMBO_WIDTH,
                regex : curRec.regexValidation ? new RegExp(curRec.regexValidation) : null
              };
            }
            else if (curRec.type == 'number')
            {
              items[j] = {
                xtype : 'numberfield',
                fieldLabel : curRec.label,
                itemCls : curRec.required ? 'required-field' : '',
                helpText : curRec.helpText,
                name : fieldNamePrefix + curRec.id,
                allowBlank : curRec.required ? false : true,
                disabled : true,
                width : this.COMBO_WIDTH,
                regex : curRec.regexValidation ? new RegExp(curRec.regexValidation) : null
              };
            }
            else if (curRec.type == 'checkbox')
            {
              items[j] = {
                xtype : 'checkbox',
                fieldLabel : curRec.label,
                helpText : curRec.helpText,
                name : fieldNamePrefix + curRec.id,
                disabled : true
              };
            }
            else if (curRec.type == 'date')
            {
              items[j] = {
                xtype : 'datefield',
                fieldLabel : curRec.label,
                itemCls : curRec.required ? 'required-field' : '',
                helpText : curRec.helpText,
                name : fieldNamePrefix + curRec.id,
                allowBlank : curRec.required ? false : true,
                disabled : true,
                value : new Date()
              };
            }
            else if (curRec.type == 'repo')
            {
              items[j] = {
                xtype : 'combo',
                fieldLabel : curRec.label,
                itemCls : curRec.required ? 'required-field' : '',
                helpText : curRec.helpText,
                name : fieldNamePrefix + curRec.id,
                store : repoStore,
                displayField : 'name',
                valueField : 'id',
                editable : false,
                forceSelection : true,
                mode : 'local',
                triggerAction : 'all',
                emptyText : 'Select...',
                selectOnFocus : true,
                allowBlank : curRec.required ? false : true,
                disabled : true,
                width : this.COMBO_WIDTH,
                minListWidth : this.COMBO_WIDTH
              };
            }
            else if (curRec.type == 'group')
            {
              items[j] = {
                xtype : 'combo',
                fieldLabel : curRec.label,
                itemCls : curRec.required ? 'required-field' : '',
                helpText : curRec.helpText,
                name : fieldNamePrefix + curRec.id,
                store : groupStore,
                displayField : 'name',
                valueField : 'id',
                editable : false,
                forceSelection : true,
                mode : 'local',
                triggerAction : 'all',
                emptyText : 'Select...',
                selectOnFocus : true,
                allowBlank : curRec.required ? false : true,
                disabled : true,
                width : this.COMBO_WIDTH,
                minListWidth : this.COMBO_WIDTH
              };
            }
            else if (curRec.type == 'repo-or-group')
            {
              items[j] = {
                xtype : 'combo',
                fieldLabel : curRec.label,
                itemCls : curRec.required ? 'required-field' : '',
                helpText : curRec.helpText,
                name : fieldNamePrefix + curRec.id,
                store : repoOrGroupStore,
                displayField : 'name',
                valueField : 'id',
                editable : false,
                forceSelection : true,
                mode : 'local',
                triggerAction : 'all',
                emptyText : 'Select...',
                selectOnFocus : true,
                allowBlank : curRec.required ? false : true,
                disabled : true,
                width : this.COMBO_WIDTH,
                minListWidth : this.COMBO_WIDTH
              };
            }

            allTypes[allTypes.length] = {
              xtype : 'fieldset',
              id : panelId + '_' + item.data.id,
              checkboxToggle : false,
              title : fieldSetName,
              anchor : Sonatype.view.FIELDSET_OFFSET,
              collapsible : false,
              autoHeight : true,
              labelWidth : 175,
              layoutConfig : {
                labelSeparator : ''
              },
              items : items
            };
          }
        }
        else
        {
          allTypes[allTypes.length] = {
            xtype : 'fieldset',
            id : panelId + '_' + item.data.id,
            checkboxToggle : false,
            title : fieldSetName,
            anchor : Sonatype.view.FIELDSET_OFFSET,
            collapsible : false,
            autoHeight : true,
            labelWidth : 175,
            layoutConfig : {
              labelSeparator : ''
            }
          };
        }
      }, this);

  return allTypes;
};