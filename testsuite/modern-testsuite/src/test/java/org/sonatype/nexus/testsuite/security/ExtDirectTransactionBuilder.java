/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.security;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Maps;
import org.eclipse.jetty.util.ajax.JSON;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generic builder for Ext Direct transaction payloads.
 *
 * @see <a href="http://www.sencha.com/products/extjs/extdirect">Sencha Ext Direct spec defining transaction payload</a>
 * @since 3.0
 */
public class ExtDirectTransactionBuilder
{

  private Long tid = 1L;

  private String type = "rpc";

  private String action;

  private String method;

  private String data;

  public ExtDirectTransactionBuilder withType(final String type) {
    this.type = type;
    return this;
  }

  public ExtDirectTransactionBuilder withAction(final String action) {
    this.action = action;
    return this;
  }

  public ExtDirectTransactionBuilder withMethod(final String method) {
    this.method = method;
    return this;
  }

  public ExtDirectTransactionBuilder withData(final String data) {
    this.data = data;
    return this;
  }

  public ExtDirectTransactionBuilder withId(final Long tid) {
    checkNotNull(tid);
    this.tid = tid;
    return this;
  }

  /**
   * @return JSON representation of transaction
   */
  @Override public String toString() {
    return toJson();
  }

  /**
   * Output this builder as JSON.
   *
   * @return JSON representation of this instance
   */
  public String toJson() {
    HashMap<String, Object> args = Maps.newLinkedHashMap();
    args.put("tid", this.tid);
    args.put("type", this.type);
    args.put("data", this.data);
    args.put("action", this.action);
    args.put("method", this.method);
    return JSON.toString(args);
  }

  public String toJson(List<ExtDirectTransactionBuilder> transactions) {
    return JSON.toString(transactions);
  }

}
