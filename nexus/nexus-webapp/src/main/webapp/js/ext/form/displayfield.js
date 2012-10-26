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
/*global define*/

define('ext/form/displayfield',['extjs'], function(Ext) {
/**
 * @class Ext.form.DisplayField
 * @extends Ext.form.Field A display-only text field which is not validated and
 *          not submitted.
 * @constructor Creates a new DisplayField.
 * @param {Object}
      *          config Configuration options
 * @xtype displayfield
 */
Ext.form.DisplayField = Ext.extend(Ext.form.Field, {
  validationEvent : false,
  validateOnBlur : false,
  defaultAutoCreate : {
    tag : "div"
  },
  /**
   * @cfg {String} fieldClass The default CSS class for the field (defaults to
   *      <tt>"x-form-display-field"</tt>)
   */
  fieldClass : "x-form-display-field",
  /**
   * @cfg {Boolean} htmlEncode <tt>false</tt> to skip HTML-encoding the text
   *      when rendering it (defaults to <tt>false</tt>). This might be
   *      useful if you want to include tags in the field's innerHTML rather
   *      than rendering them as string literals per the default logic.
   */
  htmlEncode : false,

  // private
  initEvents : Ext.emptyFn,

  isValid : function() {
    return true;
  },

  validate : function() {
    return true;
  },

  getRawValue : function() {
    var v = this.rendered ? this.el.dom.innerHTML : Ext.value(this.value, '');
    if (v === this.emptyText)
    {
      v = '';
    }
    if (this.htmlEncode)
    {
      v = Ext.util.Format.htmlDecode(v);
    }
    return v;
  },

  getValue : function() {
    return this.getRawValue();
  },

  getName : function() {
    return this.name;
  },

  setRawValue : function(v) {
    if (this.htmlEncode)
    {
      v = Ext.util.Format.htmlEncode(v);
    }
    return this.rendered ? (this.el.dom.innerHTML = (Ext.isEmpty(v) ? '' : v)) : (this.value = v);
  },

  setValue : function(v) {
    this.setRawValue(v);
    return this;
  }
  /**
   * @cfg {String} inputType
   * @hide
   */
  /**
   * @cfg {Boolean} disabled
   * @hide
   */
  /**
   * @cfg {Boolean} readOnly
   * @hide
   */
  /**
   * @cfg {Boolean} validateOnBlur
   * @hide
   */
  /**
   * @cfg {Number} validationDelay
   * @hide
   */
  /**
   * @cfg {String/Boolean} validationEvent
   * @hide
   */
});

Ext.reg('displayfield', Ext.form.DisplayField);

Ext.form.TimestampDisplayField = Ext.extend(Ext.form.DisplayField, {
  setValue : function(v) {
    // java give the timestamp in miliseconds, extjs consumes it in seconds
    var toSecs = Math.round(v / 1000);
    v = new Date.parseDate(toSecs, 'U').toString();
    this.setRawValue(v);
    return this;
  }
});

Ext.reg('timestampDisplayField', Ext.form.TimestampDisplayField);

Ext.form.ByteDisplayField = Ext.extend(Ext.form.DisplayField, {
  setValue : function(v) {
    if (v < 1024)
    {
      v = v + ' Bytes';
    }
    else if (v < 1048576)
    {
      v = (v / 1024).toFixed(2) + ' KB';
    }
    else if (v < 1073741824)
    {
      v = (v / 1048576).toFixed(2) + ' MB';
    }
    else
    {
      v = (v / 1073741824).toFixed(2) + ' GB';
    }
    this.setRawValue(v);
    return this;
  }
});

Ext.reg('byteDisplayField', Ext.form.ByteDisplayField);

});
