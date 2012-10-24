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
/*global define, top*/
define(['extjs'], function(Ext){
// needed to override whole history singleton to make 1 simple change (see
// comment in startUp method)
Ext.History = (function() {
  var iframe, hiddenField, ready = false, currentToken, doc;

  function getHash() {
    var href = top.location.href, i = href.indexOf("#");
    return i >= 0 ? href.substr(i + 1) : null;
  }

  function doSave() {
    hiddenField.value = currentToken;
  }

  function handleStateChange(token) {
    currentToken = token;
    Ext.History.fireEvent('change', token);
  }

  function updateIFrame(token) {
    var html = ['<html><body><div id="state">', token, '</div></body></html>'].join('');
    try
    {
      doc = iframe.contentWindow.document;
      doc.open();
      doc.write(html);
      doc.close();
      return true;
    }
    catch (e)
    {
      return false;
    }
  }

  function checkIFrame() {
    if (!iframe.contentWindow || !iframe.contentWindow.document)
    {
      setTimeout(checkIFrame, 10);
      return;
    }

    var
          doc = iframe.contentWindow.document,
          elem = doc.getElementById("state"),
          token = elem ? elem.innerText : null,
          hash = getHash();

    setInterval(function() {

      doc = iframe.contentWindow.document;
      elem = doc.getElementById("state");

      var newtoken = elem ? elem.innerText : null, newHash = getHash();

      if (newtoken !== token)
      {
        token = newtoken;
        handleStateChange(token);
        top.location.hash = token;
        hash = token;
        doSave();
      }
      else if (newHash !== hash)
      {
        hash = newHash;
        updateIFrame(newHash);
      }

    }, 50);

    ready = true;

    Ext.History.fireEvent('ready', Ext.History);
  }

  function startUp() {
    // fix bug that was fixed in some version of ext newer than we have now
    // currentToken = hiddenField.value;
    currentToken = hiddenField.value || getHash();

    if (Ext.isIE)
    {
      checkIFrame();
    }
    else
    {
      var hash = getHash();
      setInterval(function() {
        var newHash = getHash();
        if (newHash !== hash)
        {
          hash = newHash;
          handleStateChange(hash);
          doSave();
        }
      }, 50);
      ready = true;
      Ext.History.fireEvent('ready', Ext.History);
    }
  }

  return {

    fieldId : 'x-history-field',

    iframeId : 'x-history-frame',

    events : {},

    init : function(onReady, scope) {
      if (ready)
      {
        Ext.callback(onReady, scope, [this]);
        return;
      }
      if (!Ext.isReady)
      {
        Ext.onReady(function() {
          Ext.History.init(onReady, scope);
        });
        return;
      }
      hiddenField = Ext.getDom(Ext.History.fieldId);
      if (Ext.isIE)
      {
        iframe = Ext.getDom(Ext.History.iframeId);
      }
      this.addEvents('ready', 'change');
      if (onReady)
      {
        this.on('ready', onReady, scope, {
          single : true
        });
      }
      startUp();
    },

    add : function(token, preventDup) {
      if (preventDup !== false)
      {
        if (this.getToken() === token)
        {
          return true;
        }
      }
      if (Ext.isIE)
      {
        return updateIFrame(token);
      }
      else
      {
        top.location.hash = token;
        return true;
      }
    },

    back : function() {
      history.go(-1);
    },

    forward : function() {
      history.go(1);
    },

    getToken : function() {
      return ready ? currentToken : getHash();
    }
  };
}());
Ext.apply(Ext.History, new Ext.util.Observable());
});
