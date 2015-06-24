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
var Harness = Siesta.Harness.Browser.ExtJS;

Harness.configure({
  title: 'Nexus Functional TestSuite',

  loaderPath: {
    NX: 'static/rapture/NX'
  },

  // use our custom test-class extensions
  testClass: NX.TestClass,

  // HACK: Run tests sequentially, better chance of this working due to problem with session
  runCore: 'sequential',
  speedRun: true,

  hostPageUrl: '/',
  waitForAppReady: true
});

Harness.start(
    {
      group: 'Framework',
      items: [
        {
          group: 'UT',
          hostPageUrl: undefined,
          waitForAppReady: false,
          preload: [
            //'static/rapture/resources/baseapp-debug.css',
            //'static/rapture/resources/rapture.css',
            //'static/rapture/resources/loading.css',
            'static/rapture/baseapp-debug.js',
            'static/rapture/extdirect-debug.js'
          ],
          items: [
            { url: 'testsuite/framework/010_statechange.t.js' },
            { url: 'testsuite/framework/011_validation.t.js' }
          ]
        },

        {
          group: 'FT',
          items: [
            { url: 'testsuite/framework/110_signin_signout.t.js' },
            // FIXME: disable this test, which is constantly failing with phantomjs
            //{ url: 'testsuite/framework/120_bookmarking.t.js' },
            { url: 'testsuite/framework/130_mode_button.t.js' },
            { url: 'testsuite/framework/140_unsupported_browser.t.js' }
          ]
        }
      ]
    },

    {
      group: 'BlobStore',
      items: [
        {
          group: 'FT',
          items: [
            { url: 'testsuite/blobstore/110_blobstore_crud.t.js' }
          ]
        }
      ]
    }
);
