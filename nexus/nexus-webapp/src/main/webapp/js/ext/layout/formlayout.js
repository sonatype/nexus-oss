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
define('ext/layout/formlayout',['extjs'], function(Ext){
  /* ext-3.4.0
Ext.override(Ext.layout.FormLayout, {
  renderItem : function(c, position, target) {
    if (c && !c.rendered && c.isFormField && c.inputType !== 'hidden')
    {
      var args = [c.id, c.fieldLabel, c.labelStyle || this.labelStyle || '', this.elementStyle || '', typeof c.labelSeparator === 'undefined' ? this.labelSeparator : c.labelSeparator,
        (c.itemCls || this.container.itemCls || '') + (c.hideLabel ? ' x-hide-label' : ''), c.clearCls || 'x-form-clear-left'];
      if (typeof position === 'number')
      {
        position = target.dom.childNodes[position] || null;
      }
      if (position)
      {
        c.formItem = this.fieldTpl.insertBefore(position, args, true);
      }
      else
      {
        c.formItem = this.fieldTpl.append(target, args, true);
      }
      c.render('x-form-el-' + c.id);
      c.container = c.formItem; // must set after render, because render
      // sets it.
      c.actionMode = 'container';
    }
    else
    {
      Ext.layout.FormLayout.superclass.renderItem.apply(this, arguments);
    }
  }
});
*/
});
