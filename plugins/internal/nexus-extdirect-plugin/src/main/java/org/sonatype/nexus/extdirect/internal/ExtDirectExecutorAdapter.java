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

package org.sonatype.nexus.extdirect.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.extdirect.ux.model.Response;

import com.director.core.DirectAction;
import com.director.core.DirectContext;
import com.director.core.DirectException;
import com.director.core.DirectMethod;
import com.director.core.DirectTransactionData;
import com.director.core.DirectTransactionResult;
import com.director.core.ExecutorAdapter;
import com.director.core.json.JsonParser;
import com.google.inject.Key;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.inject.BeanLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.extdirect.ux.model.Responses.error;
import static org.sonatype.nexus.extdirect.ux.model.Responses.invalid;
import static org.sonatype.nexus.extdirect.ux.model.Responses.success;

/**
 * An {@link ExecutorAdapter} that invokes actions on guice components.
 *
 * @since 2.8
 */
@Named
@Singleton
public class ExtDirectExecutorAdapter
    implements ExecutorAdapter
{

  private static final Logger LOG = LoggerFactory.getLogger(ExtDirectExecutorAdapter.class);

  private final BeanLocator beanLocator;

  @Inject
  public ExtDirectExecutorAdapter(final BeanLocator beanLocator) {
    this.beanLocator = checkNotNull(beanLocator);
  }

  @Override
  public DirectTransactionResult execute(final DirectAction directAction,
                                         final DirectMethod directMethod,
                                         final DirectTransactionData data) throws DirectException
  {
    try {
      LOG.debug("Invoking {}#{}", directAction.getName(), directMethod.getName());
      Iterable<BeanEntry<Annotation, Object>> actionInstance = beanLocator.locate(
          Key.get(directAction.getActionClass())
      );
      Object actionClassInstance = actionInstance.iterator().next().getValue();
      Object result = directMethod.getMethod().invoke(actionClassInstance, directMethod.parseParameters(data));
      return result(directMethod, result);
    }
    catch (InvocationTargetException e) {
      LOG.error(
          "Failed to invoke action method {} of direct class {}",
          directMethod.getName(), directAction.getName(), e.getTargetException()
      );
      if (e.getTargetException() instanceof InvalidConfigurationException) {
        return result(directMethod, invalid((InvalidConfigurationException) e.getTargetException()));
      }
      return result(directMethod, error(e.getTargetException().getMessage()));
    }
    catch (Throwable e) {
      LOG.error(
          "Failed to invoke action method {} of direct class {}",
          directMethod.getName(), directAction.getName(), e
      );
      if (e instanceof InvalidConfigurationException) {
        return result(directMethod, invalid((InvalidConfigurationException) e));
      }
      return result(directMethod, error(e.getMessage()));
    }
  }

  private DirectTransactionResult result(final DirectMethod directMethod, final Object result) {
    Response response;
    if (result == null) {
      response = success();
    }
    else {
      if (result instanceof Response) {
        response = (Response) result;
      }
      else {
        response = success(result);
      }
    }
    JsonParser parser = DirectContext.get().getConfiguration().getParser();
    return parser.buildResult(directMethod, response);
  }

}
