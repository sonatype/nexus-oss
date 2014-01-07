/*
 * Copyright (c) 2011-2013,  original author or authors.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.director.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Copied from direct-core in order to fix url & add logging.
 *
 * @since 2.8
 * @see #getUrl()
 */
public abstract class AbstractProvider implements Provider {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractProvider.class);

   private String id;
   private String namespace;
   private ProviderType type;
   private DirectConfiguration configuration;

   protected AbstractProvider(String id, String namespace, ProviderType type, DirectConfiguration configuration) {
      this.id = id;
      this.namespace = namespace;
      this.type = type;
      this.configuration = configuration;
   }

   public String getUrl() {
     String routerUrl = this.configuration.getRouterUrl();
     String providerParamName = this.configuration.getProviderParamName();
     return String.format("%s?%s=%s", routerUrl, providerParamName, this.id);
   }

   public String getId() {
      return this.id;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public String getType() {
      return this.type.getTypeName();
   }

   public void process(HttpServletRequest request, HttpServletResponse response) {
      try {
         DirectContext.init(request, response, this.configuration);
         this.handleProcess();
         this.handleResponse();
      } finally {
         DirectContext.dispose();
      }
   }

   private void handleProcess() {
      try {
         this.doProcess();
      } catch(Throwable e) {
         LOG.error("Provider error, provider id " + this.id + " error executing direct request ", e);
         DirectContext.get().pushEvent(new DirectExceptionEvent(e));
      }
   }

   private void handleResponse() {
      try {
         this.formatForOutput();
      } catch(Exception e) {
         LOG.error("Provider error, provider id " + this.id + " error formatting direct request output", e);
      }
   }

   protected abstract void doProcess() throws Throwable;

   protected abstract void formatForOutput() throws IOException;
}
