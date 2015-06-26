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
/**
 * Tests support metrics.
 */
StartTest(function(t) {
  t.describe('Support Metrics', function(t) {
    t.it('Metrics view is populated', function(t) {
      t.chain(
        t.openPageAsAdmin('admin/support/metrics'),
        { waitFor: 'CQVisible', args: 'nx-coreui-support-metrics'},
        { waitFor: 'CQVisible', args: 'nx-coreui-support-metrics button[action=download]' },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-metrics button[action=threads]' },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-metrics chart#memoryUsage' },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-metrics chart#memoryDist' },
        { waitFor: 'CQVisible', args: 'nx-coreui-support-metrics chart#threadStates' }
      );
    });
  });
});
