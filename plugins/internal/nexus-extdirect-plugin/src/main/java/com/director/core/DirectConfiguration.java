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

import com.director.core.annotation.DirectMethod;
import com.director.core.json.JsonParser;
import com.director.core.json.impl.gson.GsonParser;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Copied from direct-core in order to allow registration by class && customize generated api for ExtJS3.
 *
 * @since 2.8
 * @see #registerClass(Class, String, String)
 * @see #getFormattedApi()
 */
public class DirectConfiguration {

   private static final String DEFAULT_WRAP_PREFIX = "/* ";
   private static final String DEFAULT_WRAP_SUFFIX = " */ ";

   private String routerUrl = "DirectRouter";
   private String providerParamName = "providerId";

   private String uploadDir = "";
   private long uploadMaxSize;

   private boolean preventScriptHijacking;
   private String wrapPrefix = DEFAULT_WRAP_PREFIX;

   private String wrapSuffix = DEFAULT_WRAP_SUFFIX;
   private JsonParser parser = new GsonParser();
   private ExecutorAdapter executorAdapter = new SimpleExecutorAdapter();
   private Map<Class, ParameterFactory> paramFactories = new HashMap<Class, ParameterFactory>();

   private Map<String, Provider> providers = new HashMap<String, Provider>();

   public DirectConfiguration() {
   }

   public String getUploadDir() {
      return this.uploadDir;
   }

   public void setUploadDir(String uploadDir) {
      this.uploadDir = uploadDir;
   }

   public long getUploadMaxSize() {
      return this.uploadMaxSize;
   }

   public void setUploadMaxSize(long uploadMaxSize) {
      this.uploadMaxSize = uploadMaxSize;
   }

   public void setPreventScriptHijacking(boolean preventScriptHijacking) {
      this.preventScriptHijacking = preventScriptHijacking;
   }

   public boolean isPreventScriptHijacking() {
      return this.preventScriptHijacking;
   }

   public void setWrapPrefix(String wrapPrefix) {
      this.wrapPrefix = wrapPrefix;
   }

   public String getWrapPrefix() {
      return this.wrapPrefix;
   }

   public void setWrapSuffix(String wrapSuffix) {
      this.wrapSuffix = wrapSuffix;
   }

   public String getWrapSuffix() {
      return this.wrapSuffix;
   }

   public void setRouterUrl(String routerUrl) {
      this.routerUrl = routerUrl;
   }

   public String getRouterUrl() {
      return this.routerUrl;
   }

   public void setProviderParamName(String providerParamName) {
      this.providerParamName = providerParamName;
   }

   public String getProviderParamName() {
      return this.providerParamName;
   }

   public void registerAdapter(ExecutorAdapter executorAdapter) {
      this.executorAdapter = executorAdapter;
   }

   public ExecutorAdapter getExecutorAdapter() {
      return this.executorAdapter;
   }

   public void registerParser(JsonParser parser) {
      this.parser = parser;
   }

   public JsonParser getParser() {
      return this.parser;
   }

   public void registerParameterFactory(Class parameterType, ParameterFactory factory) {
      this.paramFactories.put(parameterType, factory);
   }

   public ParameterFactory getParameterFactory(Class parameterType) {
      return this.paramFactories.get(parameterType);
   }

   public Collection<Provider> getProviders() {
      return Collections.unmodifiableCollection(this.providers.values());
   }

   public Provider getProvider(String id) {
      return this.providers.get(id);
   }

   public String getFormattedApi() {
      String formattedApi = "Ext.direct.PROVIDER_BASE_URL=window.location.protocol + '//' + window.location.host + '/' + (window.location.pathname.split('/').length>2 ? window.location.pathname.split('/')[1]+ '/' : '');";
      for(Provider provider : providers.values()) {
         formattedApi += "Ext.Direct.addProvider(" + this.parser.format(provider).replace("\"url\":\"/service/extdirect/DirectRouter","\"url\":Ext.direct.PROVIDER_BASE_URL+\"service/extdirect/DirectRouter") + ");";
      }
      return formattedApi;
   }

   /**
    * Register a java class as Direct Action, adding all methods annotated with the DirectMethod annotation.
    *
    * @param className full class name of the class to register.
    * @param directNameSpace the direct namespace name.
    * @param directActionName the direct action name.
    * @throws DirectException
    */
   public void registerClass(String className, String directNameSpace, String directActionName) throws DirectException {

      if(StringUtils.isBlank(className)) {
         throw new IllegalArgumentException("Class name must be specified");
      }

      try {
         Class actionClass = Class.forName(className);
         registerClass(actionClass, directNameSpace, directActionName);
      } catch(Exception e) {
         throw new DirectException("Error registering class: " + className + " with actionName: " + directActionName, e);
      }
   }

  public void registerClass(Class<?> actionClass, String directNameSpace, String directActionName) throws DirectException {

    try {
      Method[] methods = actionClass.getMethods();
      for(Method method : methods) {
        if(method.isAnnotationPresent(DirectMethod.class)) {
          DirectMethod directMethod = method.getAnnotation(DirectMethod.class);
          String methodName = StringUtils.isBlank(directMethod.name()) ? method.getName() : directMethod.name();
          this.registerMethod(directNameSpace, directActionName, methodName, actionClass, method);
        }
      }
    } catch(Exception e) {
      throw new DirectException("Error registering class: " + actionClass + " with actionName: " + directActionName, e);
    }
  }

   /**
    *
    *
    * @param className full class name containing the method to register.
    * @param methodName name of the method to register.
    * @param directNameSpace the direct namespace name.
    * @param directActionName the direct action name.
    * @param directMethodName
    * @throws DirectException
    */
   public void registerMethod(String className,
                              String methodName,
                              String directNameSpace,
                              String directActionName,
                              String directMethodName) throws DirectException {

      if(StringUtils.isBlank(className)) {
         throw new IllegalArgumentException("Class name must be specified");
      }
      if(StringUtils.isBlank(methodName)) {
         throw new IllegalArgumentException("Method name must be specified");
      }
      if(StringUtils.isBlank(directNameSpace)) {
         directNameSpace = null;
      }

      try {
         Class actionClass = Class.forName(className);
         Method[] methods = actionClass.getMethods();
         for(Method method : methods) {
            if(methodName.equals(method.getName()) && method.isAnnotationPresent(DirectMethod.class)) {
               this.registerMethod(directNameSpace, directActionName, directMethodName, actionClass, method);
            }
         }
      } catch(Exception e) {
         throw new DirectException("Error registering method " + directMethodName, e);
      }
   }

   /**
    * Register the
    *
    * @param directNameSpace
    * @param directActionName
    * @param directMethodName
    * @param actionClass
    * @param method
    * @throws Exception
    */
   private void registerMethod(String directNameSpace,
                               String directActionName,
                               String directMethodName,
                               Class actionClass,
                               Method method) throws Exception {

      DirectMethod directMethod = method.getAnnotation(DirectMethod.class);
      ProviderType type = directMethod.providerType();
      String providerId = type.getProviderId(directNameSpace);
      Provider provider = this.getProvider(directNameSpace, type, providerId);

      String aName = (directActionName == null) ? actionClass.getSimpleName() : directActionName;
      String mName = (directMethodName == null) ? method.getName() : directMethodName;
      provider.registerMethod(aName, mName, actionClass, method);
   }

   /**
    * Get the provider by the unique Id from the cache of already instantiated ones.
    * If the provider doesn't exist, a new one is created and added to the cache.
    *
    * @param namespace
    * @param type
    * @param providerId
    * @return the provider
    * @throws Exception
    */
   private Provider getProvider(String namespace, ProviderType type, String providerId) throws Exception {

      Provider provider = this.getProvider(providerId);
      if(provider == null) {
         provider = ProviderFactory.create(providerId, namespace, type, this);
         this.providers.put(providerId, provider);
      }
      return provider;
   }
}
