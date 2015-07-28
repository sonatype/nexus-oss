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

  var name = t.uniqueId();

  t.describe('Repository administration', function(t) {
    var message = t.spyOn(NX.application.getController('Message'), 'addMessage').and.callThrough();
    t.beforeEach(function(){
      message.reset();
    });

    t.it('Default repositories are populated', function(t) {
      t.chain(
          t.openPageAsAdmin('admin/repository/repositories'),
          {waitForRowsVisible: 'nx-coreui-repository-list'},
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
    });
    t.it('Repository creation', function(t) {
      t.chain(
          {click: '>>nx-coreui-repository-list button[text=Create repository]'},
          {waitForCQVisible: '>>nx-coreui-repository-selectrecipe'},
          // pick the type of repo to create
          function(next, selectRecipe) {
            var grid = selectRecipe[0],
                mavenHostedRow = t.getRow(grid, grid.getStore().find('id', 'maven2-hosted'));
            t.click(mavenHostedRow, next);
          },
          function(next){
            t.waitForAnimations(next);
          },
          {waitForCQ: '>>nx-coreui-repository-add field[name=name]'},
          // fill out the form
          {type: name, target: '>>nx-coreui-repository-add field[name=name]'},
          {click: '#versionPolicy => .x-form-text'},
          {click: '#versionPolicy.getPicker() => .x-boundlist-item:contains(Mixed)'},
          {click: '#blobStoreName => .x-form-text'},
          {click: '#blobStoreName.getPicker() => .x-boundlist-item:contains(default)'},
          {click: '#writePolicy => .x-form-text'},
          {click: '#writePolicy.getPicker() => .x-boundlist-item:contains(Allow redeploy)'},

          // we are ready to go, so press the "Add" button and wait for grid to refresh
          Ext.apply(t.waitForStore('Repository'),
              {trigger: {click: '>>nx-coreui-repository-add button[action=add]'}}
          ),
          {waitForSpyToBeCalled: message},
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name);
            t.is(model.get('type'), 'hosted');
            t.is(model.get('format'), 'maven2');
            t.expect(model.get('url')).toBe(t.anyStringLike('repository/' + name + '/'));
            t.is(model.get('attributes').storage.writePolicy, 'ALLOW');
            t.is(model.get('attributes').storage.strictContentTypeValidation, true);

            t.expect(message).toHaveBeenCalled();
            var lastMessage = message.calls.mostRecent().args[0];
            t.expect(lastMessage.text).toBe('Repository created: ' + name);
            t.expect(lastMessage.type).toBe('success');
            next();
          }
      );
    });
    t.it('Repository display', function(t) {
      t.chain(
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                testRepoRow = t.getRow(grid, grid.getStore().find('name', name));
            t.click(testRepoRow, next);
          },
          {waitForCQ: 'nx-coreui-repository-maven2-hosted'},
          // confirm the state of displayed data
          function(next, forms) {
            var form = forms[0];
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
    });
    t.it('Repository update', function(t) {
      t.chain(
          //update the writePolicy
          {click: '#writePolicy => .x-form-text'},
          {click: '#writePolicy.getPicker() => .x-boundlist-item:contains(Disable redeploy)'},
          Ext.apply(t.waitForStore('Repository'),
              {trigger: {click: '>>nx-coreui-repository-settings button[action=save]'}}
          ),
          {waitForSpyToBeCalled: message},
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name);
            t.is(model.get('attributes').storage.writePolicy, 'ALLOW_ONCE');

            t.expect(message).toHaveBeenCalled();
            var lastMessage = message.calls.mostRecent().args[0];
            t.expect(lastMessage.text).toBe('Repository updated: ' + name);
            t.expect(lastMessage.type).toBe('success');
            next();
          }
      );
    }, 60000);
    t.it('Repository delete', function(t) {
      t.chain(
          {click: '>>nx-actions button[text=Delete repository] => .x-btn-inner'},
          Ext.apply(t.waitForStore('Repository'),
              {trigger: {click: '>>button[text=Yes]'}}
          ),
          {waitForSpyToBeCalled: message},
          function(next) {
            var grid = t.cq1('nx-coreui-repository-list'),
                store = grid.getStore(),
                model = store.findRecord('name', name);

            t.ok(!model, 'Repository "' + name + '" has been removed');

            t.expect(message).toHaveBeenCalled();
            var lastMessage = message.calls.mostRecent().args[0];
            t.expect(lastMessage.text).toBe('Repository deleted: ' + name);
            t.expect(lastMessage.type).toBe('success');
            next();
          }
      );
    })

  });
});
