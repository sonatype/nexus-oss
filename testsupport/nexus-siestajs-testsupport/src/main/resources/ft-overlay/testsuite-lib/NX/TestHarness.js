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

// HACK: fix component inspector api link to proper version
Siesta.CurrentLocale['Siesta.Harness.Browser.UI.DomContainer'].docsUrlText =
    'http://docs.sencha.com/extjs/4.2.3/#!/api/{1}';

/**
 * NX specific Siesta test-harness extensions.
 */
Class('NX.TestHarness', {
  isa: Siesta.Harness.Browser.ExtJS,

  my: {
    has: {
      appMode: 'prod'
    },

    methods: {
      configure: function (config) {
        var debug = window.location.href.search("[?&]debug") > -1;
        console.log('Debug: ' + debug);

        if (debug) {
          this.appMode = 'debug';
        }
        console.log('App Mode: ' + this.appMode);

        // detect the application url to use, enable debug
        var url = '/';
        if (debug) {
          url = url + '?debug';
        }
        console.log('URL: ' + url);

        // apply defaults to the configuration
        Ext.applyIf(config, {
          testClass: NX.TestClass,

          hostPageUrl: url,

          loaderPath: {
            NX: 'static/rapture/NX'
          },

          // NX.app.Loader is based on Ext.onReady, so waiting for app just creates delay from timeout
          waitForExtReady: true,
          waitForAppReady: false,

          // Run tests sequentially, better chance of this working due to problem with session
          runCore: 'sequential',
          speedRun: true,

          // disable recording offsets
          recorderConfig: {
            recordOffsets: false
          }
        });

        this.SUPER(config);
      },

      resource: function(ref) {
        return ref.replace('{mode}', this.appMode);
      }
    }
  }
});
