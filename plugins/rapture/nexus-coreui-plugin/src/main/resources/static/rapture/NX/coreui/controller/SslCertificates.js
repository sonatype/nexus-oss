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
/*global Ext, NX*/

/**
 * Ssl Certificates controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.SslCertificates', {
  extend: 'NX.controller.MasterDetail',

  list: 'nx-coreui-sslcertificate-list',

  models: [
    'SslCertificate'
  ],
  stores: [
    'SslCertificate'
  ],
  views: [
    'ssl.SslCertificateAddFromPem',
    'ssl.SslCertificateAddFromServer',
    'ssl.SslCertificateDetails',
    'ssl.SslCertificateDetailsWindow',
    'ssl.SslCertificateFeature',
    'ssl.SslCertificateList'
  ],
  refs: [
    {
      ref: 'list',
      selector: 'nx-coreui-sslcertificate-list'
    },
    {
      ref: 'details',
      selector: 'nx-coreui-sslcertificate-feature nx-coreui-sslcertificate-details'
    }
  ],
  icons: {
    'sslcertificate-default': {
      file: 'ssl_certificates.png',
      variants: ['x16', 'x32']
    },
    'sslcertificate-add-by-pem': {
      file: 'server_add.png',
      variants: ['x16', 'x32']
    },
    'sslcertificate-add-by-server': {
      file: 'server_connect.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/Security/SSL Certificates',
    view: { xtype: 'nx-coreui-sslcertificate-feature' },
    description: 'Manage trusted SSL certificates for use with the Nexus Trust Store',
    iconConfig: {
      file: 'ssl_certificates.png',
      variants: ['x16', 'x32']
    },
    visible: function () {
      return NX.Permissions.check('nexus:ssl:truststore', 'read');
    }
  },
  permission: 'nexus:ssl:truststore',

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.callParent();

    me.listen({
      component: {
        'nx-coreui-sslcertificate-list menuitem[action=newfromserver]': {
          click: me.showAddWindowFromServer
        },
        'nx-coreui-sslcertificate-list menuitem[action=newfrompem]': {
          click: me.showAddWindowFromPem
        },
        'nx-coreui-sslcertificate-add-from-pem button[action=load]': {
          click: me.loadCertificateByPem
        },
        'nx-coreui-sslcertificate-add-from-server button[action=load]': {
          click: me.loadCertificateByServer
        },
        'nx-coreui-sslcertificate-details-window button[action=add]': {
          click: me.create
        },
        'nx-coreui-sslcertificate-details-window button[action=remove]': {
          click: me.remove
        }
      }
    });
  },

  /**
   * @override
   */
  getDescription: function (model) {
    return model.get('subjectCommonName');
  },

  /**
   * @override
   */
  onSelection: function (list, model) {
    var me = this;

    if (Ext.isDefined(model)) {
      me.getDetails().loadRecord(model);
    }
  },

  /**
   * @private
   */
  showAddWindowFromServer: function () {
    Ext.widget('nx-coreui-sslcertificate-add-from-server');
  },

  /**
   * @private
   */
  showAddWindowFromPem: function () {
    Ext.widget('nx-coreui-sslcertificate-add-from-pem');
  },

  /**
   * @private
   * Shows certificate details window.
   */
  showCertificateDetails: function (certificate) {
    var me = this,
        window = Ext.widget('nx-coreui-sslcertificate-details-window'),
        form = window.down('form'),
        model = me.getSslCertificateModel().create(certificate);

    form.loadRecord(model);
  },

  /**
   * @private
   * Retrieves details of a certificate specified by PEM, showing the certificate details if successful.
   */
  loadCertificateByPem: function (button) {
    var me = this,
        win = button.up('window'),
        pem = button.up('form').getForm().getFieldValues().pem;

    win.getEl().mask('Loading certificate...');
    NX.direct.ssl_Certificate.details({ value: pem }, function (response) {
      win.getEl().unmask();
      if (Ext.isObject(response) && response.success) {
        win.close();
        me.showCertificateDetails(response.data);
      }
    });
  },

  /**
   * @private
   * Retrieves certificate from host, showing the certificate details if successful.
   */
  loadCertificateByServer: function (button) {
    var me = this,
        win = button.up('window'),
        parsed = me.parseHostAndPort(button.up('form').getForm().getFieldValues().server);

    win.getEl().mask('Loading certificate...');
    NX.direct.ssl_Certificate.retrieveFromHost(parsed[0], parsed[1], undefined, function (response) {
      win.getEl().unmask();
      if (Ext.isObject(response) && response.success) {
        win.close();
        me.showCertificateDetails(response.data);
      }
    });
  },

  /**
   * @private
   * Parses server string into an array of [host,port].
   */
  parseHostAndPort: function (server) {
    var rx, matches;

    if (!server) {
      return [];
    }
    if (server.indexOf(":") === -1) { // neither URL nor host:port
      return [server];
    }
    if (server.indexOf("/") === -1) { // definitely no URL
      server.split(":");
    }

    rx = /[^:]*:\/\/([^:\/]*)(:([0-9]*))?/;
    matches = server.match(rx);
    if (matches) {
      return [matches[1], Ext.Number.from(matches[3], undefined)];
    }

    return [server];
  },

  /**
   * @private
   * Creates an SSL certificate.
   */
  create: function (button) {
    var me = this,
        win = button.up('window'),
        form = button.up('form'),
        model = form.getRecord(),
        description = me.getDescription(model);

    NX.direct.ssl_TrustStore.create({ value: model.get('pem') }, function (response) {
      if (Ext.isObject(response) && response.success) {
        win.close();
        me.loadStore();
        NX.Messages.add({ text: 'SSL Certificate created: ' + description, type: 'success' });
      }
    });
  },

  /**
   * @private
   * Removes an SSL certificate.
   */
  remove: function (button) {
    var me = this,
        win = button.up('window'),
        form = button.up('form'),
        model = form.getRecord(),
        description = me.getDescription(model);

    NX.direct.ssl_TrustStore.delete_(model.getId(), function (response) {
      if (Ext.isObject(response) && response.success) {
        win.close();
        me.loadStore();
        NX.Messages.add({ text: 'SSL Certificate deleted: ' + description, type: 'success' });
      }
    });
  },

  /**
   * @private
   * @override
   * Deletes an SSL certificate.
   * @param {NX.coreui.model.SslCertificate} model to be deleted
   */
  deleteModel: function (model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.ssl_TrustStore.delete_(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({ text: 'SSL Certificate deleted: ' + description, type: 'success' });
      }
    });
  }

});