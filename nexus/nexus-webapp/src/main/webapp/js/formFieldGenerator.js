FormFieldGenerator = function(panelId, fieldSetName, fieldNamePrefix, typeStore, repoStore, groupStore, repoOrGroupStore, customTypes, width) {
  var allTypes = [];

  if (!width)
  {
    width = 300;
  }

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
                width : width,
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
                width : width,
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
                width : width,
                minListWidth : width
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
                width : width,
                minListWidth : width
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
                width : width,
                minListWidth : width
              };
            }
            else if (customTypes && customTypes[curRec.type])
            {
              items[j] = customTypes[curRec.type].createItem.call(this, curRec, fieldNamePrefix, width);
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

FormFieldExporter = function(formPanel, panelIdSuffix, formFieldPrefix, customTypes) {
  var outputArr = [];

  var formFieldPanel = formPanel.findById(formPanel.id + panelIdSuffix);
  var i = 0;
  // These are dynamic fields here, so some pretty straightforward generic
  // logic below
  formFieldPanel.getLayout().activeItem.items.each(function(item, i, len) {
        var value;

        if (item.xtype == 'datefield')
        {
          // long representation is used, not actual date
          // force to a string, as that is what the current api requires
          value = '' + item.getValue().getTime();
        }
        else if (item.xtype == 'textfield')
        {
          value = item.getValue();
        }
        else if (item.xtype == 'numberfield')
        {
          // force to a string, as that is what the current api requires
          value = '' + item.getValue();
        }
        else if (item.xtype == 'checkbox')
        {
          value = '' + item.getValue();
        }
        else if (item.xtype == 'combo')
        {
          value = item.getValue();
        }
        else if (customTypes && customTypes[item.xtype])
        {
          value = customTypes[item.xtype].retrieveValue.call(item, item);
        }

        outputArr[i] = {
          key : item.getName().substring(formFieldPrefix.length),
          value : value
        };
        i++;
      }, formFieldPanel.getLayout().activeItem);

  return outputArr;
};
FormFieldImporter = function(jsonObject, formPanel, formFieldPrefix, customTypes) {
  // Maps the incoming json properties to the generic component
  for (var i = 0; i < jsonObject.properties.length; i++)
  {
    var formFields = formPanel.find('name', formFieldPrefix + jsonObject.properties[i].key);
    for (var j = 0; j < formFields.length; j++)
    {
      var formField = formFields[j];

      if (formField != null)
      {
        if (!formField.disabled && !Ext.isEmpty(jsonObject.properties[i].value))
        {
          if (formField.xtype == 'datefield')
          {
            formField.setValue(new Date(Number(jsonObject.properties[i].value)));
          }
          else if (formField.xtype == 'textfield')
          {
            formField.setValue(jsonObject.properties[i].value);
          }
          else if (formField.xtype == 'numberfield')
          {
            formField.setValue(Number(jsonObject.properties[i].value));
          }
          else if (formField.xtype == 'checkbox')
          {
            formField.setValue(Boolean('true' == jsonObject.properties[i].value));
          }
          else if (formField.xtype == 'combo')
          {
            formField.setValue(jsonObject.properties[i].value);
          }
          else if (customTypes && customTypes[formField.xtype])
          {
            customTypes[formField.xtype].setValue.call(formField, formField, jsonObject.properties[i].value);
          }
          break;
        }
      }
    }
  }
};