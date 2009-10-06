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
Sonatype.repoServer.ArtifactContainer = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  Sonatype.repoServer.ArtifactContainer.superclass.constructor.call(this, {
    region: 'south',
    collapsible: true,
    collapsed: true,
    height: 160,
    minHeight: 100,
    maxHeight: 400,
    layoutOnTabChange: true
  });
  
  Sonatype.Events.fireEvent( 'artifactContainerInit', this, null );
};

Ext.extend(Sonatype.repoServer.ArtifactContainer, Sonatype.panels.AutoTabPanel, {
  collapsePanel : function() {
    if ( !this.collapsed ){
      this.collapse();
    }
    Sonatype.Events.fireEvent( 'artifactContainerUpdate', this, null );
  },
  updateArtifact : function( data ) {
    Sonatype.Events.fireEvent( 'artifactContainerUpdate', this, data );
    if ( this.collapsed ) {
      this.expand();
    }
  }
} );