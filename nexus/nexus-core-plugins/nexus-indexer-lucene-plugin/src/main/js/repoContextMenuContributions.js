/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
        text: 'Repair Index',
        handler: function( rec ) {
          REINDEX_ACTION( rec, true );
        },
        scope: this
      });
      menu.add( {
        text: 'Update Index',
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
        && repoRecord.data['repoType'] != 'virtual' ) {
      menu.add( {
        text: 'Update Index',
        handler: function( rec ) {
          REINDEX_ACTION( rec, false );
        },
        scope: this
      });
    }
  }
);