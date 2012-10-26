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
define('ext/form/ComboBox',['extjs'], function(Ext) {
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
        rec = this.findRecord(this.displayField, val);
        this.setRawValue( rec.get(this.valueField || this.displayField) );
      }
    }
  });
});
