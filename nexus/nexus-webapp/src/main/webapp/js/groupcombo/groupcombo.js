/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
/**
 * @class Ext.ux.form.GroupComboBox
 * @extends Ext.form.ComboBox This class extends a combobox to allow grouping to
 *          be displayed like that of the groupinggrid.
 * @license: BSD
 * @author: Robert B. Williams (extjs id: vtswingkid)
 * @constructor Creates a new GroupComboBox
 * @param {Object}
 *          config Configuration options
 * @version 0.1.0
 */
Ext.namespace("Ext.ux", "Ext.ux.form");
Ext.ux.form.GroupComboBox = Ext.extend(Ext.form.ComboBox, {
      initList : function() {
        if (!this.list)
        {
          var cls = 'x-combo-list';

          this.list = new Ext.Layer({
                shadow : this.shadow,
                cls : [cls, this.listClass].join(' '),
                constrain : false
              });

          var lw = this.listWidth || Math.max(this.wrap.getWidth(), this.minListWidth);
          this.list.setWidth(lw);
          this.list.swallowEvent('mousewheel');
          this.assetHeight = 0;

          if (this.title)
          {
            this.header = this.list.createChild({
                  cls : cls + '-hd',
                  html : this.title
                });
            this.assetHeight += this.header.getHeight();
          }

          this.innerList = this.list.createChild({
                cls : cls + '-inner'
              });
          this.innerList.on('mouseover', this.onViewOver, this);
          this.innerList.on('mousemove', this.onViewMove, this);
          this.innerList.setWidth(lw - this.list.getFrameWidth('lr'));

          if (this.pageSize)
          {
            this.footer = this.list.createChild({
                  cls : cls + '-ft'
                });
            this.pageTb = new Ext.PagingToolbar({
                  store : this.store,
                  pageSize : this.pageSize,
                  renderTo : this.footer
                });
            this.assetHeight += this.footer.getHeight();
          }

          if (!this.tpl)
          {
            this.tpl = '<tpl for="."><div class="' + cls + '-item">{' + this.displayField + '}</div></tpl>';
          }

          this.view = new Ext.ux.GroupDataView({
                applyTo : this.innerList,
                tpl : this.tpl,
                singleSelect : true,
                selectedClass : this.selectedClass,
                itemSelector : this.itemSelector || '.' + cls + '-item',
                showGroupName : this.showGroupName,
                startCollapsed : this.startCollapsed,
                groupTextTpl : this.groupTextTpl,
                combo : this
              });

          this.view.on('click', this.onViewClick, this);

          this.bindStore(this.store, true);

          if (this.resizable)
          {
            this.resizer = new Ext.Resizable(this.list, {
                  pinned : true,
                  handles : 'se'
                });
            this.resizer.on('resize', function(r, w, h) {
                  this.maxHeight = h - this.handleHeight - this.list.getFrameWidth('tb') - this.assetHeight;
                  this.listWidth = w;
                  this.innerList.setWidth(w - this.list.getFrameWidth('lr'));
                  this.restrictHeight();
                }, this);
            this[this.pageSize ? 'footer' : 'innerList'].setStyle('margin-bottom', this.handleHeight + 'px');
          }
        }
      }
    });
Ext.reg('uxgroupcombo', Ext.ux.form.GroupComboBox);