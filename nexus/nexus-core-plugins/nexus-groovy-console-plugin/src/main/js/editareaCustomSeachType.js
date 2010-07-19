Sonatype.Events.addListener('initializeCustomTypes', function(customTypes) {
      customTypes['textarea'] = {
        createItem : function(curRec) {
          return {
            xtype : 'textarea',
            fieldLabel : curRec.name,
            itemCls : curRec.required ? 'required-field' : '',
            helpText : curRec.helpText,
            name : 'serviceProperties_' + curRec.id,
            allowBlank : curRec.required ? false : true,
            disabled : true,
            width : this.COMBO_WIDTH
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
