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
/*global top, define*/
define('Nexus/util/FormFieldUtil',['extjs', 'Sonatype/view', 'nexus'], function(Ext, Sonatype, Nexus) {

Ext.namespace('Nexus.util');
Nexus.util.FormFieldUtil = {
  generateForm : function(panelId, fieldSetName, fieldNamePrefix, typeStore, repoStore, groupStore, repoOrGroupStore,
        customTypes, width) {
    var allTypes = [];

    if (!width) {
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
      var j, items = [], curRec;
      if (item.data.formFields.length > 0) {
        for (j = 0; j < item.data.formFields.length; j = j + 1) {
          curRec = item.data.formFields[j];
          // Note that each item is disabled initially, this is because
          // the select handler for the capabilityType
          // combo box handles enabling/disabling as necessary, so each
          // inactive card isn't also included in the form
          if (curRec.type === 'string') {
            items[j] = {
              xtype : 'textfield',
              htmlDecode : true,
              fieldLabel : curRec.label,
              itemCls : curRec.required ? 'required-field' : '',
              helpText : curRec.helpText,
              name : fieldNamePrefix + curRec.id,
              allowBlank : curRec.required ? false : true,
              disabled : true,
              width : width,
              regex : curRec.regexValidation ? new RegExp(curRec.regexValidation) : null
            };
            if (curRec.initialValue) {
              items[j].value = curRec.initialValue;
            }
          }
          else if (curRec.type === 'number') {
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
            if (curRec.initialValue) {
              items[j].value = Number(curRec.initialValue);
            }
          }
          else if (curRec.type === 'text-area') {
            items[j] = {
              xtype : 'textarea',
              htmlDecode : true,
              fieldLabel : curRec.label,
              itemCls : curRec.required ? 'required-field' : '',
              helpText : curRec.helpText,
              name : fieldNamePrefix + curRec.id,
              allowBlank : curRec.required ? false : true,
              disabled : true,
              anchor : '-20',
              height : '138',
              regex : curRec.regexValidation ? new RegExp(curRec.regexValidation) : null
            };
            if (curRec.initialValue) {
              items[j].value = curRec.initialValue;
            }
          }
          else if (curRec.type === 'checkbox') {
            items[j] = {
              xtype : 'checkbox',
              fieldLabel : curRec.label,
              helpText : curRec.helpText,
              name : fieldNamePrefix + curRec.id,
              disabled : true
            };
            if (curRec.initialValue) {
              items[j].checked = Boolean('true' === curRec.initialValue);
            }
          }
          else if (curRec.type === 'date') {
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
            if (curRec.initialValue) {
              items[j].value = new Date(Number(curRec.initialValue));
            }
          }
          else if (curRec.type === 'repo') {
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
            if (curRec.initialValue) {
              items[j].value = curRec.initialValue;
            }
          }
          else if (curRec.type === 'group') {
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
            if (curRec.initialValue) {
              items[j].value = curRec.initialValue;
            }
          }
          else if (curRec.type === 'repo-or-group') {
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
            if (curRec.initialValue) {
              items[j].value = curRec.initialValue;
            }
          }
          else if (customTypes && customTypes[curRec.type]) {
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
      else {
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
  }, "exportForm" : function(formPanel, panelIdSuffix, formFieldPrefix, customTypes) {
    var outputArr = [], i = 0, formFieldPanel = formPanel.findById(formPanel.id + panelIdSuffix);

    // These are dynamic fields here, so some pretty straightforward generic
    // logic below
    formFieldPanel.getLayout().activeItem.items.each(function(item, i, len) {
      var value;

      if (item.xtype === 'datefield') {
        // long representation is used, not actual date
        // force to a string, as that is what the current api requires
        value = String(item.getValue().getTime());
      }
      else if (item.xtype === 'textfield') {
        value = item.getValue();
      }
      else if (item.xtype === 'numberfield') {
        // force to a string, as that is what the current api requires
        value = String(item.getValue());
      }
      else if (item.xtype === 'textarea') {
        value = item.getValue();
      }
      else if (item.xtype === 'checkbox') {
        value = String(item.getValue());
      }
      else if (item.xtype === 'combo') {
        value = item.getValue();
      }
      else if (customTypes && customTypes[item.xtype]) {
        value = customTypes[item.xtype].retrieveValue.call(item, item);
      }

      outputArr[i] = {
        key : item.getName().substring(formFieldPrefix.length),
        value : value
      };
      i = i + 1;
    }, formFieldPanel.getLayout().activeItem);

    return outputArr;
  }, "importForm" : function(jsonObject, formPanel, formFieldPrefix, customTypes) {
    var i, j, formFields, formField;
    // Maps the incoming json properties to the generic component
    for (i = 0; i < jsonObject.properties.length; i = i + 1) {
      formFields = formPanel.find('name', formFieldPrefix + jsonObject.properties[i].key);
      for (j = 0; j < formFields.length; j = j + 1) {
        formField = formFields[j];

        if (formField !== null) {
          if (!formField.disabled && !Ext.isEmpty(jsonObject.properties[i].value)) {
            if (formField.xtype === 'datefield') {
              formField.setValue(new Date(Number(jsonObject.properties[i].value)));
            }
            else if (formField.xtype === 'textfield') {
              formField.setValue(jsonObject.properties[i].value);
            }
            else if (formField.xtype === 'numberfield') {
              formField.setValue(Number(jsonObject.properties[i].value));
            }
            else if (formField.xtype === 'textarea') {
              formField.setValue(jsonObject.properties[i].value);
            }
            else if (formField.xtype === 'checkbox') {
              formField.setValue(Boolean('true' === jsonObject.properties[i].value));
            }
            else if (formField.xtype === 'combo') {
              formField.setValue(jsonObject.properties[i].value);
            }
            else if (customTypes && customTypes[formField.xtype]) {
              customTypes[formField.xtype].setValue.call(formField, formField, jsonObject.properties[i].value);
            }
            break;
          }
        }
      }
    }
  }
};

// FIXME legacy
top.FormFieldGenerator = Nexus.util.FormFieldUtil.generateForm;
top.FormFieldImporter = Nexus.util.FormFieldUtil.importForm;
top.FormFieldExporter = Nexus.util.FormFieldUtil.exportForm;

});