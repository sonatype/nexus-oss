Ext.override(Sonatype.repoServer.HostedRepositoryEditor, {
  afterProviderSelectHandler: function( combo, rec, index ) {
    this.updateIndexableCombo(rec.data.format);
    
    if ( rec.data.provider == 'maven-site' ){
      this.find('name','writePolicy')[0].setValue('ALLOW_WRITE');
    }
    else {
      this.find('name','writePolicy')[0].setValue('ALLOW_WRITE_ONCE');
    }
  }
});