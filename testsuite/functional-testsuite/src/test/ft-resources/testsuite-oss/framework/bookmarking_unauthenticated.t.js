/*
 * Copyright (c) 2008-2015 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Tests bookmarking as an unauthenticated user.
 */
StartTest(function(t) {

  var ignoredFeatures = [
        '/browse/Search',
        '/browse/Browse'
      ],
      authRequired = function(item) {
        return item.get('authenticationRequired') && ignoredFeatures.indexOf(item.get('path')) === -1;
      },
      noAuthRequired = function(item) {
        return !item.get('authenticationRequired') && ignoredFeatures.indexOf(item.get('path')) === -1;
      };

  t.ok(Ext);
  t.ok(NX);
  t.waitForSessionToBeInvalidated();
  
  t.describe('Can use bookmarks to navigate the UI', function(t) {
    var featureStore = t.Ext().StoreManager.get('Feature');
    featureStore.filter([
      {
        filterFn: noAuthRequired
      }
    ]);
    featureStore.data.each(function(feature) {
      var path = feature.get('path');
      t.it('Can navigate anonymously to ' + path, function(t) {
        t.chain(
            function(next) {
              t.navigateTo(feature.get('bookmark'));
              next();
            },
            {waitFor: 'bookmark', args: feature.get('bookmark')},
            {waitFor: 'Feature', args: feature.get('text')},
            function(next) {
              t.waitForFn(function() {
                return t.global.location.hash === '#' + feature.get('bookmark');
              }, next)
            }
        )
      })
    });
    featureStore.clearFilter(true);
    featureStore.filter([
      {
        filterFn: authRequired
      }
    ]);
    featureStore.data.each(function(feature) {
      var path = feature.get('path');
      t.it('Can not navigate anonymously to ' + path, function(t) {
        t.chain(
            function(next) {
              t.navigateTo(feature.get('bookmark'));
              next();
            },
            {waitFor: 'CQ', args: 'nx-feature-notvisible'}
        )
      })
    });
    featureStore.clearFilter(true);
  }, 300000)
});
