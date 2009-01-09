/*
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
Sonatype.repoServer.ArtifactoryMigrationPanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Artifactory Import'
  };
  Ext.apply( this, config, defaultConfig );


  Sonatype.repoServer.ArtifactoryMigrationPanel.superclass.constructor.call(this, {
    autoScroll: false,
    layout: 'border',
    items: [
      {
        html: 'Arifactory Import'
      }
    ]
  });
};

Ext.extend( Sonatype.repoServer.ArtifactoryMigrationPanel, Ext.Panel, {
} );

Sonatype.Events.addListener( 'nexusNavigationInit', function( nexusPanel ) {
  nexusPanel.add( {
    enabled: Sonatype.lib.Permissions.checkPermission( 'nexus:artifactorymigrate', Sonatype.lib.Permissions.CREATE ) &&
      Sonatype.lib.Permissions.checkPermission( 'nexus:artifactoryupload', Sonatype.lib.Permissions.CREATE ),
    sectionId: 'st-nexus-config',
    title: 'Artifactory Import',
    tabId: 'migration-artifactory',
    tabCode: Sonatype.repoServer.ArtifactoryMigrationPanel
  } );
} );
