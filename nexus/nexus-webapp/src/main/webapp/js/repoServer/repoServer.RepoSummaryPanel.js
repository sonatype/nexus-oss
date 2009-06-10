/*
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */

Sonatype.repoServer.AbstractRepositorySummaryPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  
  Ext.apply( this, config, defaultConfig );
  
  var abstractConfigItems = [
    {
      xtype: 'textfield',
      name: 'idField',
      fieldLabel: 'Repository ID',
      width: '95%',
      readOnly: true,
      helpText: 'ID of this repository.'
    },
    {
      xtype: 'textfield',
      name: 'nameField',
      fieldLabel: 'Repository Name',
      width: '95%',
      readOnly: true,
      helpText: 'Name of this repository.'
    },
    {
      xtype: 'textfield',
      name: 'typeField',
      fieldLabel: 'Repository Type',
      width: '95%',
      readOnly: true,
      helpText: 'Type of this repository.'
    },
    {
      xtype: 'textfield',
      name: 'policyField',
      fieldLabel: 'Repository Policy',
      width: '95%',
      readOnly: true,
      helpText: 'Policy of this repository.'
    },
    {
      xtype: 'textfield',
      name: 'formatField',
      fieldLabel: 'Repository Format',
      width: '95%',
      readOnly: true,
      helpText: 'Format of this repository.'
    }
  ];
  var abstractMetaItems = [
    {
      xtype: 'textfield',
      name: 'groupField',
      fieldLabel: 'Contained in Group(s)',
      width: '95%',
      readOnly: true,
      helpText: 'Repository Groups that contain this Repository.'
    },
    {
      xtype: 'textfield',
      name: 'sizeOnDiskField',
      fieldLabel: 'Size on disk (bytes)',
      width: '95%',
      readOnly: true,
      helpText: 'Space consumed on disk by this repository.'
    },
    {
      xtype: 'textfield',
      name: 'fileCountField',
      fieldLabel: 'Number of files',
      width: '95%',
      readOnly: true,
      helpText: 'Number of files contained in this repository.'
    }
  ];
  
  Sonatype.repoServer.AbstractRepositorySummaryPanel.superclass.constructor.call( this, {
    uri: this.payload.data.resourceURI + '/meta',
    readOnly: true,
    dataModifiers: {
      load: {
        'rootData' :this.populateFields.createDelegate(this)    
      },
      save: {}    
    },
    items: abstractConfigItems.concat( this.configItems ).concat( abstractMetaItems ).concat( this.metaItems ) 
  } );
};

Ext.extend( Sonatype.repoServer.AbstractRepositorySummaryPanel, Sonatype.ext.FormPanel, {
  getActionURL : function() {
    return this.uri;
  },
  populateFields : function(arr, srcObj, fpanel) {
    this.populateGroupField( srcObj.groups );
    this.populateIdField( srcObj.id );
    this.populateNameField( this.payload.data.name );
    this.populateTypeField( this.payload.data.repoType );
    this.populateFormatField( this.payload.data.format );
    this.populatePolicyField( this.payload.data.repoPolicy );
    this.populateSizeOnDiskField( srcObj.sizeOnDisk );
    this.populateFileCountField( srcObj.fileCountInRepository );
  },
  populateGroupField : function( groups ) {
    if ( groups != undefined && groups.length > 0 ) {
      var combinedGroups = '';
      for ( var i = 0 ; i < groups.length ; i++ ){
        var group = this.groupStore.getAt( this.groupStore.findBy( 
            function( rec, recid ) {
              return rec.data.id == groups[i];
            }, this ) );
        
        if ( group ){
          if ( combinedGroups.length > 0 ) {
            combinedGroups += ', ';
          }
          
          combinedGroups += group.data.name;
        }
      }
      this.find( 'name', 'groupField' )[0].setValue( combinedGroups );
    }
  },
  populateIdField : function( id ) {
    this.find( 'name', 'idField' )[0].setValue( id );
  },
  populateNameField : function( name ) {
    this.find( 'name', 'nameField' )[0].setValue( name );
  },
  populateTypeField : function( type ) {
    this.find( 'name', 'typeField' )[0].setValue( type );
  },
  populateFormatField : function( format ) {
    this.find( 'name', 'formatField' )[0].setValue( format );
  },
  populatePolicyField : function( policy ) {
    this.find( 'name', 'policyField' )[0].setValue( policy );
  },
  populateSizeOnDiskField : function( policy ) {
    this.find( 'name', 'sizeOnDiskField' )[0].setValue( policy );
  },
  populateFileCountField : function( policy ) {
    this.find( 'name', 'fileCountField' )[0].setValue( policy );
  }
} );

Sonatype.repoServer.HostedRepositorySummaryPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  Sonatype.repoServer.HostedRepositorySummaryPanel.superclass.constructor.call( this, {
    configItems: [
    ],
    metaItems: [
      {
        xtype: 'textfield',
        fieldLabel: 'Distribution Management',
        helpText: 'Distribution Management section that can be placed in your pom.xml file.',
        hidden: true
      },
      {
        xtype: 'textarea',
        name: 'distMgmtField',
        anchor: Sonatype.view.FIELD_OFFSET,
        readOnly: true,
        hideLabel: true,
        height: 100
      }
    ]
  } );
};

Ext.extend( Sonatype.repoServer.HostedRepositorySummaryPanel, Sonatype.repoServer.AbstractRepositorySummaryPanel, {
  populateFields : function(arr, srcObj, fpanel) {
    Sonatype.repoServer.HostedRepositorySummaryPanel.superclass.populateFields.call(
      this,
      arr,
      srcObj,
      fpanel );
    
    this.populateDistributionManagementField( 
      this.payload.data.id,
      this.payload.data.repoPolicy,
      this.payload.data.resourceURI );
  },
  populateDistributionManagementField : function( id, policy, uri ) {
    var distMgmtString = '<distributionManagement>\n  <${repositoryType}>\n    <id>${repositoryId}</id>\n    <url>${repositoryUrl}</url>\n  </${repositoryType}>\n</distributionManagement>';
    
    distMgmtString = distMgmtString.replaceAll('${repositoryType}', policy == 'Release' ? 'repository' : 'snapshotRepository' );
    distMgmtString = distMgmtString.replaceAll('${repositoryId}', id );
    distMgmtString = distMgmtString.replaceAll('${repositoryUrl}', Sonatype.config.repos.restToContentUrl( uri ) );
    
    this.find( 'name', 'distMgmtField' )[0].setRawValue( distMgmtString );
  }
} );

Sonatype.repoServer.ProxyRepositorySummaryPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  Sonatype.repoServer.ProxyRepositorySummaryPanel.superclass.constructor.call( this, {
    configItems: [
      {
        xtype: 'textfield',
        name: 'remoteUrlField',
        fieldLabel: 'Remote URL',
        width: '95%',
        readOnly: true,
        helpText: 'Remote URL of this repository.'
      }
    ],
    metaItems: [
    ]
  } );
};

Ext.extend( Sonatype.repoServer.ProxyRepositorySummaryPanel, Sonatype.repoServer.AbstractRepositorySummaryPanel, {
  populateFields : function(arr, srcObj, fpanel) {
    Sonatype.repoServer.ProxyRepositorySummaryPanel.superclass.populateFields.call(
      this,
      arr,
      srcObj,
      fpanel );
    
    this.populateRemoteUrlField( this.payload.data.remoteUri );
  },
  populateRemoteUrlField : function( url ) {
    this.find( 'name', 'remoteUrlField' )[0].setValue( url );
  }
} );

Sonatype.repoServer.VirtualRepositorySummaryPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply( this, config, defaultConfig );

  Sonatype.repoServer.VirtualRepositorySummaryPanel.superclass.constructor.call( this, {
    configItems: [
    ],
    metaItems: [
    ]
  } );
};

Ext.extend( Sonatype.repoServer.VirtualRepositorySummaryPanel, Sonatype.repoServer.AbstractRepositorySummaryPanel, {} );

Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec, gridPanel ) {
  var sp = Sonatype.lib.Permissions;
  
  var repoPanels = {
    hosted: Sonatype.repoServer.HostedRepositorySummaryPanel,
    proxy: Sonatype.repoServer.ProxyRepositorySummaryPanel,
    virtual: Sonatype.repoServer.VirtualRepositorySummaryPanel
  };

  var panel = repoPanels[rec.data.repoType];
  
  if ( panel
      && rec.data.resourceURI
      && sp.checkPermission( 'nexus:repometa', sp.READ ) ) {
    cardPanel.add( new panel( {
      tabTitle: 'Summary',
      payload: rec,
      groupStore: gridPanel.groupStore
    } ) );
  }
} );