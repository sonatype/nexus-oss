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
var Harness = Siesta.Harness.Browser.ExtJS;

Harness.configure({
  title: 'Nexus UI Suite',
  viewDOM: true,

  testClass: NX.Test,

  preload: [
    'baseapp-debug.js',
    'extdirect-debug.js'
  ]

});

Harness.start(
    {
      group: 'Framework',
      items: [
        {
          group: 'Functional',
          hostPageUrl: '/nexus',
          items: [
            { url: 'framework/Bookmarking.js' },
            { url: 'framework/LoginLogout.js' },
            { url: 'framework/ModeButtons.js' },
            { url: 'framework/UnsupportedBrowser.js' }
          ]
        },
        {
          group: 'UT',
          items: [
            { url: 'framework/StateChangeEvent.js' }
          ]
        }
      ]
    },

    {
      group: 'RepositoryTarget',
      items: [
        {
          group: 'Functional',
          hostPageUrl: '/nexus',
          items: [
            { url: 'repositorytarget/RepositoryTargetCRUD.js' }
          ]
        }
      ]
    }

);
