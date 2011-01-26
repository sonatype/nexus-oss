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
Sonatype.repoServer.Documentation = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  Sonatype.repoServer.Documentation.superclass.constructor
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

Ext.extend(Sonatype.repoServer.Documentation, Ext.Panel, {
  getHelpText : function() {
    return '<div class="little-padding">'
    + 'Sonatype Nexus&trade; '
    + Sonatype.utils.edition
    + ' Version'
    + '<br><a href="http://nexus.sonatype.org/" target="_new">Nexus Home</a>'
    + '<br><a href="http://www.sonatype.com/book/reference/repository-manager.html" target="_new">Getting Started</a>'
    + '<br><a href="http://nexus.sonatype.org/wiki/" target="_new">Nexus Wiki</a>'
    + '<br><a href="http://www.sonatype.com/books/nexus-book/" target="_new">Nexus Book</a>'
    + '<br><a href="http://www.sonatype.com/book/reference/public-book.html" target="_new">Maven Book</a>'
    + '<br><a href="http://nexus.sonatype.org/changes.html" target="_new">Release Notes</a>'
    + '</div>'
  }
});
