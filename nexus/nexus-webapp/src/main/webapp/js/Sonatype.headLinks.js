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
Sonatype.headLinks = Ext.emptyFn;

Ext.apply(Sonatype.headLinks.prototype, {
      linkEventApplied : false,
      /**
       * Update the head links based on the current status of Nexus
       * 
       * @param {Ext.Element}
       *          linksEl parent of all the links' parent
       */
      updateLinks : function() {
        var left = Ext.get('head-link-l');
        var middle = Ext.get('head-link-m');
        var right = Ext.get('head-link-r');

        var loggedIn = Sonatype.user.curr.isLoggedIn;
        if (loggedIn)
        {
          this.updateLeftWhenLoggedIn(left);
          this.updateMiddleWhenLoggedIn(middle);
          this.updateRightWhenLoggedIn(right);
        }
        else
        {
          this.updateLeftWhenLoggedOut(left);
          this.updateMiddleWhenLoggedOut(middle);
          this.updateRightWhenLoggedOut(right);
        }
      },

      updateLeftWhenLoggedIn : function(linkEl) {
        linkEl.update(Sonatype.user.curr.username);
      },

      updateMiddleWhenLoggedIn : function(linkEl) {
        linkEl.update(' | ');
      },

      updateRightWhenLoggedIn : function(linkEl) {
        linkEl.update('Log Out');
        this.setClickLink(linkEl);
        linkEl.setStyle({
              'cursor' : 'pointer',
              'text-align' : 'right'
            });
      },
      updateLeftWhenLoggedOut : function(linkEl) {
        linkEl.update('');
      },

      updateMiddleWhenLoggedOut : function(linkEl) {
        linkEl.update('');
      },

      updateRightWhenLoggedOut : function(linkEl) {
        linkEl.update('Log In');
        this.setClickLink(linkEl);
        linkEl.setStyle({
              'cursor' : 'pointer',
              'text-align' : 'right'
            });
      },
      setClickLink : function(el) {
        if (!this.linkEventApplied)
        {
          el.on('click', Sonatype.repoServer.RepoServer.loginHandler, Sonatype.repoServer.RepoServer);
          this.linkEventApplied = true;
        }
      }
    });
