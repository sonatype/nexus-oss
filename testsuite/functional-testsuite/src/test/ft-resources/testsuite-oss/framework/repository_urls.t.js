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
StartTest(function(t) {

  /**
   * Verify a string representation of a link HTMLElement with the specified text and pathname.
   * @private
   * @param link
   * @param text
   * @param pathname
   * @param t {@link Siesta.Test}
   */
  function verify(link, text, pathname, t) {
    var element = new Ext.Element(document.createElement('div'));
    element.insertHtml('beforeEnd',link);
    var a = element.down('a').dom;
    t.expect(a.target).toBe('_blank');
    t.expect(a.text).toBe(text);
    t.expect(a.pathname).toBe(pathname);
    element.destroy();
  }

  t.ok(Ext, 'Ext is here');
  t.ok(NX, 'NX is here');

  t.requireOk('NX.coreui.util.RepositoryUrls', 'NX.coreui.model.Asset', function() {
    t.describe('Can create download urls for asset files', function(t) {

      var mavenAsset = Ext.create('NX.coreui.model.Asset', {
        name: '/org/sonatype/test/1.0/test.jar',
        repositoryName: 'test'
      });
      var nugetAsset = Ext.create('NX.coreui.model.Asset', {
        name: 'test',
        repositoryName: 'test',
        attributes: {
          nuget: {
            version: '1.0'
          }
        }
      });
      var rawAsset = Ext.create('NX.coreui.model.Asset', {
        name: 'test.txt',
        repositoryName: 'test'
      });

      t.it('Can render a link for maven assets', function(t){
        var link = NX.coreui.util.RepositoryUrls.asRepositoryLink(mavenAsset, 'maven2');
        verify(link, '/org/sonatype/test/1.0/test.jar', '/repository/test/org/sonatype/test/1.0/test.jar', t);
      });
      t.it('Can render a link for nuget assets', function(t){
        var link = NX.coreui.util.RepositoryUrls.asRepositoryLink(nugetAsset, 'nuget');
        verify(link, '/test/1.0', '/repository/test/test/1.0', t);
      });
      t.it('Can render a link for raw assets', function(t){
        var link = NX.coreui.util.RepositoryUrls.asRepositoryLink(rawAsset, 'raw');
        verify(link, 'test.txt', '/repository/test/test.txt', t);
      });

    });
  });
});
