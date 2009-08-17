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

var checkedNewVersion = false;

Sonatype.Events.addListener( 'nexusStatus', function() {
  if ( !checkedNewVersion ){
    Ext.Ajax.request( {
      method: 'GET',
      suppressStatus: [404,401,-1],
      url: Sonatype.config.servicePath + '/lvo/nexus-' +
        Sonatype.utils.editionShort.substr( 0, 3 ).toLowerCase() + '/' + Sonatype.utils.versionShort,
      success: function( response, options ) {
        checkedNewVersion = true;
        var r = Ext.decode( response.responseText );
        
        if ( r.response != null && r.response.isSuccessful && r.response.version ) {
          Sonatype.utils.postWelcomePageAlert(
            '<span style="color:#000">' +
            '<b>UPGRADE AVAILABLE:</b> ' +
            'Nexus ' + Sonatype.utils.edition + ' ' + r.response.version + ' is now available. ' +
            '<a href="' + r.response.url + '" target="_blank">Download now!</a>' +
            '</span>' 
          );
        }
      },
      failure: function() {
        checkedNewVersion = true;
      }
    });
  }
} );