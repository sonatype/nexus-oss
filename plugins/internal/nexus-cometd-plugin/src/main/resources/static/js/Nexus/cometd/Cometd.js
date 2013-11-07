/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
 * Nexus CometD integration.
 *
 * @since 2.7
 */
NX.define('Nexus.cometd.Cometd', {
  extend: 'org.cometd.Cometd',
  requireSuper: false,
  singleton: true,

  requirejs: [
    'org/cometd',
    'org/cometd/AckExtension',
    'org/cometd/ReloadExtension',
    'org/cometd/TimeStampExtension',
    'org/cometd/TimeSyncExtension'
  ],

  mixins: [
    'Nexus.LogAwareMixin'
  ],

  /**
   * @constructor
   */
  constructor: function () {
    var me = this, url;

    // install extjs json codecs
    org.cometd.JSON.toJSON = Ext.encode;
    org.cometd.JSON.fromJSON = Ext.decode;

    me.constructor.superclass.constructor.call(me, 'default');

    url = Sonatype.config.host + Sonatype.config.contextPath + '/service/cometd/'; // trailing slash is important
    me.logDebug('URL: ' + url);

    Ext.ns('Ext.Cometd');

    // FIXME: copied from (asl2) https://code.google.com/p/ext-cometd/source/browse/trunk/src/main/webapp/js/ext-cometd/ext-cometd.js
    // FIXME: adapt to proper classes, once we get the basics working ................................................

    var ResponseTransformer = function (packet) {
      return function (response, options) {
        packet.onSuccess(response.responseText);
      }
    };

    var XHRAborter = function (xhr) {
      return {
        abort: function () {
          Ext.Ajax.abort(xhr);
        }
      }
    };

    Ext.Cometd.LongPollingTransport = function () {
      this.xhrSend = function (packet) {
        var xhr = Ext.Ajax.request({
          url: packet.url,
          method: 'POST',
          headers: packet.headers,
          jsonData: packet.body,
          failure: packet.onError,
          success: new ResponseTransformer(packet)
        });

        return new XHRAborter(xhr);
      }
    };
    Ext.Cometd.LongPollingTransport.prototype = new org.cometd.LongPollingTransport();
    Ext.Cometd.LongPollingTransport.prototype.constructor = Ext.Cometd.LongPollingTransport;

    Ext.Cometd.CallbackPollingTransport = function () {
      this.jsonpSend = function (packet) {
        var xhr = Ext.Ajax.request({
          url: packet.url,
          method: 'GET',
          headers: packet.headers,
          jsonp: 'jsonp',
          jsonData: {
            message: packet.body
          },
          failure: packet.onError,
          success: new ResponseTransformer(packet)
        });

        return new XHRAborter(xhr);
      }
    };
    Ext.Cometd.CallbackPollingTransport.prototype = new org.cometd.CallbackPollingTransport();
    Ext.Cometd.CallbackPollingTransport.prototype.constructor = Ext.Cometd.CallbackPollingTransport;

    // FIXME: end copy ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    // register transports
    if (org.cometd.WebSocket) {
      me.registerTransport('websocket', NX.create('org.cometd.WebSocketTransport'));
      me.logDebug('Websocket support detected');
    }
    me.registerTransport('long-polling', NX.create('Ext.Cometd.LongPollingTransport'));
    me.registerTransport('callback-polling', NX.create('Ext.Cometd.CallbackPollingTransport'));

    // register extensions
    me.registerExtension('ack', NX.create('org.cometd.AckExtension'));
    me.registerExtension('reload', NX.create('org.cometd.ReloadExtension'));
    me.registerExtension('timestamp', NX.create('org.cometd.TimeStampExtension'));
    me.registerExtension('timesync', NX.create('org.cometd.TimeSyncExtension'));

    // configure
    me.configure({
      url: url,
      logLevel: 'debug'
    });

    // HACK
    me.addListener('/meta/handshake', function (message)
    {
      if (message.successful) {
        me.logError('SUCCESS');
      }
      else {
        me.logError('FAILURE');
      }
    });

    me.handshake({
      // TODO
    });

    me.logDebug('Initialized');
  }
});