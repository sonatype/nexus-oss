/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
Ext.override(Sonatype.repoServer.HostedRepositoryEditor, {
      afterProviderSelectHandler : function(combo, rec, index) {
        this.updateIndexableCombo(rec.data.format);

        if (rec.data.provider == 'maven-site')
        {
          this.find('name', 'writePolicy')[0].setValue('ALLOW_WRITE');
        }
        else
        {
          this.find('name', 'writePolicy')[0].setValue('ALLOW_WRITE_ONCE');
        }
      }
    });