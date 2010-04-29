/*
 * Sonatype Nexus (TM) Open Source Version. Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at
 * http://nexus.sonatype.org/dev/attributions.html This program is licensed to
 * you under Version 3 only of the GNU General Public License as published by
 * the Free Software Foundation. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License Version 3 for more details. You should have received a copy of
 * the GNU General Public License Version 3 along with this program. If not, see
 * http://www.gnu.org/licenses/. Sonatype Nexus (TM) Professional Version is
 * available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc.
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
