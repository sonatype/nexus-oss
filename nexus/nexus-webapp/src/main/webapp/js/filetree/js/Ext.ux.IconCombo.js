// vim: ts=4:sw=4:nu:fdc=2:nospell
/**
 * Ext.ux.IconCombo Extension Class for Ext 2.x Library
 * 
 * @author Ing. Jozef Sakalos
 * @version $Id: Ext.ux.IconCombo.js 101 2008-03-27 00:46:38Z jozo $
 * @license Ext.ux.IconCombo is licensed under the terms of the Open Source LGPL
 *          3.0 license. Commercial use is permitted to the extent that the
 *          code/component(s) do NOT become part of another Open Source or
 *          Commercially licensed development library or toolkit without
 *          explicit permission. License details:
 *          http://www.gnu.org/licenses/lgpl.html
 */

/* global Ext */

/**
 * @class Ext.ux.IconCombo
 * @extends Ext.form.ComboBox
 */
Ext.ux.IconCombo = Ext.extend(Ext.form.ComboBox, {
      initComponent : function() {

        Ext.apply(this, {
              tpl : '<tpl for=".">' + '<div class="x-combo-list-item ux-icon-combo-item ' + '{' + this.iconClsField + '}">' + '{' + this.displayField + '}' + '</div></tpl>'
            });

        // call parent initComponent
        Ext.ux.IconCombo.superclass.initComponent.apply(this, arguments);

      } // eo function initComponent

      ,
      onRender : function(ct, position) {
        // call parent onRender
        Ext.ux.IconCombo.superclass.onRender.apply(this, arguments);

        // adjust styles
        this.wrap.applyStyles({
              position : 'relative'
            });
        this.el.addClass('ux-icon-combo-input');

        // add div for icon
        this.icon = Ext.DomHelper.append(this.el.up('div.x-form-field-wrap'), {
              tag : 'div',
              style : 'position:absolute'
            });
      } // eo function onRender

      ,
      afterRender : function() {
        Ext.ux.IconCombo.superclass.afterRender.apply(this, arguments);
        if (undefined !== this.value)
        {
          this.setValue(this.value);
        }
      } // eo function afterRender
      ,
      setIconCls : function() {
        var rec = this.store.query(this.valueField, this.getValue()).itemAt(0);
        if (rec && this.icon)
        {
          this.icon.className = 'ux-icon-combo-icon ' + rec.get(this.iconClsField);
        }
      } // eo function setIconCls

      ,
      setValue : function(value) {
        Ext.ux.IconCombo.superclass.setValue.call(this, value);
        this.setIconCls();
      } // eo function setValue

      ,
      clearValue : function() {
        Ext.ux.IconCombo.superclass.clearValue.call(this);
        if (this.icon)
        {
          this.icon.className = '';
        }
      } // eo function clearValue

    });

// register xtype
Ext.reg('iconcombo', Ext.ux.IconCombo);

// eof
