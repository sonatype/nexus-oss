/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Test the ability to create, update and delete a new Repository in the UI.
 */
StartTest(function(t) {

  var name = t.uniqueId(),
      waitForStoreToLoad = {
        waitFor: 'storesToLoad', args: function() {
          return t.cq1('nx-coreui-repository-list').getStore();
        }
      };

  t.describe('Repository administration', function(t) {
    t.it('Default repositories are populated', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/repository/repositories'),
          function(next) {
            t.waitForAnimations(next);
          },
          {waitFor: 'CQ', args: 'nx-coreui-repository-list'},
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                store = grid.getStore(),
                mavenRepos = store.query('format', 'maven2').collect('name', 'data'),
                nugetRepos = store.query('format', 'nuget').collect('name', 'data');
            ['maven-central', 'maven-public', 'maven-releases', 'maven-snapshots'].forEach(
                function(element, index, array) {
                  
                  t.expect(mavenRepos).toContain(element);
                });
            ['nuget-group', 'nuget-hosted', 'nuget.org-proxy'].forEach(
                function(element, index, array) {
                  t.expect(nugetRepos).toContain(element);
                });
            next();
          }
      )
    }, 300000);
    t.it('Repository creation', function(t) {
      t.chain(
          {click: '>>nx-coreui-repository-list button[text=Create repository]'},
          function(next) {
            t.waitForAnimations(next);
          },
          // pick the type of repo to create
          function(next) {
            var grid = t.cq1('nx-coreui-repository-selectrecipe'),
                mavenHostedRow = t.getRow(grid, grid.getStore().find('id', 'maven2-hosted'));
            t.click(mavenHostedRow, next);
          },

          function(next) {
            t.waitForAnimations(next);
          },

          // fill out the form
          {type: name, target: '>>nx-coreui-repository-add field[name=name]'},
          {click: '#versionPolicy => .x-form-text'},
          {click: '#versionPolicy.getPicker() => .x-boundlist-item:contains(Mixed)'},
          {click: '#blobStoreName => .x-form-text'},
          {click: '#blobStoreName.getPicker() => .x-boundlist-item:contains(default)'},
          {click: '#writePolicy => .x-form-text'},
          {click: '#writePolicy.getPicker() => .x-boundlist-item:contains(Allow redeploy)'},

          // we are ready to go, so press the "Add" button and wait for grid to refresh
          Ext.apply(Ext.clone(waitForStoreToLoad),
              {trigger: {click: '>>nx-coreui-repository-add button[action=add]'}}
          ),
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name);
            t.is(model.get('type'), 'hosted');
            t.is(model.get('format'), 'maven2');
            t.expect(model.get('url')).toBe(t.anyStringLike('repository/' + name + '/'));
            t.is(model.get('attributes').storage.writePolicy, 'ALLOW');
            t.is(model.get('attributes').storage.strictContentTypeValidation, true);
            next();
          }
      );
    }, 300000);
    t.it('Repository display', function(t) {
      t.chain(
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                testRepoRow = t.getRow(grid, grid.getStore().find('name', name));
            t.click(testRepoRow, next);
          },
          function(next) {
            t.waitForAnimations(next);
          },
          // confirm the state of displayed data
          function(next) {
            var form = t.cq1('nx-coreui-repository-maven2-hosted');
            t.is(form.down('#name').readOnly, true);
            t.is(form.down('#format').readOnly, true);
            t.is(form.down('#format').getValue(), 'maven2');
            t.is(form.down('#type').readOnly, true);
            t.is(form.down('#type').getValue(), 'hosted');
            t.is(form.down('#url').readOnly, true);
            t.expect(form.down('#url').getValue()).toBe(t.anyStringLike('repository/' + name + '/'));
            t.is(form.down('#online').readOnly, false);
            t.is(form.down('#versionPolicy').readOnly, true);
            t.is(form.down('#versionPolicy').getValue(), 'MIXED');
            t.is(form.down('#strictContentTypeValidation').readOnly, false);
            t.is(form.down('#strictContentTypeValidation').getValue(), true);
            t.is(form.down('#writePolicy').readOnly, false);
            t.is(form.down('#writePolicy').getValue(), 'ALLOW');

            next();
          }
      );
    }, 300000);
    t.it('Repository update', function(t) {
      t.chain(
          //update the writePolicy
          {click: '#writePolicy => .x-form-text'},
          {click: '#writePolicy.getPicker() => .x-boundlist-item:contains(Disable redeploy)'},
          Ext.apply(Ext.clone(waitForStoreToLoad),
              {trigger: {click: '>>nx-settingsform button[action=save]'}}
          ),          
          function(next) {
            t.waitForAnimations(next);
          },
          function(next) {
            t.navigateTo('admin/repository/repositories');
            next()
          },
          {waitFor: 'CQ', args: 'nx-coreui-repository-list'},
          function(next) {
            t.waitForAnimations(next);
          },
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name);
            t.is(model.get('attributes').storage.writePolicy, 'ALLOW_ONCE');
            next();
          }
      );
    }, 300000);
    t.it('Repository delete', function(t) {
      t.chain(
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                testRepoRow = t.getRow(grid, grid.getStore().find('name', name));
            t.click(testRepoRow, next);
          },
          function(next) {
            t.waitForAnimations(next);
          },
          {click: '>>nx-actions button[text=Delete repository] => .x-btn-inner'},
          Ext.apply(Ext.clone(waitForStoreToLoad),
              {trigger: {click: '>>button[text=Yes]'}}
          ),
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name);

            t.ok(!model, 'Repository "' + name + '" has been removed');
            next();
          }
      );
    }, 300000)

  }, 300000);
});
