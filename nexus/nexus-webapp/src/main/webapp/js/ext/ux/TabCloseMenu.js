/**
 * Very simple plugin for adding a close context menu to tabs.
 *
 * Copied from ExtJS 2.3 examples/tabs/TabCloseMenu.js
 *
 * @constructor
 */
/*global define*/
define(['extjs'], function(Ext){
Ext.ux.TabCloseMenu = function () {
  var tabs, menu, ctxItem;

  function onContextMenu(ts, item, e) {
    // create context menu on first right click
    if (!menu) {
      menu = new Ext.menu.Menu([
        {
          id: tabs.id + '-close',
          text: 'Close Tab',
          handler: function () {
            tabs.remove(ctxItem);
          }
        },
        {
          id: tabs.id + '-close-others',
          text: 'Close Other Tabs',
          handler: function () {
            tabs.items.each(function (item) {
              if (item.closable && item != ctxItem) {
                tabs.remove(item);
              }
            });
          }
        }
      ]);
    }
    ctxItem = item;
    var items = menu.items;
    items.get(tabs.id + '-close').setDisabled(!item.closable);

    // Disable close others options if there are no tabs which can be closed
    var disableCloseOthers = true;
    tabs.items.each(function () {
      if (this != item && this.closable) {
        disableCloseOthers = false;
        return false;
      }
    });
    items.get(tabs.id + '-close-others').setDisabled(disableCloseOthers);

    // If there is only one tab, then disable close (close others will also be disabled by ^^^)
    // FIXME: This is partially faulty since all tabs are closeable but really should disable closable for the last tab
    var disableClose = false;
    if (tabs.items.length === 1) {
      disableClose = true;
    }
    items.get(tabs.id + '-close').setDisabled(disableClose);

    menu.showAt(e.getPoint());
  }

  this.init = function (tp) {
    tabs = tp;
    tabs.on('contextmenu', onContextMenu);
  };
};
});
