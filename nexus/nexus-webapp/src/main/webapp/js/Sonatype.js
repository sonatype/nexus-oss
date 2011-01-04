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
(function() {

  window.Sonatype = function() {
    return {
      init : function() {
        Ext.QuickTips.init();
        Ext.apply(Ext.QuickTips.getQuickTip(), {
          showDelay : 250,
          hideDelay : 300,
          dismissDelay : 0
            // don't automatically hide quicktip
          });

        Ext.History.init();

        Ext.get('header').hide();
        Ext.get('welcome-tab').hide();

        Sonatype.state.CookieProvider = new Sonatype.lib.CookieProvider({
          expires : new Date(new Date().getTime() + (1000 * 60 * 60 * 24 * 365))
            // expires in 1 year
          });

        var cp = Sonatype.state.CookieProvider;
        var username = cp.get('username', null);
        // Sonatype.utils.clearCookie('JSESSIONID');

        Sonatype.view.init();

        Sonatype.utils.loadNexusStatus();
      }

    };
  }();

  // Define all second level namespaces
  Ext.namespace('Sonatype.state', 'Sonatype.ext', 'Sonatype.lib', 'Sonatype.utils', 'Sonatype.config', 'Sonatype.user', 'Sonatype.resources', 'Sonatype.repoServer', 'Sonatype.repoServer.resources');

})();
