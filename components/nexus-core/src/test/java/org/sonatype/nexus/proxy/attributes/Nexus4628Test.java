/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.attributes;

import java.io.IOException;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

import org.junit.Test;
import org.mockito.Mockito;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * NEXUS-4628: Lessen the occurence of "lastRequested" attribute storing to 24h, to lessen IO in general.
 *
 * @author cstamas
 */
public class Nexus4628Test
    extends AbstractAttributesHandlerTest
{
  protected ResourceStoreRequest fakeRemoteRequest;

  protected RepositoryItemUid uid;

  protected long beforeCallTs;

  protected long afterCallTs;

  @Override
  public void setUp()
      throws Exception
  {
    super.setUp();

    fakeRemoteRequest = new ResourceStoreRequest("/activemq/activemq-core/1.2/activemq-core-1.2.jar");
    fakeRemoteRequest.getRequestContext().put(AccessManager.REQUEST_REMOTE_ADDRESS, "192.168.1.1");

    uid = getRepositoryItemUidFactory().createUid(repository, fakeRemoteRequest.getRequestPath());

    Attributes aitem = attributesHandler.getAttributeStorage().getAttributes(uid);
    assertThat(aitem, nullValue());

    beforeCallTs = System.currentTimeMillis();
    repository.recreateAttributes(new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT, true), null);
    afterCallTs = System.currentTimeMillis();
  }

  /**
   * Set up repo, and recreate attributes (this will set lastRequested too, since items does not have any attributes
   * yet). Then "touch" them, but nothing should happen (no change and no IO) since we are within the "resolution".
   */
  @Test
  public void testSimpleTouchIsDoneOnceDaily()
      throws IOException, ItemNotFoundException
  {
    Attributes aitem;

    final AttributeStorage attributeStorageSpy = spyOnRealAttributeStorage();

    aitem = attributesHandler.getAttributeStorage().getAttributes(uid);
    checkNotNull(aitem);
    assertThat(aitem.getLastRequested(),
        allOf(greaterThanOrEqualTo(beforeCallTs), lessThanOrEqualTo(afterCallTs)));

    attributesHandler.touchItemLastRequested(System.currentTimeMillis(), fakeRemoteRequest, uid, aitem);

    aitem = attributesHandler.getAttributeStorage().getAttributes(uid);
    checkNotNull(aitem);
    assertThat(aitem.getLastRequested(),
        allOf(greaterThanOrEqualTo(beforeCallTs), lessThanOrEqualTo(afterCallTs)));

    Mockito.verify(attributeStorageSpy, Mockito.times(2)).getAttributes(Mockito.<RepositoryItemUid>any());
    Mockito.verify(attributeStorageSpy, Mockito.times(0)).putAttributes(Mockito.<RepositoryItemUid>any(),
        Mockito.<Attributes>any());
  }

  /**
   * The "touch" into past should always work as before: as many WRITEs as many touches happens.
   */
  @Test
  public void testSimpleTouchInPastAlwaysWork()
      throws IOException, ItemNotFoundException
  {
    Attributes aitem;

    final AttributeStorage attributeStorageSpy = spyOnRealAttributeStorage();

    aitem = attributesHandler.getAttributeStorage().getAttributes(uid);
    checkNotNull(aitem);
    assertThat(aitem.getLastRequested(),
        allOf(greaterThanOrEqualTo(beforeCallTs), lessThanOrEqualTo(afterCallTs)));

    final long past = System.currentTimeMillis() - 10000;
    attributesHandler.touchItemLastRequested(past, fakeRemoteRequest, uid, aitem);

    aitem = attributesHandler.getAttributeStorage().getAttributes(uid);
    checkNotNull(aitem);
    assertThat(aitem.getLastRequested(), equalTo(past));

    final long past2 = past - 10000;
    attributesHandler.touchItemLastRequested(past2, fakeRemoteRequest, uid, aitem);

    aitem = attributesHandler.getAttributeStorage().getAttributes(uid);
    checkNotNull(aitem);
    assertThat(aitem.getLastRequested(), equalTo(past2));

    Mockito.verify(attributeStorageSpy, Mockito.times(3)).getAttributes(Mockito.<RepositoryItemUid>any());
    Mockito.verify(attributeStorageSpy, Mockito.times(2)).putAttributes(Mockito.<RepositoryItemUid>any(),
        Mockito.<Attributes>any());
  }

  // ==

  protected AttributeStorage spyOnRealAttributeStorage() {
    final DelegatingAttributeStorage das =
        (DelegatingAttributeStorage) repository.getAttributesHandler().getAttributeStorage();
    final AttributeStorage attributeStorageSpy = Mockito.spy(das.getDelegate());
    repository.getAttributesHandler().setAttributeStorage(attributeStorageSpy);
    return attributeStorageSpy;
  }

}
