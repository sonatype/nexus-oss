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
          hostPageUrl: '/nexus/rapture.html',
          items: [
            { url: 'tests/framework/functional/Bookmarking.js' },
            { url: 'tests/framework/functional/LoginLogout.js' },
            { url: 'tests/framework/functional/ModeButtons.js' },
            { url: 'tests/framework/functional/UnsupportedBrowser.js' }
          ]
        },
        {
          group: 'State',
          items: [
            { url: 'tests/framework/state/StateChangeEvent.js' }
          ]
        }
      ]
    },

    {
      group: 'RepositoryTarget',
      items: [
        {
          group: 'Functional',
          hostPageUrl: '/nexus/rapture.html',
          items: [
            { url: 'tests/repositorytarget/RepositoryTargetCRUD.js' }
          ]
        },
        { url: 'tests/repositorytarget/RepositoryTargetExtDirectRead.js' },
        { url: 'tests/repositorytarget/RepositoryTargetExtDirectCreate.js' },
        { url: 'tests/repositorytarget/RepositoryTargetExtDirectCreateValidation.js' }
      ]
    }

    //{
    //  group: 'Capability',
    //  items: [
    //    'tests/capability/direct.js',
    //    'tests/capability/list.js',
    //    'tests/capability/sanity.js'
    //  ]
    //},
    //{
    //  group: 'Repository',
    //  items: [
    //    'tests/repository/direct.js',
    //    'tests/repository/sanity.js',
    //    {
    //      preload: [],
    //      hostPageUrl: '/nexus/rapture.html',
    //      url: 'tests/repository/remove-repository.js'
    //    }
    //  ]
    //}
);
