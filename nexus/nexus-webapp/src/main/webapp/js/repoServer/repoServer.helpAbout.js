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
Sonatype.repoServer.HelpAboutPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  Sonatype.repoServer.HelpAboutPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
    items: [
      {
        xtype: 'panel',
        region: 'center',
        layout: 'fit',
        html: '<div class="little-padding">' +
              'Sonatype Nexus&trade; ' + Sonatype.utils.edition + ' Edition - ' + Sonatype.utils.version +
              '<br>Copyright&copy; 2008 Sonatype, Inc.  All rights reserved.' +
              '<br>Includes the third-party code listed at <a href="http://www.sonatype.com">www.sonatype.com</a>' +
              '</div>'
      }
    ]
  });
};

Ext.extend(Sonatype.repoServer.HelpAboutPanel, Ext.Panel, {
});