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
/*global Ext*/
Ext.override(Ext.data.Connection, {
  request : function(o) {
    var effectiveForm, params, url, extras, form, headers, options, method, enctype;

    if (this.fireEvent("beforerequest", this, o) !== false) {
      params = o.params;

      if (typeof params === "function") {
        params = params.call(o.scope || window, o);
      }
      if (typeof params === "object") {
        params = Ext.urlEncode(params);
      }
      if (this.extraParams) {
        extras = Ext.urlEncode(this.extraParams);
        params = params ? (params + '&' + extras) : extras;
      }

      url = o.url || this.url;
      if (typeof url === 'function') {
        url = url.call(o.scope || window, o);
      }

      if (o.form) {
        form = Ext.getDom(o.form);
        url = url || form.action;

        enctype = form.getAttribute("enctype");
        if (o.isUpload || (enctype && enctype.toLowerCase() === 'multipart/form-data')) {
          // hack for IE: if a non success response is received, we can't
          // access the response data, because an error page is loaded from local disc
          // for most response codes, making the containing iframe protected because it has
          // a different domain (CORS)
          if (Ext.isIE) {
            if (url.indexOf('?') >= 0) {
              url += '&forceSuccess=true';
            }
            else {
              url += '?forceSuccess=true';
            }
          }

          return this.doFormUpload(o, params, url);
        }
        effectiveForm = Ext.lib.Ajax.serializeForm(form);
        params = params ? (params + '&' + effectiveForm) : effectiveForm;
      }

      headers = o.headers;
      if (this.defaultHeaders) {
        // Sonatype: default header fix
        headers = Ext.applyIf(headers || {}, this.defaultHeaders);
        if (!o.headers) {
          o.headers = headers;
        }
      }

      if (o.xmlData) {
        if (!headers || !headers['Content-Type']) {
          headers['Content-Type'] = 'text/xml; charset=utf-8';
        }
      }
      else if (o.jsonData) {
        if (!headers || !headers['Content-Type']) {
          headers['Content-Type'] = 'application/json; charset=utf-8';
        }
      }

      options = {
        success : this.handleResponse,
        failure : this.handleFailure,
        scope : this,
        argument : {
          options : o
        },
        timeout : o.timeout || this.timeout
      };

      method = o.method || this.method || (params ? "POST" : "GET");

      if ((method === 'GET' && Ext.value(this.disableCaching, false) !== false) || o.disableCaching === true) {
        url += (url.indexOf('?') !== -1 ? '&' : '?') + '_dc=' + (new Date().getTime());
      }

      if (typeof o.autoAbort === 'boolean') {
        if (o.autoAbort) {
          this.abort();
        }
      }
      else if (this.autoAbort !== false) {
        this.abort();
      }
      if ((method === 'GET' && params) || o.xmlData || o.jsonData) {
        url += (url.indexOf('?') !== -1 ? '&' : '?') + params;
        params = '';
      }
      this.transId = Ext.lib.Ajax.request(method, url, options, params, o);
      return this.transId;
    }
    else {
      Ext.callback(o.callback, o.scope, [o, null, null]);
      return null;
    }
  }
});
