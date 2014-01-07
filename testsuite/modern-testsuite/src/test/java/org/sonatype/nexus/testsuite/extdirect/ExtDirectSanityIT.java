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

package org.sonatype.nexus.testsuite.extdirect;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.sonatype.nexus.client.core.subsystem.Utilities;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.testsuite.NexusCoreITSupport;

import com.sun.jersey.api.client.GenericType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Ext.Direct sanity IT.
 *
 * @since 2.8
 */
public class ExtDirectSanityIT
    extends NexusCoreITSupport
{

  public ExtDirectSanityIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  /**
   * Verify that generated Ext.Direct api contains discovered "Test" resource.
   */
  @Test
  public void api() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    client().getSubsystem(Utilities.class).download("js/extdirect.js", out);
    assertThat(out.toString("UTF-8"), containsString("\"Test\""));
  }

  /**
   * Verify that "Test" resource "currentTime" is invoked and expected results are returned.
   */
  @Test
  public void call() throws Exception {
    ExtDirectPayload payload = new ExtDirectPayload();
    payload.action = "Test";
    payload.method = "currentTime";

    List<ExtDirectResponse> results = ((JerseyNexusClient) client())
        .uri("service/extdirect/DirectRouter?providerId=NX.direct-remoting-provider")
        .type(MediaType.APPLICATION_JSON_TYPE)
        .post(new GenericType<List<ExtDirectResponse>>() {}, payload);

    assertThat(results, is(notNullValue()));
    assertThat(results, hasSize(1));
    ExtDirectResponse result = results.get(0);
    assertThat(result, is(notNullValue()));
    assertThat(result.result, is(notNullValue()));
    assertThat(result.result.success, is(true));
    assertThat(result.result.data, is(notNullValue()));
  }

  public static class ExtDirectPayload
  {
    private static int count = 0;

    @JsonProperty("action")
    private String action;

    @JsonProperty("method")
    private String method;

    @JsonProperty("data")
    private Object data;

    @JsonProperty("type")
    private String type = "rpc";

    @JsonProperty("tid")
    private int tid;

    public ExtDirectPayload() {
      tid = ++count;
    }

  }

  public static class ExtDirectResponse
  {
    @JsonProperty("action")
    private String action;

    @JsonProperty("method")
    private String method;

    @JsonProperty("result")
    private Result result;

    @JsonProperty("type")
    private String type = "rpc";

    @JsonProperty("tid")
    private int tid;

    public static class Result
    {
      @JsonProperty("success")
      private boolean success;

      @JsonProperty("data")
      private String data;
    }

  }

}
