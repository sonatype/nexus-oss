/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

(function() {
var hostedHandler = Sonatype.repoServer.HostedRepositoryEditor.prototype.afterProviderSelectHandler;
Ext.override( Sonatype.repoServer.HostedRepositoryEditor, {
  afterProviderSelectHandler:function ( combo, rec, index ) {
    hostedHandler.apply(this, arguments);

    if ( rec.data.provider == 'maven-site' ) {
      this.find( 'name', 'writePolicy' )[0].setValue( 'ALLOW_WRITE' );
    }
    else {
      this.find( 'name', 'writePolicy' )[0].setValue( 'ALLOW_WRITE_ONCE' );
    }
  }
} );
})();

Ext.override( Sonatype.repoServer.HostedRepositorySummaryPanel, {
  populateFields:function ( arr, srcObj, fpanel ) {
    Sonatype.repoServer.HostedRepositorySummaryPanel.superclass.populateFields.call( this, arr, srcObj, fpanel );

    if ( this.payload.data.provider == 'maven-site' ) {
      this.populateSiteDistributionManagementField(
          this.payload.data.id, this.payload.data.contentResourceURI
      );
    }
    else {
      this.populateDistributionManagementField(
          this.payload.data.id, this.payload.data.repoPolicy, this.payload.data.contentResourceURI
      );
    }
  },
  populateSiteDistributionManagementField:function ( id, uri ) {
    var distMgmtString = '<distributionManagement>\n  <site>\n    <id>${repositoryId}</id>\n    <url>${repositoryUrl}</url>\n  </site>\n</distributionManagement>';

    distMgmtString = distMgmtString.replaceAll( '${repositoryId}', id );
    distMgmtString = distMgmtString.replaceAll( '${repositoryUrl}', uri );

    this.find( 'name', 'distMgmtField' )[0].setRawValue( distMgmtString );
  }
} );
