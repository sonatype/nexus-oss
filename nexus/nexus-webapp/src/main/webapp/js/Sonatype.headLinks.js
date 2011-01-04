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
              'color' : '#15428B',
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
              'color' : '#15428B',
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
