/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
