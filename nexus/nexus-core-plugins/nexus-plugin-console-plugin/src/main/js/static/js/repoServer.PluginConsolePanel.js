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
Sonatype.repoServer.PluginConsolePanel = function( config ) {
  var config = config || {};
  var defaultConfig = {
    title: 'Plugin Console'
  };
  Ext.apply( this, config, defaultConfig );
    
  Sonatype.repoServer.PluginConsolePanel.superclass.constructor.call( this, {
    url: Sonatype.config.servicePath + '/plugin_console/plugin_infos',
    dataAntoLoad: true,
    tabbedChildren: true,
    dataSortInfo: { field: 'name', direction: 'asc' },
    tbar: [],
    columns: [
    { name: 'name',
      header: 'Name',
      width: 300
    },
    { name: 'version',
      header: 'Version',
      width: 150
    },    
    { name: 'description',
      header: 'Description',
      width: 300
    },
    { name: 'status',
      id: 'status',
      header: 'Status',
      width: 100,
      renderer: function( value ){
        if ( Ext.isEmpty(value) ){
          return value;
        }
        return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
      }
    },
    { name: 'scmVersion' },
    { name: 'scmTimestamp' },
    { name: 'failureReason' },
    { name: 'site'},
    { name: 'documentation'},
    { name: 'restInfos'}
    ],
    autoExpandColumn: 'status',
    rowClickEvent: 'pluginInfoInit'
  });
};

Ext.extend( Sonatype.repoServer.PluginConsolePanel, Sonatype.panels.GridViewer, {
} );

Sonatype.Events.addListener( 'nexusNavigationInit', function( nexusPanel ) {
  var sp = Sonatype.lib.Permissions;
  if ( sp.checkPermission( 'nexus:pluginconsoleplugininfos', sp.READ) ){
    nexusPanel.add( {
      enabled: true,
      sectionId: 'st-nexus-config',
      title: 'Plugin Console',
      tabId: 'plugin_console',
      tabTitle: 'Plugin Console',
      tabCode: Sonatype.repoServer.PluginConsolePanel
    } );
  }
} );