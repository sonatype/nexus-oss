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
 * Tests support system information.
 */
StartTest(function(t) {
  var expectedSections = [
        "nexus-status",
        "nexus-node",
        "nexus-configuration",
        "nexus-properties",
        "nexus-license",
        "nexus-bundles",
        "system-time",
        "system-properties",
        "system-environment",
        "system-runtime",
        "system-network",
        "system-filestores"
      ],
      sectionHeaderQuery = 'nx-coreui-support-sysinfo => .x-panel-header-text-container-nx-subsection-framed';

  t.describe('Support System Information', function(t) {
    t.it('System Information view is populated', function(t) {
      t.chain(
        t.openPageAsAdmin('admin/support/systeminformation'),
        { waitFor: 'CQVisible', args: 'nx-coreui-support-sysinfo'},
        { waitFor: 'CQVisible', args: 'nx-coreui-support-sysinfo button[action=download]' },
        { waitFor: 'CompositeQuery', args: sectionHeaderQuery },
        function(next) {
          // get an array of section names
          var namedSections = t.compositeQuery(sectionHeaderQuery).map(function(e){return e.innerHTML;});
          // check for the existence of each expected section
          expectedSections.forEach(function(e) {
            t.ok(Ext.Array.contains(namedSections, e), "Expected section: " + e);
          });
          // check for unexpected sections
          t.isDeeply(Ext.Array.difference(namedSections, expectedSections), [], "Check for unexpected sections");
          next();
        }
      );
    });
  });
});
