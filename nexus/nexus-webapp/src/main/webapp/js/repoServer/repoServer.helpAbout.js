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
        + 'Sonatype Nexus&trade; '
        + Sonatype.utils.edition
        + ' Version'
        + '<br/>Copyright &copy; 2008-2011 Sonatype, Inc.'
		+ '<br/>All rights reserved. Includes the third-party code listed at <a href="http://www.sonatype.com/products/nexus/attributions" target="_new">http://www.sonatype.com/products/nexus/attributions</a>.' 
		+ '<br/>'
		+ '<br/>This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General'
		+ '<br/>Public License Version 3 as published by the Free Software Foundation.'
		+ '<br/>'
		+ '<br/>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied'
		+ '<br/>warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3'
		+ '<br/>for more details.'
		+ '<br/>'
		+ '<br/>You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see'
		+ '<br/><a href="http://www.gnu.org/licenses" target="_new">http://www.gnu.org/licenses</a>.'
		+ '<br/>'
		+ '<br/>Sonatype Nexus&trade; '+ Sonatype.utils.edition +' Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of'
		+ '<br/>Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.'
		+ '<br/>All other trademarks are the property of their respective owners.'
		+ '</div>';
  }
});