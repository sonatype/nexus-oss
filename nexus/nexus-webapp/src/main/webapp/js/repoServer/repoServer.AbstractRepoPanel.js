/*
 * Sonatype Nexus (TM) Open Source Version. Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at
 * http://nexus.sonatype.org/dev/attributions.html This program is licensed to
 * you under Version 3 only of the GNU General Public License as published by
 * the Free Software Foundation. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License Version 3 for more details. You should have received a copy of
 * the GNU General Public License Version 3 along with this program. If not, see
 * http://www.gnu.org/licenses/. Sonatype Nexus (TM) Professional Version is
 * available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc.
 */
/*
 * Repository panel superclass
 */

/*
 * config options: { id: the is of this panel instance [required] title: title
 * of this panel (shows in tab) }
 */
Sonatype.repoServer.AbstractRepoPanel = function(config) {
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);

  this.sp = Sonatype.lib.Permissions;

  this.ctxRecord = null;
  this.reposGridPanel == null;

  this.repoActions = {
    clearCache : {
      text : 'Expire Cache',
      handler : this.clearCacheHandler,
      scope : this
    },
    rebuildMetadata : {
      text : 'Rebuild Metadata',
      handler : this.rebuildMetadataHandler,
      scope : this
    },
    putInService : {
      text : 'Put in Service',
      scope : this,
      handler : this.putInServiceHandler
    },
    putOutOfService : {
      text : 'Put Out of Service',
      scope : this,
      handler : this.putOutOfServiceHandler
    },
    allowProxy : {
      text : 'Allow Proxy',
      scope : this,
      handler : this.allowProxyHandler
    },
    blockProxy : {
      text : 'Block Proxy',
      scope : this,
      handler : this.blockProxyHandler
    }
  };

  Sonatype.repoServer.AbstractRepoPanel.superclass.constructor.call(this, {});
};

Ext.extend(Sonatype.repoServer.AbstractRepoPanel, Ext.Panel, {
      hasSelection : function() {
        return this.ctxRecord || this.reposGridPanel.getSelectionModel().hasSelection();
      },

      viewHandler : function() {
        if (this.ctxRecord || this.reposGridPanel.getSelectionModel().hasSelection())
        {
          var rec = (this.ctxRecord) ? this.ctxRecord : this.reposGridPanel.getSelectionModel().getSelected();
          this.viewRepo(rec);
        }
      },

      clearCacheHandler : function(rec) {
        var url = Sonatype.config.repos.urls.cache + rec.data.resourceURI.slice(Sonatype.config.host.length + Sonatype.config.servicePath.length);

        // make sure to provide /content path for repository root requests like
        // ../repositories/central
        if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url))
        {
          url += '/content';
        }

        Ext.Ajax.request({
              url : url,
              callback : this.clearCacheCallback,
              scope : this,
              method : 'DELETE'
            });
      },

      clearCacheCallback : function(options, isSuccess, response) {
        // @todo: stop updating messaging here
        if (isSuccess)
        {

        }
        else
        {
          Sonatype.utils.connectionError(response, 'The server did not clear the repository\'s cache.');
        }
      },

      rebuildMetadataHandler : function(rec) {
        var url = Sonatype.config.repos.urls.metadata + rec.data.resourceURI.slice(Sonatype.config.host.length + Sonatype.config.servicePath.length);

        // make sure to provide /content path for repository root requests like
        // ../repositories/central
        if (/.*\/repositories\/[^\/]*$/i.test(url) || /.*\/repo_groups\/[^\/]*$/i.test(url))
        {
          url += '/content';
        }

        Ext.Ajax.request({
              url : url,
              callback : this.rebuildMetadataCallback,
              scope : this,
              method : 'DELETE'
            });
      },

      rebuildMetadataCallback : function(options, isSuccess, response) {
        // @todo: stop updating messaging here
        if (isSuccess)
        {

        }
        else
        {
          Sonatype.utils.connectionError(response, 'The server did not rebuild metadata in the repository.');
        }
      },

      putInServiceHandler : function(rec) {
        Ext.Ajax.request({
              url : rec.data.resourceURI + '/status',
              jsonData : {
                data : {
                  id : rec.data.id,
                  repoType : rec.data.repoType,
                  localStatus : 'IN_SERVICE'
                }
              },
              callback : this.putInServiceCallback,
              repoRecord : rec,
              scope : this,
              method : 'PUT'
            });
      },

      putInServiceCallback : function(options, isSuccess, response) {
        // @todo: stop updating messaging here
        if (isSuccess)
        {
          var statusResp = Ext.decode(response.responseText);
          this.updateRepoStatuses(statusResp.data, options.repoRecord);
        }
        else
        {
          Sonatype.utils.connectionError(response, 'The server did not put the repository into service.');
        }
      },

      putOutOfServiceHandler : function(rec) {
        Ext.Ajax.request({
              url : rec.data.resourceURI + '/status',
              jsonData : {
                data : {
                  id : rec.data.id,
                  repoType : rec.data.repoType,
                  localStatus : 'OUT_OF_SERVICE'
                }
              },
              callback : this.putOutOfServiceCallback,
              repoRecord : rec,
              scope : this,
              method : 'PUT'
            });
      },

      allowProxyHandler : function(rec) {
        if (rec.data.status)
        {
          Ext.Ajax.request({
                url : rec.data.resourceURI + '/status',
                jsonData : {
                  data : {
                    id : rec.data.id,
                    repoType : rec.data.repoType,
                    localStatus : rec.data.status.localStatus,
                    remoteStatus : rec.data.status.remoteStatus,
                    proxyMode : 'ALLOW'
                  }
                },
                callback : this.allowProxyCallback,
                scope : this,
                repoRecord : rec,
                method : 'PUT'
              });
        }
      },

      allowProxyCallback : function(options, isSuccess, response) {
        // @todo: stop updating messaging here
        if (isSuccess)
        {
          var statusResp = Ext.decode(response.responseText);
          this.updateRepoStatuses(statusResp.data, options.repoRecord);
        }
        else
        {
          Sonatype.utils.connectionError(response, 'The server did not update the proxy repository status to allow.');
        }
      },

      blockProxyHandler : function(rec) {
        if (rec.data.status)
        {
          Ext.Ajax.request({
                url : rec.data.resourceURI + '/status',
                jsonData : {
                  data : {
                    id : rec.data.id,
                    repoType : rec.data.repoType,
                    localStatus : rec.data.status.localStatus,
                    remoteStatus : rec.data.status.remoteStatus,
                    proxyMode : 'BLOCKED_MANUAL'
                  }
                },
                callback : this.blockProxyCallback,
                repoRecord : rec,
                scope : this,
                method : 'PUT'
              });
        }
      },

      blockProxyCallback : function(options, isSuccess, response) {
        // @todo: stop updating messaging here
        if (isSuccess)
        {
          var statusResp = Ext.decode(response.responseText);
          this.updateRepoStatuses(statusResp.data, options.repoRecord);
        }
        else
        {
          Sonatype.utils.connectionError(response, 'The server did not update the proxy repository status to blocked.');
        }
      },

      putOutOfServiceCallback : function(options, isSuccess, response) {
        // @todo: stop updating messaging here
        if (isSuccess)
        {
          var statusResp = Ext.decode(response.responseText);
          this.updateRepoStatuses(statusResp.data, options.repoRecord);
        }
        else
        {
          Sonatype.utils.connectionError(response, 'The server did not put the repository out of service.');
        }
      },

      statusConverter : function(status, parent) {
        if (!parent.status)
          return '<I>retrieving...</I>';

        var remoteStatus = ('' + status.remoteStatus).toLowerCase();
        var sOut = (status.localStatus == 'IN_SERVICE') ? 'In Service' : 'Out of Service';

        if (parent.repoType == 'proxy')
        {

          if (status.proxyMode.search(/BLOCKED/) === 0)
          {
            sOut += status.proxyMode == 'BLOCKED_AUTO' ? ' - Remote Automatically Blocked' : ' - Remote Manually Blocked';
            sOut += remoteStatus == 'available' ? ' and Available' : ' and Unavailable';
          }
          else
          { // allow
            if (status.localStatus == 'IN_SERVICE')
            {
              if (remoteStatus != 'available')
              {
                sOut += remoteStatus == 'unknown' ? ' - <I>checking remote...</I>' : ' - Attempting to Proxy and Remote Unavailable';
              }
            }
            else
            { // Out of service
              sOut += remoteStatus == 'available' ? ' - Remote Available' : ' - Remote Unavailable';
            }
          }
        }

        return sOut;
      },

      updateRepoStatuses : function(status, rec) {
        rec.beginEdit();
        rec.data.status = status;
        rec.set('displayStatus', this.statusConverter(status, rec.data));
        rec.commit();
        rec.endEdit();

        if (status.dependentRepos)
        {
          for (var i = 0; i < status.dependentRepos.length; i++)
          {
            var status2 = status.dependentRepos[i];
            var rec2 = rec.store.getById(Sonatype.config.host + Sonatype.config.repos.urls.repositories + '/' + status2.id);
            if (rec2)
            {
              rec2.beginEdit();
              rec2.data.status = status2;
              rec2.set('displayStatus', this.statusConverter(status2, rec2.data));
              rec2.commit();
              rec2.endEdit();
            }
          }
        }

        Sonatype.Events.fireEvent('nexusRepositoryStatus', status);
      },

      onRepositoryMenuInit : function(menu, repoRecord) {
        if (repoRecord.id.substring(0, 4) == 'new_' || !repoRecord.data.exposed || !repoRecord.data.userManaged)
          return;

        var isVirtual = repoRecord.get('repoType') == 'virtual';
        var isHosted = repoRecord.get('repoType') == 'hosted';
        var isProxy = repoRecord.get('repoType') == 'proxy';
        var isGroup = repoRecord.get('repoType') == 'group';
        var isMaven = repoRecord.get('format') == 'maven2' || repoRecord.get('format') == 'maven1';

        if (this.sp.checkPermission('nexus:cache', this.sp.DELETE) && !isVirtual)
        {
          menu.add(this.repoActions.clearCache);
        }

        if (this.sp.checkPermission('nexus:metadata', this.sp.DELETE) && isMaven && (isHosted || isGroup))
        {
          menu.add(this.repoActions.rebuildMetadata);
        }
        menu.add('-');

        if (this.sp.checkPermission('nexus:repostatus', this.sp.EDIT))
        {
          if (isProxy)
          {
            if (repoRecord.data.status && repoRecord.data.status.proxyMode == 'ALLOW')
            {
              menu.add(this.repoActions.blockProxy);
            }
            else
            {
              menu.add(this.repoActions.allowProxy);
            }
          }

          if (!isGroup)
          {
            if (repoRecord.data.status && repoRecord.data.status.localStatus == 'IN_SERVICE')
            {
              menu.add(this.repoActions.putOutOfService);
            }
            else
            {
              menu.add(this.repoActions.putInService);
            }
          }
          menu.add('-');
        }
      }
    });

Sonatype.repoServer.DefaultRepoHandler = new Sonatype.repoServer.AbstractRepoPanel();
Sonatype.Events.addListener('repositoryMenuInit', Sonatype.repoServer.DefaultRepoHandler.onRepositoryMenuInit, Sonatype.repoServer.DefaultRepoHandler);
