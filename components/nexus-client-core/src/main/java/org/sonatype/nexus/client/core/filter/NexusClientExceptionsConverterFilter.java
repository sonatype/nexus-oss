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
package org.sonatype.nexus.client.core.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.sonatype.nexus.client.core.exception.NexusClientAccessForbiddenException;
import org.sonatype.nexus.client.core.exception.NexusClientBadRequestException;
import org.sonatype.nexus.client.core.exception.NexusClientErrorResponseException;
import org.sonatype.nexus.client.core.exception.NexusClientErrorResponseException.ErrorMessage;
import org.sonatype.nexus.client.core.exception.NexusClientException;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.internal.msg.ErrorResponse;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.codehaus.plexus.util.IOUtil;

/**
 * A filter that converts some known http error codes to specific {@link NexusClientException}:
 * <ul>
 * <li>400 -> {@link NexusClientBadRequestException}</li>
 * <li>400 with errors body -> {@link NexusClientErrorResponseException}</li>
 * <li>403 -> {@link NexusClientAccessForbiddenException}</li>
 * <li>404 -> {@link NexusClientNotFoundException}</li>
 * </ul>
 *
 * @since 2.7
 */
public class NexusClientExceptionsConverterFilter
    extends ClientFilter
{

  @Override
  public ClientResponse handle(final ClientRequest request) throws ClientHandlerException {
    final ClientResponse response = getNext().handle(request);
    {
      final NexusClientException exception = convertIf404(response);
      if (exception != null) {
        throw exception;
      }
    }
    {
      final NexusClientException exception = convertIf403(response);
      if (exception != null) {
        throw exception;
      }
    }
    {
      final NexusClientException exception = convertIf400WithErrorMessage(response);
      if (exception != null) {
        throw exception;
      }
    }
    {
      final NexusClientException exception = convertIf400(response);
      if (exception != null) {
        throw exception;
      }
    }
    return response;
  }

  private NexusClientException convertIf400WithErrorMessage(final ClientResponse response) {
    if (ClientResponse.Status.BAD_REQUEST.equals(response.getClientResponseStatus())) {
      final String body = getResponseBody(response);
      try {
        response.bufferEntity();
        final ErrorResponse errorResponse = response.getEntity(ErrorResponse.class);
        if (errorResponse != null) {
          // convert them to hide stupid "old" REST model, and not have it leak out
          final ArrayList<ErrorMessage> errors =
              new ArrayList<NexusClientErrorResponseException.ErrorMessage>(errorResponse.getErrors().size());
          for (org.sonatype.nexus.client.internal.msg.ErrorMessage message : errorResponse.getErrors()) {
            errors.add(
                new NexusClientErrorResponseException.ErrorMessage(message.getId(), message.getMsg()));
          }
          return new NexusClientErrorResponseException(
              response.getClientResponseStatus().getReasonPhrase(),
              body,
              errors
          );
        }
      }
      catch (Exception ignore) {
        // ignore, probably we do not have an error response body
        ignore.printStackTrace();
      }
    }
    return null;
  }

  private NexusClientException convertIf400(final ClientResponse response) {
    if (ClientResponse.Status.BAD_REQUEST.equals(response.getClientResponseStatus())) {
      return new NexusClientBadRequestException(
          response.getClientResponseStatus().getReasonPhrase(),
          getResponseBody(response)
      );
    }
    return null;
  }

  private NexusClientException convertIf403(final ClientResponse response) {
    if (ClientResponse.Status.FORBIDDEN.equals(response.getClientResponseStatus())) {
      return new NexusClientAccessForbiddenException(
          response.getClientResponseStatus().getReasonPhrase(),
          getResponseBody(response)
      );
    }
    return null;
  }

  private NexusClientException convertIf404(final ClientResponse response) {
    if (ClientResponse.Status.NOT_FOUND.equals(response.getClientResponseStatus())) {
      return new NexusClientNotFoundException(
          response.getClientResponseStatus().getReasonPhrase(),
          getResponseBody(response)
      );
    }
    return null;
  }

  public String getResponseBody(final ClientResponse response) {
    try {
      final byte[] body = IOUtil.toByteArray(response.getEntityInputStream());
      response.setEntityInputStream(new ByteArrayInputStream(body));
      return IOUtil.toString(body, "UTF-8");
    }
    catch (IOException e) {
      throw new IllegalStateException("Jersey unexpectedly refused to rewind buffered entity.");
    }
  }

}
