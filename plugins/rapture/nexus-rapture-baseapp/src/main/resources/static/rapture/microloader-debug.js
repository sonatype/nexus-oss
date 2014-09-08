/*
 This file is part of Ext JS 5.0.1.1255

 Copyright (c) 2011-2014 Sencha Inc

 Contact:  http://www.sencha.com/contact

 Commercial Usage
 Licensees holding valid commercial licenses may use this file in accordance with the Commercial
 Software License Agreement provided with the Software or, alternatively, in accordance with the
 terms contained in a written agreement between you and Sencha.

 If you are unsure which license is appropriate for your use, please contact the sales department
 at http://www.sencha.com/contact.

 Version: 5.0.1.1255 Build date: 2014-08-05 20:18:44 (2852ec9b146a917f790d13cfa9b9c2fa041fccf8)

 */





var Ext = Ext || {};



Ext.Boot = (function (emptyFn) {

  var doc = document,
      apply = function (dest, src, defaults) {
        if (defaults) {
          apply(dest, defaults);
        }

        if (dest && src && typeof src == 'object') {
          for (var key in src) {
            dest[key] = src[key];
          }
        }
        return dest;
      },
      _config = {

        disableCaching: (/[?&](?:cache|disableCacheBuster)\b/i.test(location.search) ||
            !(/http[s]?\:/i.test(location.href)) ||
            /(^|[ ;])ext-cache=1/.test(doc.cookie)) ? false :
            true,


        disableCachingParam: '_dc',


        loadDelay: false,


        preserveScripts: true,


        charset: undefined
      },

      cssRe = /\.css(?:\?|$)/i,
      resolverEl = doc.createElement('a'),
      isBrowser = typeof window !== 'undefined',
      _environment = {
        browser: isBrowser,
        node: !isBrowser && (typeof require === 'function'),
        phantom: (typeof phantom !== 'undefined' && phantom.fs)
      },
      _tags = {},


      _debug = function (message) {

      },

      _apply = function (object, config, defaults) {
        if (defaults) {
          _apply(object, defaults);
        }
        if (object && config && typeof config === 'object') {
          for (var i in config) {
            object[i] = config[i];
          }
        }
        return object;
      },

      Boot = {
        loading: 0,
        loaded: 0,
        env: _environment,
        config: _config,



        scripts: {

        },


        currentFile: null,
        suspendedQueue: [],
        currentRequest: null,



        syncMode: false,



        debug: _debug,

        listeners: [],

        Request: Request,

        Entry: Entry,

        platformTags: _tags,


        detectPlatformTags: function () {
          var ua = navigator.userAgent,
              isMobile = _tags.isMobile = /Mobile(\/|\s)/.test(ua),
              isPhone, isDesktop, isTablet, touchSupported, isIE10, isBlackberry,
              element = document.createElement('div'),
              uaTagChecks = [
                'iPhone',
                'iPod',
                'Android',
                'Silk',
                'Android 2',
                'BlackBerry',
                'BB',
                'iPad',
                'RIM Tablet OS',
                'MSIE 10',
                'Trident',
                'Chrome',
                'Tizen',
                'Firefox',
                'Safari',
                'Windows Phone'
              ],
              isEventSupported = function(name, tag) {
                if (tag === undefined) {
                  tag = window;
                }

                var eventName = 'on' + name.toLowerCase(),
                    isSupported = (eventName in element);

                if (!isSupported) {
                  if (element.setAttribute && element.removeAttribute) {
                    element.setAttribute(eventName, '');
                    isSupported = typeof element[eventName] === 'function';

                    if (typeof element[eventName] !== 'undefined') {
                      element[eventName] = undefined;
                    }

                    element.removeAttribute(eventName);
                  }
                }

                return isSupported;
              },
              uaTags = {},
              len = uaTagChecks.length, check, c;

          for (c = 0; c < len; c++) {
            check = uaTagChecks[c];
            uaTags[check] = new RegExp(check).test(ua);
          }

          isPhone =
              (uaTags.iPhone || uaTags.iPod) ||
              (!uaTags.Silk && (uaTags.Android && (uaTags['Android 2'] || isMobile))) ||
              ((uaTags.BlackBerry || uaTags.BB) && uaTags.isMobile) ||
              (uaTags['Windows Phone']);

          isTablet =
              (!_tags.isPhone) && (
              uaTags.iPad ||
              uaTags.Android ||
              uaTags.Silk ||
              uaTags['RIM Tablet OS'] ||
              (uaTags['MSIE 10'] && /; Touch/.test(ua))
              );

          touchSupported =


              isEventSupported('touchend') ||



              navigator.maxTouchPoints ||

              navigator.msMaxTouchPoints;

          isDesktop = !isPhone && !isTablet;
          isIE10 = uaTags['MSIE 10'];
          isBlackberry = uaTags.Blackberry || uaTags.BB;

          apply(_tags, Boot.loadPlatformsParam(), {
            phone: isPhone,
            tablet: isTablet,
            desktop: isDesktop,
            touch: touchSupported,
            ios: (uaTags.iPad || uaTags.iPhone || uaTags.iPod),
            android: uaTags.Android || uaTags.Silk,
            blackberry: isBlackberry,
            safari: uaTags.Safari && isBlackberry,
            chrome: uaTags.Chrome,
            ie10: isIE10,
            windows: isIE10 || uaTags.Trident,
            tizen: uaTags.Tizen,
            firefox: uaTags.Firefox
          });
        },


        loadPlatformsParam: function () {

          var paramsString = window.location.search.substr(1),
              paramsArray = paramsString.split("&"),
              params = {}, i,
              platforms = {},
              tmpArray, tmplen, platform, name, enabled;

          for (i = 0; i < paramsArray.length; i++) {
            tmpArray = paramsArray[i].split("=");
            params[tmpArray[0]] = tmpArray[1];
          }

          if (params.platformTags) {
            tmpArray = params.platform.split(/\W/);
            for (tmplen = tmpArray.length, i = 0; i < tmplen; i++) {
              platform = tmpArray[i].split(":");
              name = platform[0];
              if (platform.length > 1) {
                enabled = platform[1];
                if (enabled === 'false' || enabled === '0') {
                  enabled = false;
                } else {
                  enabled = true;
                }
              }
              platforms[name] = enabled;
            }
          }
          return platform;
        },

        getPlatformTags: function () {
          return Boot.platformTags;
        },

        filterPlatform: function (platform) {
          platform = [].concat(platform);
          var tags = Boot.getPlatformTags(),
              len, p, tag;

          for (len = platform.length, p = 0; p < len; p++) {
            tag = platform[p];
            if (tags.hasOwnProperty(tag)) {
              return !!tags[tag];
            }
          }
          return false;
        },

        init: function () {
          var me = this,
              scriptEls = doc.getElementsByTagName('script'),
              len = scriptEls.length,
              re = /\/ext(\-[a-z\-]+)?\.js$/,
              entry, script, src, state, baseUrl, key, n, origin;




          for (n = 0; n < len; n++) {
            src = (script = scriptEls[n]).src;
            if (!src) {
              continue;
            }
            state = script.readyState || null;


            if (!baseUrl) {
              if (re.test(src)) {
                me.hasReadyState = ("readyState" in script);
                me.hasAsync = ("async" in script) || !me.hasReadyState;
                baseUrl = src;
              }
            }

            if (!me.scripts[key = me.canonicalUrl(src)]) {

              _debug("creating entry " + key + " in Boot.init");

              entry = new Entry({
                key: key,
                url: src,
                done: state === null ||
                    state === 'loaded' || state === 'complete',
                el: script,
                prop: 'src'
              });
            }
          }

          if (!baseUrl) {
            script = scriptEls[scriptEls.length - 1];
            baseUrl = script.src;
            me.hasReadyState = ('readyState' in script);
            me.hasAsync = ("async" in script) || !me.hasReadyState;
          }

          me.baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1);
          origin = window.location.origin ||
              window.location.protocol +
              "//" +
              window.location.hostname +
              (window.location.port ? ':' + window.location.port: '');
          me.origin = origin;

          me.detectPlatformTags();
          Ext.filterPlatform = me.filterPlatform;
        },


        canonicalUrl: function (url) {


          resolverEl.href = url;

          var ret = resolverEl.href,
              dc = _config.disableCachingParam,
              pos = dc ? ret.indexOf(dc + '=') : -1,
              c, end;



          if (pos > 0 && ((c = ret.charAt(pos - 1)) === '?' || c === '&')) {
            end = ret.indexOf('&', pos);
            end = (end < 0) ? '' : ret.substring(end);
            if (end && c === '?') {
              ++pos;
              end = end.substring(1);
            }
            ret = ret.substring(0, pos - 1) + end;
          }

          return ret;
        },


        getConfig: function (name) {
          return name ? this.config[name] : this.config;
        },


        setConfig: function (name, value) {
          if (typeof name === 'string') {
            this.config[name] = value;
          } else {
            for (var s in name) {
              this.setConfig(s, name[s]);
            }
          }
          return this;
        },

        getHead: function () {
          return this.docHead ||
              (this.docHead = doc.head ||
                  doc.getElementsByTagName('head')[0]);
        },

        create: function (url, key, cfg) {
          var config = cfg || {};
          config.url = url;
          config.key = key;
          return this.scripts[key] = new Entry(config);
        },

        getEntry: function (url, cfg) {
          var key = this.canonicalUrl(url),
              entry = this.scripts[key];
          if (!entry) {
            entry = this.create(url, key, cfg);
          }
          return entry;
        },

        processRequest: function(request, sync) {
          request.loadEntries(sync);
        },

        load: function (request) {

          _debug("Boot.load called");

          var me = this,
              request = new Request(request);

          if(request.sync || me.syncMode) {
            return me.loadSync(request);
          }



          if (me.currentRequest) {

            _debug("current active request, suspending this request");




            request.getEntries();
            me.suspendedQueue.push(request);
          } else {
            me.currentRequest = request;
            me.processRequest(request, false);
          }
          return me;
        },

        loadSync: function (request) {

          _debug("Boot.loadSync called");

          var me = this,
              request = new Request(request);

          me.syncMode++;
          me.processRequest(request, true);
          me.syncMode--;
          return me;
        },

        loadBasePrefix: function(request) {
          request = new Request(request);
          request.prependBaseUrl = true;
          return this.load(request);
        },

        loadSyncBasePrefix: function(request) {
          request = new Request(request);
          request.prependBaseUrl = true;
          return this.loadSync(request);
        },

        requestComplete: function(request) {
          var me = this,
              next;
          if(me.currentRequest === request) {
            me.currentRequest = null;
            while(me.suspendedQueue.length > 0) {
              next = me.suspendedQueue.shift();
              if(!next.done) {

                _debug("resuming suspended request");

                me.load(next);
                break;
              }
            }
          }
          if(!me.currentRequest && me.suspendedQueue.length == 0) {
            me.fireListeners();
          }
        },

        isLoading: function () {
          return !this.currentRequest && this.suspendedQueue.length == 0;
        },

        fireListeners: function () {
          var listener;
          while (this.isLoading() && (listener = this.listeners.shift())) {
            listener();
          }
        },

        onBootReady: function (listener) {
          if (!this.isLoading()) {
            listener();
          } else {
            this.listeners.push(listener);
          }
        },


        getPathsFromIndexes: function (indexMap, loadOrder) {
          return Request.prototype.getPathsFromIndexes(indexMap, loadOrder);
        },

        createLoadOrderMap: function(loadOrder) {
          return Request.prototype.createLoadOrderMap(loadOrder);
        },

        fetchSync: function(url) {
          var exception, xhr, status, content;

          exception = false;
          xhr = new XMLHttpRequest();

          try {
            xhr.open('GET', url, false);
            xhr.send(null);
          } catch (e) {
            exception = true;
          }

          status = (xhr.status === 1223) ? 204 :
              (xhr.status === 0 && ((self.location || {}).protocol === 'file:' ||
                  (self.location || {}).protocol === 'ionp:')) ? 200 : xhr.status;
          content = xhr.responseText;

          xhr = null;

          return {
            content: content,
            exception: exception,
            status: status
          };
        },

        notifyAll: function(entry) {
          entry.notifyRequests();
        }
      };


  function Request(cfg) {
    if(cfg.$isRequest) {
      return cfg;
    }

    var cfg = cfg.url ? cfg : {url: cfg},
        url = cfg.url,
        urls = url.charAt ? [ url ] : url,
        boot = cfg.boot || Boot,
        charset = cfg.charset || boot.config.charset,
        buster = (('cache' in cfg) ? !cfg.cache : boot.config.disableCaching) &&
            (boot.config.disableCachingParam + '=' + new Date().getTime());
    _apply(cfg, {
      urls: urls,
      boot: boot,
      charset: charset,
      buster: buster
    });
    _apply(this, cfg);
  };
  Request.prototype = {
    $isRequest: true,


    createLoadOrderMap: function (loadOrder) {
      var len = loadOrder.length,
          loadOrderMap = {},
          i, element;

      for (i = 0; i < len; i++) {
        element = loadOrder[i];
        loadOrderMap[element.path] = element;
      }

      return loadOrderMap;
    },


    getLoadIndexes: function (index, indexMap, loadOrder, includeUses, skipLoaded) {
      var item = loadOrder[index],
          len, i, reqs, entry, stop, added, idx, ridx, url;

      if (indexMap[index]) {

        return indexMap;
      }

      indexMap[index] = true;

      stop = false;
      while (!stop) {
        added = false;



        for (idx in indexMap) {
          if (indexMap.hasOwnProperty(idx)) {
            item = loadOrder[idx];
            if (!item) {
              continue;
            }
            url = this.prepareUrl(item.path);
            entry = Boot.getEntry(url);
            if (!skipLoaded || !entry || !entry.done) {
              reqs = item.requires;
              if (includeUses && item.uses) {
                reqs = reqs.concat(item.uses);
              }
              for (len = reqs.length, i = 0; i < len; i++) {
                ridx = reqs[i];




                if (!indexMap[ridx]) {
                  indexMap[ridx] = true;
                  added = true;
                }
              }
            }
          }
        }



        if (!added) {
          stop = true;
        }
      }

      return indexMap;
    },

    getPathsFromIndexes: function (indexMap, loadOrder) {
      var indexes = [],
          paths = [],
          index, len, i;

      for (index in indexMap) {
        if (indexMap.hasOwnProperty(index) && indexMap[index]) {
          indexes.push(index);
        }
      }

      indexes.sort(function (a, b) {
        return a - b;
      });


      for (len = indexes.length, i = 0; i < len; i++) {
        paths.push(loadOrder[indexes[i]].path);
      }

      return paths;
    },

    expandUrl: function (url, indexMap, includeUses, skipLoaded) {
      if (typeof url == 'string') {
        url = [url];
      }

      var me = this,
          loadOrder = me.loadOrder,
          loadOrderMap = me.loadOrderMap;

      if (loadOrder) {
        loadOrderMap = loadOrderMap || me.createLoadOrderMap(loadOrder);
        me.loadOrderMap = loadOrderMap;
        indexMap = indexMap || {};
        var len = url.length,
            unmapped = [],
            i, item;

        for (i = 0; i < len; i++) {
          item = loadOrderMap[url[i]];
          if (item) {
            me.getLoadIndexes(item.idx, indexMap, loadOrder, includeUses, skipLoaded);
          } else {
            unmapped.push(url[i]);
          }
        }


        return me.getPathsFromIndexes(indexMap, loadOrder).concat(unmapped);
      }
      return url;
    },

    expandUrls: function (urls, includeUses) {
      if (typeof urls == "string") {
        urls = [urls];
      }

      var expanded = [],
          expandMap = {},
          tmpExpanded,
          len = urls.length,
          i, t, tlen, tUrl;

      for (i = 0; i < len; i++) {
        tmpExpanded = this.expandUrl(urls[i], {}, includeUses, true);
        for (t = 0, tlen = tmpExpanded.length; t < tlen; t++) {
          tUrl = tmpExpanded[t];
          if (!expandMap[tUrl]) {
            expandMap[tUrl] = true;
            expanded.push(tUrl);
          }
        }
      }

      if (expanded.length == 0) {
        expanded = urls;
      }

      return expanded;
    },

    expandLoadOrder: function () {
      var me = this,
          urls = me.urls,
          expanded;

      if (!me.expanded) {
        expanded = this.expandUrls(urls);
        me.expanded = true;
      } else {
        expanded = urls;
      }

      me.urls = expanded;



      if (urls.length != expanded.length) {
        me.sequential = true;
      }

      return me;
    },

    getUrls: function () {
      this.expandLoadOrder();
      return this.urls;
    },

    prepareUrl: function(url) {
      if(this.prependBaseUrl) {
        return Boot.baseUrl + url;
      }
      return url;
    },

    getEntries: function () {
      var me = this,
          entries = me.entries,
          i, entry, urls, url;
      if (!entries) {
        entries = [];
        urls = me.getUrls();
        for (i = 0; i < urls.length; i++) {
          url = me.prepareUrl(urls[i]);
          entry = Boot.getEntry(url, {
            buster: me.buster,
            charset: me.charset
          });
          entry.requests.push(me);
          entries.push(entry);
        }
        me.entries = entries;
      }
      return entries;
    },

    loadEntries: function(sync) {
      var me = this,
          entries = me.getEntries(),
          len = entries.length,
          start = me.loadStart || 0,
          continueLoad, entry, i;

      if(sync !== undefined) {
        me.sync = sync;
      }

      me.loaded = me.loaded || 0;
      me.loading = me.loading || len;

      for(i = start; i < len; i++) {
        entry = entries[i];
        if(!entry.loaded) {
          continueLoad = entries[i].load(me.sync);
        } else {
          continueLoad = true;
        }
        if(!continueLoad) {
          me.loadStart = i;
          entry.onDone(function(){
            me.loadEntries(sync);
          });
          break;
        }
      }
      me.processLoadedEntries();
    },

    processLoadedEntries: function () {
      var me = this,
          entries = me.getEntries(),
          len = entries.length,
          start = me.startIndex || 0,
          i, entry;

      if (!me.done) {
        for (i = start; i < len; i++) {
          entry = entries[i];

          if (!entry.loaded) {
            me.startIndex = i;
            return;
          }

          if (!entry.evaluated) {
            entry.evaluate();
          }

          if (entry.error) {
            me.error = true;
          }
        }
        me.notify();
      }
    },

    notify: function () {
      var me = this;
      if (!me.done) {
        var error = me.error,
            fn = me[error ? 'failure' : 'success'],
            delay = ('delay' in me)
                ? me.delay
                : (error ? 1 : Boot.config.chainDelay),
            scope = me.scope || me;
        me.done = true;
        if (fn) {
          if (delay === 0 || delay > 0) {

            setTimeout(function () {
              fn.call(scope, me);
            }, delay);
          } else {
            fn.call(scope, me);
          }
        }
        me.fireListeners();
        Boot.requestComplete(me);
      }
    },

    onDone: function(listener) {
      var me = this,
          listeners = me.listeners || (me.listeners = []);
      if(me.done) {
        listener(me);
      } else {
        listeners.push(listener);
      }
    },

    fireListeners: function() {
      var listeners = this.listeners,
          listener;
      if(listeners) {

        _debug("firing request listeners");

        while((listener = listeners.shift())) {
          listener(this);
        }
      }
    }
  };


  function Entry(cfg) {
    if(cfg.$isEntry) {
      return cfg;
    }


    _debug("creating entry for " + cfg.url);


    var boot = cfg.boot || Boot,
        charset = cfg.charset || boot.config.charset,
        buster = cfg.buster || ((('cache' in cfg) ? !cfg.cache : boot.config.disableCaching) &&
            (boot.config.disableCachingParam + '=' + new Date().getTime()));

    _apply(cfg, {
      boot: boot,
      charset: charset,
      buster: buster,
      requests: []
    });
    _apply(this, cfg);
  };
  Entry.prototype = {
    $isEntry: true,
    done: false,
    evaluated: false,
    loaded: false,

    isCrossDomain: function() {
      var me = this;
      if(me.crossDomain === undefined) {

        _debug("checking " + me.getLoadUrl() + " for prefix " + Boot.origin);

        me.crossDomain = (me.getLoadUrl().indexOf(Boot.origin) !== 0);
      }
      return me.crossDomain;
    },

    isCss: function () {
      var me = this;
      if (me.css === undefined) {
        me.css = me.url && cssRe.test(me.url);
      }
      return this.css;
    },

    getElement: function (tag) {
      var me = this,
          el = me.el;
      if (!el) {

        _debug("creating element for " + me.url);

        if (me.isCss()) {
          tag = tag || "link";
          el = doc.createElement(tag);
          if(tag == "link") {
            el.rel = 'stylesheet';
            me.prop = 'href';
          } else {
            me.prop="textContent";
          }
          el.type = "text/css";
        } else {
          tag = tag || "script";
          el = doc.createElement(tag);
          el.type = 'text/javascript';
          me.prop = 'src';
          if (Boot.hasAsync) {
            el.async = false;
          }
        }
        me.el = el;
      }
      return el;
    },

    getLoadUrl: function () {
      var me = this,
          url = Boot.canonicalUrl(me.url);
      if (!me.loadUrl) {
        me.loadUrl = !!me.buster
            ? (url + (url.indexOf('?') === -1 ? '?' : '&') + me.buster)
            : url;
      }
      return me.loadUrl;
    },

    fetch: function (req) {
      var url = this.getLoadUrl(),
          async = !!req.async,
          xhr = new XMLHttpRequest(),
          complete = req.complete,
          status, content, exception = false,
          readyStateChange = function () {
            if (xhr && xhr.readyState == 4) {
              if (complete) {
                status = (xhr.status === 1223) ? 204 :
                    (xhr.status === 0 && ((self.location || {}).protocol === 'file:' ||
                        (self.location || {}).protocol === 'ionp:')) ? 200 : xhr.status;
                content = xhr.responseText;
                complete({
                  content: content,
                  status: status,
                  exception: exception
                });
              }
              xhr = null;
            }
          };

      async = !!async;

      if(async) {
        xhr.onreadystatechange = readyStateChange;
      }

      try {

        _debug("fetching " + url + " " + (async ? "async" : "sync"));

        xhr.open('GET', url, async);
        xhr.send(null);
      } catch (err) {
        exception = err;
        readyStateChange();
      }

      if(!async) {
        readyStateChange();
      }
    },

    onContentLoaded: function (response) {
      var me = this,
          status = response.status,
          content = response.content,
          exception = response.exception,
          url = this.getLoadUrl();
      me.loaded = true;
      if ((exception || status === 0) && !_environment.phantom) {
        me.error =

            ("Failed loading synchronously via XHR: '" + url +
                "'. It's likely that the file is either being loaded from a " +
                "different domain or from the local file system where cross " +
                "origin requests are not allowed for security reasons. Try " +
                "asynchronous loading instead.") ||

            true;
        me.evaluated = true;
      }
      else if ((status >= 200 && status < 300) || status === 304
          || _environment.phantom
          || (status === 0 && content.length > 0)
          ) {
        me.content = content;
      }
      else {
        me.error =

            ("Failed loading synchronously via XHR: '" + url +
                "'. Please verify that the file exists. XHR status code: " +
                status) ||

            true;
        me.evaluated = true;
      }
    },

    createLoadElement: function(callback) {
      var me = this,
          el = me.getElement(),
          readyStateChange = function(){
            if (this.readyState === 'loaded' || this.readyState === 'complete') {
              if(callback) {
                callback();
              }
            }
          },
          errorFn = function() {
            me.error = true;
            if(callback) {
              callback();
            }
          };
      me.preserve = true;
      el.onerror = errorFn;
      if(Boot.hasReadyState) {
        el.onreadystatechange = readyStateChange;
      } else {
        el.onload = callback;
      }

      el[me.prop] = me.getLoadUrl();
    },

    onLoadElementReady: function() {
      Boot.getHead().appendChild(this.getElement());
      this.evaluated = true;
    },

    inject: function (content, asset) {

      _debug("injecting content for " + this.url);

      var me = this,
          head = Boot.getHead(),
          url = me.url,
          key = me.key,
          base, el, ieMode, basePath;

      if (me.isCss()) {
        me.preserve = true;
        basePath = key.substring(0, key.lastIndexOf("/") + 1);
        base = doc.createElement('base');
        base.href = basePath;
        if(head.firstChild) {
          head.insertBefore(base, head.firstChild);
        } else {
          head.appendChild(base);
        }

        base.href = base.href;

        if (url) {
          content += "\n/*# sourceURL=" + key + " */";
        }


        el = me.getElement("style");

        ieMode = ('styleSheet' in el);

        head.appendChild(base);
        if(ieMode) {
          head.appendChild(el);
          el.styleSheet.cssText = content;
        } else {
          el.textContent = content;
          head.appendChild(el);
        }
        head.removeChild(base);

      } else {



        if (url) {
          content += "\n//# sourceURL=" + key;
        }
        Ext.globalEval(content);
      }
      return me;
    },

    loadCrossDomain: function() {
      var me = this,
          complete = function(){
            me.loaded = me.evaluated = me.done = true;
            me.notifyRequests();
          };
      if(me.isCss()) {
        me.createLoadElement();
        me.evaluateLoadElement();
        complete();
      } else {
        me.createLoadElement(function(){
          complete();
        });
        me.evaluateLoadElement();



        return false;
      }
      return true;
    },

    loadSync: function() {
      var me = this;
      me.fetch({
        async: false,
        complete: function (response) {
          me.onContentLoaded(response);
        }
      });
      me.evaluate();
      me.notifyRequests();
    },

    load: function (sync) {
      var me = this;
      if (!me.loaded) {
        if(me.loading) {







          return false;
        }
        me.loading = true;


        if (!sync) {


          if(me.isCrossDomain()) {
            return me.loadCrossDomain();
          }



          else if(!me.isCss() && Boot.hasReadyState) {
            me.createLoadElement(function () {
              me.loaded = true;
              me.notifyRequests();
            });
          }



          else {
            me.fetch({
              async: !sync,
              complete: function (response) {
                me.onContentLoaded(response);
                me.notifyRequests();
              }
            });
          }
        }




        else {
          me.loadSync();
        }
      }

      return true;
    },

    evaluateContent: function () {
      this.inject(this.content);
      this.content = null;
    },

    evaluateLoadElement: function() {
      Boot.getHead().appendChild(this.getElement());
    },

    evaluate: function () {
      var me = this;
      if(!me.evaluated) {
        if(me.evaluating) {
          return;
        }
        me.evaluating = true;
        if(me.content !== undefined) {
          me.evaluateContent();
        } else if(!me.error) {
          me.evaluateLoadElement();
        }
        me.evaluated = me.done = true;
        me.cleanup();
      }
    },


    cleanup: function () {
      var me = this,
          el = me.el,
          prop;

      if (!el) {
        return;
      }

      if (!me.preserve) {
        me.el = null;

        el.parentNode.removeChild(el);

        for (prop in el) {
          try {
            if (prop !== me.prop) {


              el[prop] = null;
            }
            delete el[prop];
          } catch (cleanEx) {

          }
        }
      }




      el.onload = el.onerror = el.onreadystatechange = emptyFn;
    },

    notifyRequests: function () {
      var requests = this.requests,
          len = requests.length,
          i, request;
      for (i = 0; i < len; i++) {
        request = requests[i];
        request.processLoadedEntries();
      }
      if(this.done) {
        this.fireListeners();
      }
    },

    onDone: function(listener) {
      var me = this,
          listeners = me.listeners || (me.listeners = []);
      if(me.done) {
        listener(me);
      } else {
        listeners.push(listener);
      }
    },

    fireListeners: function() {
      var listeners = this.listeners,
          listener;
      if(listeners && listeners.length > 0) {

        _debug("firing event listeners for url " + this.url);

        while((listener = listeners.shift())) {
          listener(this);
        }
      }
    }
  };


  Ext.disableCacheBuster = function (disable, path) {
    var date = new Date();
    date.setTime(date.getTime() + (disable ? 10 * 365 : -1) * 24 * 60 * 60 * 1000);
    date = date.toGMTString();
    doc.cookie = 'ext-cache=1; expires=' + date + '; path=' + (path || '/');
  };


  if (_environment.node) {
    Boot.prototype.load = Boot.prototype.loadSync = function (request) {

      require(filePath);
      onLoad.call(scope);
    };
    Boot.prototype.init = emptyFn;
  }


  Boot.init();
  return Boot;



}(function () {
}));

Ext.globalEval = Ext.globalEval || (this.execScript
    ? function (code) { execScript(code); }
    : function ($$code) { eval.call(window, $$code); });