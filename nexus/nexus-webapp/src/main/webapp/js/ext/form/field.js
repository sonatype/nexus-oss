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
/*global Ext, Sonatype, Nexus*/

Ext.override(Ext.form.Field, {
  adjustWidth : function(tag, w) {
    tag = tag.toLowerCase();
    // Sonatype: modified input text sizing for Safari3 in strict mode bug.
    if (typeof w === 'number')
    {
      if (Ext.isIE && (tag === 'input' || tag === 'textarea'))
      {
        if (tag === 'input' && !Ext.isStrict)
        {
          return this.inEditor ? w : w - 3;
        }
        if (tag === 'input' && Ext.isStrict)
        {
          return w - (Ext.isIE6 ? 4 : 1);
        }
        if (tag === 'textarea' && Ext.isStrict)
        {
          return w - 2;
        }
      }
      else if (Ext.isOpera && Ext.isStrict)
      {
        if (tag === 'input')
        {
          return w + 2;
        }
        if (tag === 'textarea')
        {
          return w - 2;
        }
      }
      else if (Ext.isSafari3)
      {
        // Sonatype: assumes we are serving xhtml transitional doctype
        if (tag === 'input')
        {
          return w - 8;
        }
      }
    }
    return w;
  }
});

Ext.override(Ext.form.Field, {
  onEnable : function() {
    this.getActionEl().removeClass(this.disabledClass);
    if (this.actionMode === 'container')
    {
      // in some cases the action mode seems to change after rendering,
      // so we may need to clean up the disabled class from this.el
      this.el.removeClass(this.disabledClass);

      //also check for wrap
      if ( this.wrap ) {
        this.wrap.removeClass(this.disabledClass);
      }
    }
    this.el.dom.readOnly = false;
  },
  onDisable : function() {
    this.getActionEl().addClass(this.disabledClass);
    this.el.dom.readOnly = true;
  }
});

Ext.override(Ext.form.Field, {
  /*
   * Override default form field rendering to include help text quick tip on
   * question mark rendered after field label.
   */
  afterRenderOrig : Ext.form.Field.prototype.afterRender,
  afterRender : function() {
    var helpClass = null, wrapDiv = null, helpMark = null;
    if (this.getXType() === 'combo' || this.getXType() === 'uxgroupcombo' || this.getXType() === 'datefield' || this.getXType() === 'timefield')
    {
      wrapDiv = this.getEl().up('div.x-form-field-wrap');
      helpClass = 'form-label-helpmark-combo';
    }
    else if (this.getXType() === 'checkbox')
    {
      wrapDiv = this.getEl().up('div.x-form-check-wrap');
      helpClass = 'form-label-helpmark-check';
    }
    else if (this.getXType() === 'textarea')
    {
      wrapDiv = this.getEl().up('div.x-form-element');
      helpClass = 'form-label-helpmark-textarea';
    }
    else
    {
      wrapDiv = this.getEl().up('div.x-form-element');
      helpClass = 'form-label-helpmark';
    }

    // @todo: afterText doesn't work with combo boxes!
    if (this.afterText)
    {
      wrapDiv.createChild({
        tag : 'span',
        cls : 'form-label-after-field',
        html : this.afterText
      });
    }

    if (this.helpText)
    {
      helpMark = wrapDiv.createChild({
        tag : 'img',
        src : Sonatype.config.resourcePath + '/images/icons/help.png',
        width : 16,
        height : 16,
        cls : helpClass
      });

      Ext.QuickTips.register({
        target : helpMark,
        title : '',
        text : this.helpText,
        enabled : true
      });
    }

    // original method
    this.afterRenderOrig(arguments);
  }

});

Ext.override(Ext.form.TextField, {
  /**
   * @cfg {Boolean} htmlDecode
   * <tt>true</tt> to decode html entities in the value given to
   * Ext.form.TextField.setValue and Ext.form.TextField.setRawValue
   * before setting the actual value.
   * <p/>
   * This is needed for displaying the 'literal' value in the text field when it was received by the server,
   * for example in the repository name. The REST layer will encode to html entities, which will be correct
   * for html rendering, but text fields without this configuration will display '&quot;test&quot;' instead
   * of the originally sent '"test"'.
   * <p/>
   * Default value is 'true'.
   */
  htmlDecode : true,

  /**
   * @cfg {Boolean} htmlConvert
   * <tt>true</tt> to decode html entities in the value given to
   * Ext.form.TextField.set(Raw)Value
   * before setting the actual value, and encode html entities again
   * in the call to Ext.form.TextField.get(Raw)Value.
   * <p/>
   * This is needed for displaying the 'literal' value in the text field when it was received by the server
   * (see htmlDecode configuration doc), and display to the user correctly before round-tripping to the server again
   * (e.g. in a grid field).
   * <p/>
   * when this config is set, the value has to be html-decoded again before sending it to the server, because the REST layer
   * will encode the string again.
   * <p/>
   * Default value is false.
   */
  htmlConvert : false,

  setRawValueOrig : Ext.form.TextField.prototype.setRawValue,
  setValueOrig : Ext.form.TextField.prototype.setValue,
  getRawValueOrig : Ext.form.TextField.prototype.getRawValue,
  getValueOrig : Ext.form.TextField.prototype.getValue,

  setRawValue : function(value) {
    if ( this.htmlDecode || this.htmlConvert )
    {
      value = Nexus.util.Format.htmlDecode(value);
    }
    this.setRawValueOrig(value);
  },
  setValue : function(value) {
    if ( this.htmlDecode || this.htmlConvert )
    {
      value = Nexus.util.Format.htmlDecode(value);
    }
    this.setValueOrig(value);
  },
  getRawValue : function() {
    var value = this.getRawValueOrig();
    if ( this.htmlConvert )
    {
      value = Ext.util.Format.htmlEncode(value);
    }
    return value;
  },
  getValue : function() {
    var value = this.getValueOrig();
    if ( this.htmlConvert )
    {
      value = Ext.util.Format.htmlEncode(value);
    }
    return value;
  }
});

Ext.override(Ext.form.ComboBox, {
  /**
   * ComboBox field needs to encode it's value again, because the drop-down list is rendering HTML
   * (so e.g. '1&amp;2' is displayed as '1&2' in the list). The field that holds the selected value
   * is not rendering HTML, so we need to unescape the displayed value. That would lead to a mismatch
   * between the selected value and the one in the list, if we do not encode the value again in the getter.
   */
  htmlConvert : true,
  /**
   * We need to override this because ComboBox would set the DOM value directly, circumventing the htmlDecode/Convert
   * configuration.
   */
  beforeBlur : function() {
    var val = this.getRawValue(), rec;
    if (this.forceSelection) {
      if (val.length > 0 && val !== this.emptyText) {
        // do not set the DOM value directly, use #setRawValue to pick up htmlConvert setting
        // this.el.dom.value = this.lastSelectionText === undefined ? '' : this.lastSelectionText;
        this.setRawValue(this.lastSelectionText === undefined ? '' : this.lastSelectionText);
        this.applyEmptyText();
      } else {
        this.clearValue();
      }
    } else {
      this.setValue(Ext.value(
            this.findRecord(this.displayField, val),
            rec.get(this.valueField || this.displayField))
      );
    }
  }
});


