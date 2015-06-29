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

NX.TestHarness.configure({
  title: 'Nexus FT: OSS'
});

NX.TestHarness.start(
    {
      group: 'Framework',
      items: [
        {
          group: 'UT',
          hostPageUrl: undefined,
          preload: [
            NX.TestHarness.resource('static/rapture/baseapp-{mode}.js'),
            NX.TestHarness.resource('static/rapture/extdirect-{mode}.js')
          ],
          items: [
            { url: 'testsuite-oss/framework/statechange.t.js' },
            { url: 'testsuite-oss/framework/validation.t.js' }
          ]
        },

        {
          group: 'FT',
          items: [
            { url: 'testsuite-oss/framework/signin_signout.t.js' },
            { url: 'testsuite-oss/framework/mode_button.t.js' },
            { url: 'testsuite-oss/framework/unsupported_browser.t.js' }
          ]
        }
      ]
    },

    {
      group: 'Repository',
      items: [
        {
          group: 'FT',
          items: [
            { url: 'testsuite-oss/repository/blobstore_crud.t.js' },
            { url: 'testsuite-oss/repository/repository_crud.t.js' }
          ]
        }
      ]
    },

    {
      group: 'Security',
      items: [
        {
          group: 'FT',
          items: [
            { url: 'testsuite-oss/security/security_role.t.js' },
            { url: 'testsuite-oss/security/security_user.t.js' },
            { url: 'testsuite-oss/security/security_anonymous.t.js' }
          ]
        }
      ]
    },

    {
      group: 'System',
      items: [
        {
          group: 'FT',
          items: [
            { url: 'testsuite-oss/system/system_tasks.t.js' }
          ]
        }
      ]
    }
);
