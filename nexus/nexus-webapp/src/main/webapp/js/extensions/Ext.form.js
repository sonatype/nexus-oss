/*
  Ext.form.js
  Sonatype specific Ext Form overrides and extensions
    * Ext.form.Field override to provide help text quick tip
    * Sonatype implementations of form Actions
  Note: Ext namespace is maintained
*/

Ext.override(Ext.form.BasicForm, {
    /**
     * Override findField to look for enabled field
     * and return that, otherwise return first found
     */
    findField : function(id){
        var field = null;
        var fallbackField = null;
        this.items.each(function(f){
            if(f.isFormField && (f.dataIndex == id || f.id == id || f.getName() == id)){
                // Only want to grab the first one found, to match default behaviour
                if ( fallbackField == null )
                {
                    fallbackField = f;
                }
                
                // If the field isn't disabled use it
                if (f.disabled == false){
                    field = f;
                    return false;
                }
            }
        });
        
        if ( field == null )
        {
            if ( fallbackField != null )
            {
                field = fallbackField;
            }
            else
            {
                field = this.items.get(id);
            }
        }
        
        return field || null;
    },
});

/* Override default form field rendering to include help text quick tip on 
   question mark rendered after field label.
 */
Ext.override(Ext.form.Field, {
  afterRender : function(){
    var helpClass = null;
    var wrapDiv = null;
    if (this.getXType() == 'combo'
    ||  this.getXType() == 'datefield'
    ||  this.getXType() == 'timefield') {
      wrapDiv = this.getEl().up('div.x-form-field-wrap');
      helpClass = 'form-label-helpmark-combo';
    }
    else if (this.getXType() == 'checkbox') {
      wrapDiv = this.getEl().up('div.x-form-check-wrap');
      helpClass = 'form-label-helpmark-check';
    }
    else {
      wrapDiv = this.getEl().up('div.x-form-element');
      helpClass = 'form-label-helpmark';
    }
        
    //@todo: afterText doesn't work with combo boxes!
    if(this.afterText){
      wrapDiv.createChild({
        tag: 'span',
        cls: 'form-label-after-field',
        html: this.afterText
      });
    }
    
    if(this.helpText){
        var helpMark = wrapDiv.createChild({
          tag: 'img',
          src: Sonatype.config.resourcePath + '/images/icons/help.png',
          width: 16,
          height: 16,
          cls: helpClass
        });
        
      Ext.QuickTips.register({
          target:  helpMark,
          title: '',
          text: this.helpText,
          enabled: true
      });
    }
    
    //original method
    Ext.form.Field.superclass.afterRender.call(this);
    this.initEvents(); 
  }
  
});


/**
 @class Ext.form.Action.sonatypeSubmit
 @extends Ext.form.Action
 A custom sonatype form serializer that submits JSON text to the Sonatype service
 and processes the returned response.

 ToDo:
 Define the error response format and variations.  How to expose this to callbacks and event handlers?
 
 Other data may be placed into the response for processing the the Ext.form.BasicForm's callback
 or event handler methods. The object decoded from this JSON is available in the result property.
 
 Note:
   * no form.errorReader accepted.  JSON error format (when defined) is the only format accepted.
   
 Additional option params:
   * serviceDataObj: reference object that matches the service's data object for this action
   * fpanel: FormPanel
   * dataModifiers: optional functions to modify or collect data values
 
 Additional values:
   * after submit the output object is available on action.output.  This is the native object
     that was serialized and sent to the server.
 */
Ext.form.Action.sonatypeSubmit = function(form, options){
    Ext.form.Action.sonatypeSubmit.superclass.constructor.call(this, form, options);
};

Ext.extend(Ext.form.Action.sonatypeSubmit, Ext.form.Action, {
    /**
    * @cfg {boolean} clientValidation Determines whether a Form's fields are validated
    * in a final call to {@link Ext.form.BasicForm#isValid isValid} prior to submission.
    * Pass <tt>false</tt> in the Form's submit options to prevent this. If not defined, pre-submission field validation
    * is performed.
    */
    type : 'sonatypeSubmit',

    // private
    run : function(){
        var o = this.options;
        var method = this.getMethod();
        var isPost = method == 'POST';
        if(o.clientValidation === false || this.form.isValid()){
          var sJsonOutput = this.serializeForm(this.options.fpanel, this.form);
          
          Ext.Ajax.request(Ext.apply(this.createCallback(o), {
            jsonData:sJsonOutput,
            url:this.getUrl(!isPost),
            method: method,
            params:isPost ? this.getParams() : null,
            isUpload: this.form.fileUpload
          }));
        }
        else if (o.clientValidation !== false){ // client validation failed
          this.failureType = Ext.form.Action.CLIENT_INVALID;
          this.form.afterAction(this, false);
        }
    },

    // override connection failure because server validation errors come back with 400 code
    failure : function(response){
        this.response = response;
        if (response.status == 400){ //validation error
          this.success(response);
          return;
        }
        
        this.failureType = Ext.form.Action.CONNECT_FAILURE;
        this.form.afterAction(this, false);
    },
    
    // private
    success : function(response){
      var result = this.processResponse(response);

      if(result === true || result.data){
        this.form.afterAction(this, true);
        return;
      }
      
      if(result.errors != null){
        if (this.options.validationModifiers){
          var remainingErrors = [];
          for (var i = 0; i < result.errors.length; i++){
            if (this.options.validationModifiers[result.errors[i].id]){
              if (typeof(this.options.validationModifiers[result.errors[i].id]) == 'function') {
                (this.options.validationModifiers[result.errors[i].id])(result.errors[i], this.options.fpanel);
              }
              else{
                var errorObj = result.errors[i];
                errorObj.id = this.options.validationModifiers[result.errors[i].id];
                remainingErrors[remainingErrors.length] = errorObj;
              }
            }            
            else{
              remainingErrors[remainingErrors.length] = result.errors[i];
            }
          }
          
          result.errors = remainingErrors;
        }
          
        this.form.markInvalid(result.errors);
        this.failureType = Ext.form.Action.SERVER_INVALID;
      }
      this.form.afterAction(this, false);
    },

    // private
    handleResponse : function(response){
      try {
        return Ext.decode(response.responseText); //throws SyntaxError
      }
      catch (e) {
        return false; 
      }
    },
    
    // private
    serializeForm : function(fpanel, form){
      var output = Sonatype.utils.cloneObj(this.options.serviceDataObj);
      //note: srcObj (form.sonatypeLoadedData) is not modified only walked
      this.serializeFormHelper(fpanel, output, this.options.serviceDataObj, '');
      this.output = {"data":output};
      if(Sonatype.config.isDebug){ console.info(this.options.method + ' ' + this.options.url + ' ' , this.output); }
      return Ext.encode(this.output);
    },

    // serializeHelper(object fpanel, object accObj, object srcObj, string sPrepend, [string sVal])
    // Leave off sVal arg to call on root data obj
    // Walks the data object sent from the server originally, and plucks field values from form
    //   applying modifier functions if specified.  Handles collapsed fleldsets by returning null
    //   as object value to server.
    // Invariant: srcObj is not modified! If this changes, call with a cloned copy
    serializeFormHelper : function(fpanel, accObj, srcObj, sPrepend, sVal){
      var value, nextPrepend;
      if (sVal){ //non-root case
        nextPrepend = sPrepend + sVal + '.';
        value = srcObj[sVal]; //@todo: "value" name here is whack because, it's not the field value
      }
      else { //root case
        nextPrepend = sPrepend;
        value = srcObj;
      }

      if (Ext.type(value) === 'object'){
        if (sVal){ //only write object serialization for non-root objects
          var fieldSet = Ext.getCmp(fpanel.id + '_' + sPrepend + sVal);
          if(fieldSet && fieldSet.collapsed){
            eval('accObj' + '.' + sPrepend + sVal + ' = null;');
            return; //skip recursive calls for children form items
          }
        }

        for (var i in value){
          this.serializeFormHelper(fpanel, accObj, value, nextPrepend, i);
        }
      }
      else { //only non-root case should ever get in here
        var flatName = sPrepend + sVal;
        var field = fpanel.form.findField(flatName);
        var fieldValue = null;

        if(field && !Ext.isEmpty(field.getValue(), false)){ //getValue normalizes undefined to '', but it's still false
          fieldValue = field.getValue();
        }
        
        //data mod function gets the field value if the field exists.  o/w null is passed
        fieldValue = (this.options.dataModifiers && this.options.dataModifiers[flatName]) ? (this.options.dataModifiers[flatName])(fieldValue, fpanel) : fieldValue;
        eval('accObj' + '.' + flatName + ' = fieldValue;');
      }
    }
  
// @note: this was going to be used for processing hierarchical error messaging    
//  // walks the error response and flattens each field into a flat object by fieldname
//  flattenErrors : function(accObj, srcObj, sPrepend, sVal){
//    var value, nextPrepend;
//    if (sVal){ //non-root case
//      nextPrepend = sPrepend + sVal + '.';
//      value = srcObj[sVal];
//    }
//    else { //root case
//      nextPrepend = sPrepend;
//      value = srcObj;
//    }
//
//    if (Ext.type(value) === 'object'){
//      for (var i in value){
//        this.flattenErrors(fpanel, accObj, value, nextPrepend, i);
//      }
//    }
//    else { //only non-root case should ever get in here
//      var flatName = sPrepend + sVal;
//      if(!Ext.isEmpty(value, true)){
//        accObj[flatName] = value;
//      }
//    }
//  }
});


/**
 @class Ext.form.Action.sonatypeLoad
 @extends Ext.form.Action
 A class which handles loading of data from Sonatype service into the Fields of
 an Ext.form.BasicForm. 
 
 Expected repsonse format
    {
        data: {
            clientName: "Fred. Olsen Lines",
            portOfLoading: "FXT",
            portOfDischarge: "OSL"
        }
    }

 Other data may be placed into the response for processing the Ext.form.BasicForm Form's callback
 or event handler methods. The object decoded from this JSON is available in the result property.
 
 Needed Improvements:
  * create a standard way to for callbacks to assess server respone's success.  The regular
    loader gave access to this in the data.success field that it required in its data format.
    We could access this.response.HTTPcode (?).  Should we push that down in an acessible
    way, so every callback doesn't need to understand our service response codes.
 
 Notes
 * No form.reader may be used here.  JSON data format is assumed this loader 
 
 Additional options params:
   * fpanel the FromPanel containing this form
   * dataModifiers (optional)
 */
Ext.form.Action.sonatypeLoad = function(form, options){
    Ext.form.Action.sonatypeLoad.superclass.constructor.call(this, form, options);
};

Ext.extend(Ext.form.Action.sonatypeLoad, Ext.form.Action, {
  // private
  type : 'sonatypeLoad',

  // private
  run : function(){
    Ext.Ajax.request(Ext.apply(
      this.createCallback(this.options), {
        method:this.getMethod(),
        url:this.getUrl(false),
        params:this.getParams()
    }));
  },

  // private
  //note: service response object "data" value expected here in result.data
  success : function(response){
    var result = this.processResponse(response);
    if(result === true || !result.data){
      this.failureType = Ext.form.Action.LOAD_FAILURE;
      this.form.afterAction(this, false);
      return;
    }
    this.form.clearInvalid();
    var flatData = this.translateDataToFieldValues(this.options.fpanel, result.data);
    this.form.setValues(flatData);
    this.form.afterAction(this, true);
  },

  // private
  // called from in Ext.form.Action.processResponse
  handleResponse : function(response){
    return Ext.decode(response.responseText);
  },
  
  // private
  // takes result.data and returns flattened data to pass to this.form.setValues()
  translateDataToFieldValues : function(fpanel, data){
    var flat = {};
    this.translateHelper(fpanel, flat, data, '');
    return flat;
  },
  
  // translateHelper(object accObj, object srcObj, string sPrepend, [string sVal])
  // Leave off sVal arg to call on root data obj
  translateHelper : function(fpanel, accObj, srcObj, sPrepend, sVal){
    var value, nextPrepend;
    if (sVal){ //non-root case
      nextPrepend = sPrepend + sVal + '.';
      value = srcObj[sVal];
    }
    else { //root case
      nextPrepend = sPrepend;
      value = srcObj;
    }
    
    if (Ext.type(value) === 'object'){
      var hasNonEmptyChildren = false;
      for (var i in value){
        var thisChildNotEmpty = this.translateHelper(fpanel, accObj, value, nextPrepend, i);
        hasNonEmptyChildren = hasNonEmptyChildren || thisChildNotEmpty;
      }
      if (sVal){ //only write object serialization for non-root objects
        if(hasNonEmptyChildren){
          var fieldSet = Ext.getCmp(fpanel.id + '_' + sPrepend + sVal);
          if(fieldSet){
            fieldSet.expand(true);
          }
        }
        accObj['.' + sPrepend + sVal] = hasNonEmptyChildren;
      }
      return hasNonEmptyChildren;
    }
    else { //only non-root case should ever get in here
      var flatName = sPrepend + sVal;
      // note: all vaues passed to modifier funcs, even if the value is undefined, null, or empty!
      //       Modifier funcs should ALWAYS return a value, even if it's unmodified.
      value = (this.options.dataModifiers && this.options.dataModifiers[flatName]) ? this.options.dataModifiers[flatName](value, srcObj, fpanel) : value;
      if(Ext.isEmpty(value, true)){
        return false;
      }
      accObj[flatName] = value;
      return true;
    }
  }
});

Ext.form.Action.ACTION_TYPES.sonatypeLoad = Ext.form.Action.sonatypeLoad;
Ext.form.Action.ACTION_TYPES.sonatypeSubmit = Ext.form.Action.sonatypeSubmit;
