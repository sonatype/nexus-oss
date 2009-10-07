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
Sonatype.repoServer.HelpAboutPanel = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  Sonatype.repoServer.HelpAboutPanel.superclass.constructor
      .call(
          this,
          {
            layout :'border',
            autoScroll :false,
            width :'100%',
            height :'100%',
            items : [
              {
                xtype :'panel',
                region :'center',
                layout :'fit',
                html : this.getHelpText()
              }
            ]
          });
};

Ext.extend(Sonatype.repoServer.HelpAboutPanel, Ext.Panel, {
  getHelpText : function() {
    return '<div class="little-padding">'
    + 'Sonatype Nexus&trade; '
    + Sonatype.utils.edition
    + ' Version'
    + '<br>Copyright&copy; 2008 Sonatype, Inc.  All rights reserved.'
    + '<br>Includes the third-party code listed at <a href="http://nexus.sonatype.org/dev/attributions.html" target="_new">http://nexus.sonatype.org/dev/attributions.html</a>'
    + '<br><br>This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.'
    + '<br>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License Version 3 for more details.'
    + '<br><br>You should have received a copy of the GNU General Public License Version 3 along with this program. If not, see <a href="http://www.gnu.org/licenses/" target="_new">http://www.gnu.org/licenses/</a>'
    + '<br>Sonatype Nexus&trade; '
    + Sonatype.utils.edition
    + ' Version is available from Sonatype, Inc.'
    + '<br>&quot;Sonatype&quot; and &quot;Sonatype Nexus&quot; are trademarks of Sonatype, Inc.'
    + '</div>'
  }
});