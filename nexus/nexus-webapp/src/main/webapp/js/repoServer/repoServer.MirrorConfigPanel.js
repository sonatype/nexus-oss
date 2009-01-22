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

Sonatype.repoServer.MirrorConfigPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  Sonatype.repoServer.MirrorConfigPanel.superclass.constructor.call(this, {
    autoScroll: true
  });
};


Ext.extend(Sonatype.repoServer.MirrorConfigPanel, Ext.Panel, {
});

Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  var sp = Sonatype.lib.Permissions;
  if ( rec.data.resourceURI 
      && sp.checkPermission( 'nexus:repositorymirrors', sp.READ )
      && rec.data.repoType == 'proxy') {
    cardPanel.add( new Sonatype.repoServer.MirrorConfigPanel( { 
      payload: rec,
      tabTitle: 'Mirrors'
    } ) );
  }
} );
