Sonatype.Events.addListener('initializeCustomTypes', function(customTypes) {
      customTypes['textarea'] = {
        createItem : function(curRec, prefix, width) {
          return {
            xtype : 'textarea',
            fieldLabel : curRec.label,
            itemCls : curRec.required ? 'required-field' : '',
            helpText : curRec.helpText,
            name : prefix + curRec.id,
            allowBlank : curRec.required ? false : true,
            disabled : true,
            width : width
          }
        },
        retrieveValue : function(item) {
          return item.getValue();
        },
        setValue : function(item, value) {
          item.setValue(value);
        }
      };
    });
