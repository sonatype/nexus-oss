var REINDEX_ACTION = function ( rec, full ) {
  var indexUrl = null;
  if ( full ) {
    indexUrl = Sonatype.config.servicePath + '/data_index';
  } else {
    indexUrl = Sonatype.config.servicePath + '/data_incremental_index';
  }

  var url = indexUrl +
    rec.data.resourceURI.slice(Sonatype.config.host.length + Sonatype.config.servicePath.length);
  
  //make sure to provide /content path for repository root requests like ../repositories/central
  if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url)){
    url += '/content';
  }
  
  Ext.Ajax.request({
    url: url,
    callback: function(options, isSuccess, response) {
      if ( !isSuccess ) {
        Sonatype.utils.connectionError( response, 'The server did not re-index the repository.' );
      }
    },
    scope: this,
    method: 'DELETE'
  });
}
  
Sonatype.Events.addListener( 'repositoryMenuInit',
  function( menu, repoRecord ) {
    if ( Sonatype.lib.Permissions.checkPermission( 'nexus:index', Sonatype.lib.Permissions.DELETE ) 
        && repoRecord.get( 'repoType' ) != 'virtual' ) {
      menu.add({
        text: 'ReIndex',
        handler: function( rec ) {
          REINDEX_ACTION( rec, true );
        },
        scope: this
      });
      menu.add( {
        text: 'Incremental ReIndex',
        handler: function( rec ) {
          REINDEX_ACTION( rec, false );
        },
        scope: this
      });
    }
  }
);

Sonatype.Events.addListener( 'repositoryContentMenuInit',
  function( menu, repoRecord, contentRecord ) {
    if ( Sonatype.lib.Permissions.checkPermission( 'nexus:index', Sonatype.lib.Permissions.DELETE ) 
        && repoRecord.get( 'repoType' ) != 'virtual' ) {
      menu.add({
        text: 'ReIndex',
        handler: function( rec ) {
          REINDEX_ACTION( rec, true );
        },
        scope: this
      });
      menu.add( {
        text: 'Incremental ReIndex',
        handler: function( rec ) {
          REINDEX_ACTION( rec, false );
        },
        scope: this
      });
    }
  }
);