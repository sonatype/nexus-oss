/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

/*
 * Use anonymous closure to augment the current class behaviour
 */
(function() {
  var originalHandler = Sonatype.repoServer.VirtualRepositoryEditor.prototype.afterProviderSelectHandler;

  Ext.override(Sonatype.repoServer.VirtualRepositoryEditor, {
    afterProviderSelectHandler : function(combo, rec, index) {

      // first invoke the original behaviour
      originalHandler.apply(this, arguments);

      // virtual OBR can be applied on top of any non-virtual, non-OBR repository
      if (rec.data.provider == 'obr-shadow') {
        this.form.findField('shadowOf').store.filterBy(function fn(rec, id) {
            return rec.data.repoType != 'virtual' && rec.data.format != 'obr';
          });}
    }
  });
})();
