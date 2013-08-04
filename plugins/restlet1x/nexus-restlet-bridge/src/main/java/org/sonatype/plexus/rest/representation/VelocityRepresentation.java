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

package org.sonatype.plexus.rest.representation;

import java.util.Collections;
import java.util.Map;

import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.sisu.velocity.Velocity;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.ext.velocity.TemplateRepresentation;

/**
 * Velocity representation that enhances Restlet's {@link TemplateRepresentation}, that Velocity instance is reused.
 * Problem with Restlet {@link TemplateRepresentation} is that it creates a new instance of Velocity per creation of
 * {@link TemplateRepresentation}. This class remedies that, by overriding how {@link VelocityEngine} is obtained, as
 * Plexus Application will stuff a VelocityEngine provider into context, hence, a singleton instance of Velocity will
 * be
 * reused. See SISU {@link Velocity} that is used under the hub.
 *
 * @author cstamas
 */
public class VelocityRepresentation
    extends TemplateRepresentation
{
  /**
   * The engine instance to be used to render this {@link VelocityRepresentation}.
   */
  private final VelocityEngine velocityEngine;

  /**
   * Constructor when Template is already assembled.
   *
   * @since 1.21
   */
  public VelocityRepresentation(Context context, Template template, Map<String, Object> dataModel,
                                MediaType mediaType)
  {
    super(template, dataModel, mediaType);
    this.velocityEngine = getEngine(context);
  }

  /**
   * Constructor when template to use comes from some other classloader than the one where this class is.
   *
   * @since 1.23
   */
  public VelocityRepresentation(Context context, String templateName, ClassLoader cl, Map<String, Object> dataModel,
                                MediaType mediaType)
  {
    this(context, getTemplate(context, templateName, cl), dataModel, mediaType);
  }

  /**
   * Constructor when template is on core classpath (will be loaded using VelocityEngine). This constructor accepts
   * template name (binary name), and will use current classloader to locate the template. This constructor is not
   * quite usable in apps that maintains multiple classloaders, as there is no control exposed over classloader to be
   * used to load up the template resource.
   *
   * @deprecated Use the constructor that accepts {@link Template}, as it gives you total control how to obtain the
   *             template (ie. to use custom classloader or so).
   */
  public VelocityRepresentation(Context context, String templateName, Map<String, Object> dataModel,
                                MediaType mediaType)
  {
    this(context, getTemplate(context, templateName, VelocityRepresentation.class.getClassLoader()), dataModel,
        mediaType);
  }

  /**
   * Nonsense constructor... Velocity template without data model? This constructor is not quite usable in apps that
   * maintains multiple classloaders, as there is no control exposed over classloader to be used to load up the
   * template resource.
   *
   * @deprecated Use other constructors, as this one is a bit nonsense.
   */
  @Deprecated
  public VelocityRepresentation(Context context, String templateName, MediaType mediaType) {
    this(context, getTemplate(context, templateName, VelocityRepresentation.class.getClassLoader()),
        Collections.<String, Object>emptyMap(), mediaType);
  }

  // ==

  /**
   * We return our own managed velocity engine instance, to avoid Restlet create one.
   */
  @Override
  public VelocityEngine getEngine() {
    return velocityEngine;
  }

  // ==

  /**
   * Helper method to obtain {@link Template} instances, with explicit control what {@link ClassLoader} needs to be
   * used to locate it.
   *
   * @return the {@link Template} instance
   */
  public static Template getTemplate(final Context context, final String templateName, final ClassLoader cl) {
    // NOTE: Velocity's ClasspathResourceLoader goes for TCCL 1st, then would fallback to "system"
    // (in this case the classloader where Velocity is loaded) classloader
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(cl);
    try {
      return getEngine(context).getTemplate(templateName);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Cannot get the template with name " + String.valueOf(templateName),
          e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }

  /**
   * {@link PlexusRestletApplicationBridge} stuffs the SISU {@link Velocity} into context, and we use the shared
   * instance from it, instead to recreate it over and over again.
   */
  private static VelocityEngine getEngine(final Context context) {
    return ((Velocity) context.getAttributes().get(Velocity.class.getName())).getEngine();
  }
}
