/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
Ext.override(Sonatype.repoServer.VirtualRepositoryEditor, {
      afterProviderSelectHandler : function(combo, rec, index) {
        var provider = rec.data.provider;
        var sourceRepoCombo = this.form.findField('shadowOf');
        sourceRepoCombo.clearValue();
        sourceRepoCombo.focus();
        if (provider == 'obr-shadow')
        {
          sourceRepoCombo.store.filterBy(function fn(rec, id) {
                if (rec.data.repoType != 'virtual' && rec.data.format != 'obr')
                {
                  return true;
                }
                return false;
              });
        }
        else if (provider == 'm1-m2-shadow')
        {
          sourceRepoCombo.store.filterBy(function fn(rec, id) {
                if (rec.data.repoType != 'virtual' && rec.data.format == 'maven1')
                {
                  return true;
                }
                return false;
              });
        }
        else if (provider == 'm2-m1-shadow')
        {
          sourceRepoCombo.store.filterBy(function fn(rec, id) {
                if (rec.data.repoType != 'virtual' && rec.data.format == 'maven2')
                {
                  return true;
                }
                return false;
              });
        }
      }
    });