/*
 * Ext JS Library 2.0 RC 1
 * Copyright(c) 2006-2007, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

Ext.app.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
  initComponent : function(){
    Ext.app.SearchField.superclass.initComponent.call(this);
    this.on('specialkey', function(f, e){
      if(e.getKey() == e.ENTER){
          this.onTrigger2Click();
      }
    }, this);
    if ( this.searchPanel ) {
      this.searchPanel.searchField = this;
    }
  },

  validationEvent:false,
  validateOnBlur:false,
  trigger1Class:'x-form-clear-trigger',
  trigger2Class:'x-form-search-trigger',
  hideTrigger1:true,
  width:180,
  paramName : 'q',

  onTrigger1Click : function(){
    if(this.getRawValue()){
      this.el.dom.value = '';
      this.triggers[0].hide();
      this.hasSearch = false;
    }
  },

  onTrigger2Click : function(){
    var v = this.getRawValue();
    if(v.length < 1){
      this.onTrigger1Click();
      return;
    }
    //var o = {start: 0};
    this.searchPanel.startSearch( this.searchPanel );
  }
});

Ext.reg('nexussearchfield', Ext.app.SearchField);
