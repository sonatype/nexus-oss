/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * URL related utils.
 *
 * @since 3.0
 */
Ext.define('NX.util.Url', {
  singleton: true,

  /**
   * Returns the base URL of the Nexus server.  URL never ends with '/'.
   *
   * @public
   */
  baseUrl: NX.app.baseUrl,

  /**
   * @public
   */
  urlOf: function (path) {
    var baseUrl = this.baseUrl;

    if (!Ext.isEmpty(path)) {
      if (Ext.String.endsWith(baseUrl, '/')) {
        baseUrl = baseUrl.substring(0, baseUrl.length - 1);
      }
      if (!Ext.String.startsWith(path, '/')) {
        path = '/' + path;
      }
      return baseUrl + path;
    }
    return this.baseUrl;
  },

  /**
   * @public
   * Creates a link.
   * @param {String} url to link to
   * @param {String} [text] link text. If omitted, defaults to url value.
   * @param {String} [target] link target. If omitted, defaults to '_blank'
   * @param {String} [id] link id
   */
  asLink: function (url, text, target, id) {
    target = target || '_blank';
    if (Ext.isEmpty(text)) {
      text = url;
    }
    if (id) {
      id = ' id="' + id + '"';
    } else {
      id = '';
    }
    return '<a href="' + url + '" target="' + target + '"' + id + '>' + text + '</a>';
  },

  /**
   * @public
   * Creates a link to an asset in a repository.
   * @param {String} repositoryName the repository housing the asset
   * @param {String} assetName name of the asset, expected to be the path within the repository
   */
  asRepositoryLink: function(repositoryName, assetName) {
    var me = this; 
    return me.asLink(me.baseUrl + '/repository/' + repositoryName  + assetName, assetName);
  }

});
