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
package com.sonatype.nexus.repository.nuget.internal;

import java.io.InputStream;
import java.util.ArrayList;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import org.apache.commons.fileupload.util.Streams;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
public class NugetPushHandlerTest
    extends TestSupport
{
  Context context = mock(Context.class);

  Request request = mock(Request.class);

  Repository repository = mock(Repository.class);

  StorageTx tx = mock(StorageTx.class);

  StorageFacet storageFacet = mock(StorageFacet.class);

  NugetGalleryFacet nugetGalleryFacet = mock(NugetGalleryFacet.class);

  NugetPushHandler underTest = new NugetPushHandler();

  @Before
  public void setup() throws Exception {
    when(context.getRequest()).thenReturn(request);
    when(context.getRepository()).thenReturn(repository);
    when(storageFacet.txSupplier()).thenReturn(Suppliers.ofInstance(tx));
    when(repository.facet(StorageFacet.class)).thenReturn(storageFacet);
    when(repository.facet(NugetGalleryFacet.class)).thenReturn(nugetGalleryFacet);
  }

  @Test
  public void testPutOneMultipart() throws Exception {
    Payload requestPayload = mock(Payload.class);
    InputStream inputStream = mock(InputStream.class);

    when(request.getAction()).thenReturn(HttpMethods.PUT);
    when(request.isMultipart()).thenReturn(true);
    when(request.getMultiparts()).thenReturn(Lists.newArrayList(requestPayload));
    when(requestPayload.openInputStream()).thenReturn(inputStream);

    Response response = underTest.handle(context);
    checkResponse(response, HttpStatus.CREATED, ContentTypes.TEXT_HTML, "<html><body></body></html>");

    verify(nugetGalleryFacet).put(inputStream);
    verifyNoMoreInteractions(nugetGalleryFacet);
  }

  @Test
  public void testPutTwoMultipart() throws Exception {
    Payload requestPayload = mock(Payload.class);
    InputStream inputStream = mock(InputStream.class);

    when(request.getAction()).thenReturn(HttpMethods.PUT);
    when(request.isMultipart()).thenReturn(true);
    when(request.getMultiparts()).thenReturn(Lists.newArrayList(requestPayload, requestPayload));
    when(requestPayload.openInputStream()).thenReturn(inputStream);

    Response response = underTest.handle(context);
    checkResponse(response, HttpStatus.CREATED, ContentTypes.TEXT_HTML, "<html><body></body></html>");

    verify(nugetGalleryFacet, times(2)).put(inputStream);
    verifyNoMoreInteractions(nugetGalleryFacet);
  }

  @Test
  public void testPutMultipartEmpty() throws Exception {
    when(request.getAction()).thenReturn(HttpMethods.PUT);
    when(request.isMultipart()).thenReturn(true);
    when(request.getMultiparts()).thenReturn(new ArrayList<Payload>());

    Response response = underTest.handle(context);
    checkResponse(response, HttpStatus.BAD_REQUEST, ContentTypes.APPLICATION_XML, "No content was provided");
  }

  @Test
  public void testPutNoMultipart() throws Exception {
    when(request.getAction()).thenReturn(HttpMethods.PUT);
    when(request.isMultipart()).thenReturn(false);

    Response response = underTest.handle(context);
    checkResponse(response, HttpStatus.BAD_REQUEST, ContentTypes.APPLICATION_XML, "Multipart request required");
  }

  private void checkResponse(final Response response,
                             final int expectedCode,
                             final String expectedContentType,
                             final String expectedSubstring)
      throws Exception
  {
    assertThat(response.getStatus().getCode(), is(expectedCode));
    assertThat(response.getPayload(), notNullValue());
    Payload responsePayload = response.getPayload();
    assertThat(responsePayload.getContentType(), is(expectedContentType));
    String responseBody = Streams.asString(responsePayload.openInputStream());
    assertThat(responseBody.contains(expectedSubstring), is(true));
  }
}