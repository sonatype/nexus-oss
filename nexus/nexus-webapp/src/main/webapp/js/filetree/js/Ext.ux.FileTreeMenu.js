// vim: ts=4:sw=4:nu:fdc=4:nospell
/**
 * Ext.ux.FileTreeMenu
 * 
 * @author Ing. Jozef Sakáloš
 * @version $Id: Ext.ux.FileTreeMenu.js 112 2008-03-28 21:11:17Z jozo $
 * @date 13. March 2008
 * @license Ext.ux.FileField is licensed under the terms of the Open Source LGPL
 *          3.0 license. Commercial use is permitted to the extent that the
 *          code/component(s) do NOT become part of another Open Source or
 *          Commercially licensed development library or toolkit without
 *          explicit permission. License details:
 *          http://www.gnu.org/licenses/lgpl.html
 */

/* global Ext */

/**
 * @class Ext.ux.FileTreeMenu
 * @extends Ext.menu.Menu
 * @constructor Creates new FileTreeMenu object
 * @param {Object}
 *          config A configuration object
 */
Ext.ux.FileTreeMenu = function(config) {
  config = config || {};

  var uploadPanelConfig = {
    contextmenu : this,
    buttonsAt : config.buttonsAt || 'tbar',
    singleUpload : config.singleUpload || false,
    maxFileSize : config.maxFileSize,
    enableProgress : config.enableProgress
  };
  if (config.baseParams)
  {
    config.baseParams.cmd = config.baseParams.cmd || 'upload';
    config.baseParams.dir = config.baseParams.dir || '.';
    uploadPanelConfig.baseParams = config.baseParams;
  }

  // {{{
  Ext.apply(config, {
        items : [{
              text : '&#160',
              cls : 'ux-ftm-nodename',
              disabledClass : '',
              disabled : true,
              cmd : 'nodename'
            }, {
              text : this.openText + ' (Enter)',
              iconCls : this.openIconCls,
              cmd : 'open',
              menu : {
                items : [{
                      text : this.openSelfText,
                      iconCls : this.openSelfIconCls,
                      cmd : 'open-self'
                    }, {
                      text : this.openPopupText,
                      iconCls : this.openPopupIconCls,
                      cmd : 'open-popup'
                    }, {
                      text : this.openBlankText,
                      iconCls : this.openBlankIconCls,
                      cmd : 'open-blank'
                    }, {
                      text : this.openDwnldText,
                      iconCls : this.openDwnldIconCls,
                      cmd : 'open-dwnld'
                    }]
              }
            }, new Ext.menu.Separator({
                  cmd : 'sep-open'
                }), {
              text : this.reloadText + ' (Ctrl+E)',
              iconCls : this.reloadIconCls,
              cmd : 'reload'
            }, {
              text : this.expandText + ' (Ctrl+&nbsp;&rarr;)',
              iconCls : this.expandIconCls,
              cmd : 'expand'
            }, {
              text : this.collapseText + ' (Ctrl+&nbsp;&larr;)',
              iconCls : this.collapseIconCls,
              cmd : 'collapse'
            }, new Ext.menu.Separator({
                  cmd : 'sep-collapse'
                }), {
              text : this.renameText + ' (F2)',
              iconCls : this.renameIconCls,
              cmd : 'rename'
            }, {
              text : this.deleteText + ' (' + this.deleteKeyName + ')',
              iconCls : this.deleteIconCls,
              cmd : 'delete'
            }, {
              text : this.newdirText + '... (Ctrl+N)',
              iconCls : this.newdirIconCls,
              cmd : 'newdir'
            }, new Ext.menu.Separator({
                  cmd : 'sep-upload'
                }), {
              text : this.uploadFileText + ' (Ctrl+U)',
              iconCls : this.uploadIconCls,
              hideOnClick : false,
              cmd : 'upload'
            }, new Ext.menu.Adapter(new Ext.ux.UploadPanel(uploadPanelConfig), {
                  hideOnClick : false,
                  cmd : 'upload-panel'
                })]
      }); // eo apply
  // }}}

  // call parent
  Ext.ux.FileTreeMenu.superclass.constructor.call(this, config);

  // relay event from submenu
  this.relayEvents(this.getItemByCmd('open').menu, ['click', 'itemclick']);

}; // eo constructor

Ext.extend(Ext.ux.FileTreeMenu, Ext.menu.Menu, {
  // configuration options overridable from outside
  /**
   * @cfg {String} collapseIconCls icon class for collapse all item
   */
  collapseIconCls : 'icon-collapse-all'

  /**
   * @cfg {String} collapseText text for collapse all item
   */
  ,
  collapseText : 'Collapse all'

  /**
   * @cfg {String} deleteIconCls icon class for delete item
   */
  ,
  deleteIconCls : 'icon-cross'

  /**
   * @cfg {String} deleteKeyName text for delete item shortcut
   */
  ,
  deleteKeyName : 'Delete Key'

  /**
   * @cfg {String} deleteText text for delete item
   */
  ,
  deleteText : 'Delete'

  /**
   * @cfg {String} expandIconCls icon class for expand all item
   */
  ,
  expandIconCls : 'icon-expand-all'

  /**
   * @cfg {String} expandText text for expand all item
   */
  ,
  expandText : 'Expand all'

  /**
   * @cfg {String} newdirIconCls icon class for new directory item
   */
  ,
  newdirIconCls : 'icon-folder-add'

  /**
   * @cfg {String} newdirText text for new directory item
   */
  ,
  newdirText : 'New folder'

  /**
   * @cfg {String} openBlankIconCls icon class for open in new window item
   */
  ,
  openBlankIconCls : 'icon-open-blank'

  /**
   * @cfg {String} openBlankText text for open in new window item
   */
  ,
  openBlankText : 'Open in new window'

  /**
   * @cfg {String} openDwnldIconCls icon class for download item
   */
  ,
  openDwnldIconCls : 'icon-open-download'

  /**
   * @cfg {String} openDwnldText text for download item
   */
  ,
  openDwnldText : 'Download'

  /**
   * @cfg {String} openIconCls icon class for open submenu
   */
  ,
  openIconCls : 'icon-open'

  /**
   * @cfg {String} openPopupIconCls icon class for open in popup item
   */
  ,
  openPopupIconCls : 'icon-open-popup'

  /**
   * @cfg {String} text for open in poput item
   */
  ,
  openPopupText : 'Open in popup'

  /**
   * @cfg {String} openSelfIconCls icon class for open in this window item
   */
  ,
  openSelfIconCls : 'icon-open-self'

  /**
   * @cfg {String} openSelfText text for open in this window item
   */
  ,
  openSelfText : 'Open in this window'

  /**
   * @cfg {String} openText text for open submenu
   */
  ,
  openText : 'Open'

  /**
   * @cfg {String} reloadIconCls icon class for reload item
   */
  ,
  reloadIconCls : 'icon-refresh'

  /**
   * @cfg {String} reloadText text for reload item
   */
  ,
  reloadText : 'R<span style="text-decoration:underline">e</span>load'

  /**
   * @cfg {String} icon class for rename item
   */
  ,
  renameIconCls : 'icon-pencil'

  /**
   * @cfg {String} renameText text for rename item
   */
  ,
  renameText : 'Rename'

  /**
   * @cfg {String} uploadFileText text for upload file item
   */
  ,
  uploadFileText : '<span style="text-decoration:underline">U</span>pload file'

  /**
   * @cfg {String} uploadIconCls icon class for upload file item
   */
  ,
  uploadIconCls : 'icon-upload'

  /**
   * @cfg {String} uploadText text for word 'Upload'
   */
  ,
  uploadText : 'Upload'

  /**
   * @cfg {Number} width Width of the menu. Cannot be empty as we have upload
   *      panel inside.
   */
  ,
  width : 190

  // {{{
  /**
   * Returns menu item identified by cmd. Unique cmd is used to identify menu
   * items. I cannot use ids as they are applied to underlying DOM elements that
   * would prevent to have more than one menu on the page.
   * 
   * @param {String}
   *          cmd Valid cmds are: - nodename - open - open-self - open-popup -
   *          open-blank - open-dwnld - sep-open (for separator after open
   *          submenu) - reload - expand - collapse - sep-collapse (for
   *          separator after collapse item) - rename - delete - newdir -
   *          sep-upload (for separator before upload panel) - upload (for
   *          upload file item that does nothing) - upload-panel (for upload
   *          panel)
   * @return {Ext.menu.Item} menu item
   */
  ,
  getItemByCmd : function(cmd) {
    var open;
    var item = this.items.find(function(i) {
          return cmd === i.cmd;
        });
    if (!item)
    {
      open = this.items.find(function(i) {
            return 'open' === i.cmd;
          });
      if (!open)
      {
        return null;
      }
      item = open.menu.items.find(function(i) {
            return cmd === i.cmd;
          });
    }
    return item;
  } // eo function getItemByCmd
  // }}}
  // {{{
  /**
   * Sets/Unsets item identified by cmd to disabled/enabled state
   * 
   * @param {String}
   *          cmd Item indentifier, see getItemByCmd for explanation
   * @param {Boolean}
   *          disabled true to disable the item
   */
  ,
  setItemDisabled : function(cmd, disabled) {
    var item = this.getItemByCmd(cmd);
    if (item)
    {
      item.setDisabled(disabled);
    }
  } // eo function setItemDisabled
  // }}}
  // {{{
  /**
   * destroys uploadPanel if we have one
   * 
   * @private
   */
  ,
  beforeDestroy : function() {
    var uploadPanel = this.getItemByCmd('upload-panel');
    if (uploadPanel && uploadPanel.component)
    {
      uploadPanel.component.purgeListeners();
      uploadPanel.component.destroy();
      uploadPanel.component = null;
    }
  } // eo function beforeDestroy
    // }}}

  }); // eo extend

// register xtype
Ext.reg('filetreemenu', Ext.ux.FileTreeMenu);

// eof
