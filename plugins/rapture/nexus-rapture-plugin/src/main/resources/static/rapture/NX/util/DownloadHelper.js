/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Helper to facilitate browser-based file downloads.
 *
 * @since 3.0
 */
Ext.define('NX.util.DownloadHelper', {
  singleton: true,
  mixins: {
    logAware: 'NX.LogAware'
  },

  /**
   * @private
   *
   * ExtJS component identifier for nested iframe.
   */
  windowId: 'nx-download-frame',

  /**
   * @private
   *
   * Window names in IE are very picky, using '_' instead of '-' so that its its a valid javascript identifier.
   */
  windowName: 'nx_download_frame',

  /**
   * Get the hidden download frame.
   *
   * @private
   * @returns {Ext.Element}
   */
  getFrame: function() {
    var me = this, frame;

    // create the download frame if needed
    frame = Ext.get(me.windowId);
    if (!frame) {
      frame = Ext.getBody().createChild({
        tag: 'iframe',
        cls: 'x-hidden',
        id: me.windowId,
        name: me.windowName
      });
      me.logDebug('Created download-frame: ' + frame);
    }

    return frame;
  },

  /**
   * @public
   * @param {String} url URL to download
   * @return {Boolean} false if download was blocked, else true
   */
  downloadUrl: function (url) {
    var me = this,
        frame,
        win;

    me.logDebug('Downloading URL: ' + url);

    // resolve the download frame
    frame = me.getFrame();

    // TODO: Consider changing this to a dynamic form or 'a' link and automatically submit/click
    // TODO: ... to make use of html5 download attribute and avoid needing to _open_ more windows
    // TODO: Form method could be handy to GET/POST w/params vs link to just GET?

    // open new window in hidden download-from to initiate download
    win = window.open(url, me.windowName);
    if (win == null) {
      alert('Download window pop-up was blocked!');
      return false;
    }
    else {
      return true;
    }
  }
});
