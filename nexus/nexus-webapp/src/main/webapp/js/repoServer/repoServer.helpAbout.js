/*
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
Sonatype.repoServer.HelpAboutPanel = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  Sonatype.repoServer.HelpAboutPanel.superclass.constructor.call(this, {
        layout : 'border',
        autoScroll : false,
        width : '100%',
        height : '100%',
        items : [{
              xtype : 'panel',
              region : 'center',
              layout : 'fit',
              html : this.getHelpText()
            }]
      });
};

Ext.extend(Sonatype.repoServer.HelpAboutPanel, Ext.Panel, {
  getHelpText : function() {
    return '<div class="little-padding">'
        + 'Sonatype Nexus&trade; ' + Sonatype.utils.edition + ' Version'
        + '<br/>Copyright &copy; 2008-2012 Sonatype, Inc.'
		+ '<br/>All rights reserved. Includes the third-party code listed at <a href="' + Sonatype.utils.attributionsURL + '" target="_new">' + Sonatype.utils.attributionsURL + '</a>.'
		+ '<br/>'
		+ '<br/>This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,'
		+ '<br/>which accompanies this distribution and is available at <a href="http://www.eclipse.org/legal/epl-v10.html" target="_new">http://www.eclipse.org/legal/epl-v10.html</a>.'
		+ '<br/>'
		+ '<br/>Sonatype Nexus&trade; Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks'
		+ '<br/>of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the'
		+ '<br/>Eclipse Foundation. All other trademarks are the property of their respective owners.'
  		+ '</div>';
  }
});
