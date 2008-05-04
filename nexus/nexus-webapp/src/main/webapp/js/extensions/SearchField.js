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
  },

  validationEvent:false,
  validateOnBlur:false,
  trigger1Class:'x-form-clear-trigger',
  trigger2Class:'x-form-search-trigger',
  hideTrigger1:true,
  width:180,
  hasSearch : false,
  paramName : 'q',

  onTrigger1Click : function(){
    if(this.hasSearch){
      this.el.dom.value = '';
      //var o = {start: 0};
      this.triggers[0].hide();
      this.searchPanel.resetSearch( this.searchPanel );
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