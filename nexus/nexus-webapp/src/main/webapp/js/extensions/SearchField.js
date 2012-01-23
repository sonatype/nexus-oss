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
Ext.app.SearchField = Ext.extend(Ext.form.TwinTriggerField, {
      initComponent : function() {
        Ext.app.SearchField.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e) {
              if (e.getKey() == e.ENTER)
              {
                this.onTrigger2Click();
              }
            }, this);
        if (this.searchPanel)
        {
          this.searchPanel.searchField = this;
        }
      },

      validationEvent : false,
      validateOnBlur : false,
      trigger1Class : 'x-form-clear-trigger',
      trigger2Class : 'x-form-search-trigger',
      hideTrigger1 : true,
      width : 180,
      paramName : 'q',

      onTrigger1Click : function() {
        if (this.getRawValue())
        {
          this.el.dom.value = '';
          this.triggers[0].hide();
          this.hasSearch = false;
        }
        if (this.searchPanel.stopSearch)
        {
          this.searchPanel.stopSearch(this.searchPanel);
        }
      },

      onTrigger2Click : function() {
        var v = this.getRawValue();
        if (v.length < 1)
        {
          this.onTrigger1Click();
          return;
        }
        // var o = {start: 0};
        this.searchPanel.startSearch(this.searchPanel, true);
      }
    });

Ext.reg('nexussearchfield', Ext.app.SearchField);
