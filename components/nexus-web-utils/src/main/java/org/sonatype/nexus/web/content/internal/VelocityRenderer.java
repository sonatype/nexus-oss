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

package org.sonatype.nexus.web.content.internal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsHiddenAttribute;
import org.sonatype.nexus.web.content.Renderer;
import org.sonatype.sisu.velocity.Velocity;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Renderer} using SISU {@link Velocity} component.
 * 
 * @since 2.7.0
 * @see Velocity
 */
@Singleton
@Named
public class VelocityRenderer
    extends AbstractLoggingComponent
    implements Renderer
{
  private final Velocity velocity;
  private final String applicationVersion;

  @Inject
  public VelocityRenderer(final Velocity velocity, final ApplicationStatusSource applicationStatusSource) {
    this.velocity = checkNotNull(velocity);
    this.applicationVersion = checkNotNull(applicationStatusSource).getSystemStatus().getVersion();
  }

  @Override
  public void renderCollection(final HttpServletRequest request, final HttpServletResponse response,
      final StorageCollectionItem coll, final Collection<StorageItem> children) throws IOException
  {
    final Set<String> uniqueNames = Sets.newHashSetWithExpectedSize(children.size());
    final List<CollectionEntry> entries = Lists.newArrayListWithCapacity(children.size());
    for (StorageItem child : children) {
      if (child.isVirtual() || !child.getRepositoryItemUid().getBooleanAttributeValue(IsHiddenAttribute.class)) {
        if (!uniqueNames.contains(child.getName())) {
          final boolean isCollection = child instanceof StorageCollectionItem;
          final String name = isCollection ? child.getName() + "/" : child.getName();
          final CollectionEntry entry = new CollectionEntry(name, isCollection, coll.getResourceStoreRequest()
              .getRequestUrl() + name, new Date(child.getModified()), StorageFileItem.class.isAssignableFrom(child
              .getClass()) ? ((StorageFileItem) child).getLength() : -1, "");
          entries.add(entry);
          uniqueNames.add(child.getName());
        }
      }
    }

    Collections.sort(entries, new CollectionEntryComparator());

    final Map<String, Object> dataModel = createBaseModel(coll.getResourceStoreRequest());
    dataModel.put("requestPath", coll.getPath());
    dataModel.put("listItems", entries);
    render(getTemplate("repositoryContentHtml.vm"), dataModel, response.getOutputStream());
  }

  @Override
  public void renderErrorPage(final HttpServletRequest request, final HttpServletResponse response,
      final ResourceStoreRequest rsr, final Exception exception) throws IOException
  {
    final Map<String, Object> dataModel = createBaseModel(rsr);
    dataModel.put("statusCode", response.getStatus());
    dataModel.put("statusName", exception.getClass().getSimpleName());
    dataModel.put("errorDescription", StringEscapeUtils.escapeHtml(Strings.nullToEmpty(exception.getMessage())));
    if (null != exception) {
      dataModel.put("errorStackTrace", StringEscapeUtils.escapeHtml(ExceptionUtils.getStackTrace(exception)));
    }
    render(getTemplate("errorPageContentHtml.vm"), dataModel, response.getOutputStream());
  }

  @Override
  public void renderRequestDescription(final HttpServletRequest request, final HttpServletResponse response,
      final ResourceStoreRequest resourceStoreRequest, final StorageItem item, final Exception exception)
      throws IOException
  {
    final Map<String, Object> dataModel = createBaseModel(resourceStoreRequest);
    dataModel.put("req", resourceStoreRequest);
    dataModel.put("item", item);
    dataModel.put("exception", exception);
    render(getTemplate("requestDescriptionHtml.vm"), dataModel, response.getOutputStream());
  }

  // ==

  protected Map<String, Object> createBaseModel(final ResourceStoreRequest resourceStoreRequest) {
    final Map<String, Object> dataModel = Maps.newHashMap();
    String nexusRoot = resourceStoreRequest.getRequestAppRootUrl();
    if (nexusRoot.endsWith("/")) {
      nexusRoot = nexusRoot.substring(0, nexusRoot.length() - 1);
    }
    dataModel.put("nexusRoot", nexusRoot);
    dataModel.put("nexusVersion", applicationVersion);
    return dataModel;
  }

  protected void render(final Template template, final Map<String, Object> dataModel, final OutputStream outputStream)
      throws IOException
  {
    final Context context = new VelocityContext(dataModel);
    try {
      final Writer tmplWriter;
      // Load the template
      if (template.getEncoding() == null) {
        tmplWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
      }
      else {
        tmplWriter = new BufferedWriter(new OutputStreamWriter(outputStream, template.getEncoding()));
      }

      // Process the template
      template.merge(context, tmplWriter);
      tmplWriter.flush();
    }
    catch (IOException e) {
      // NEXUS-3442
      // IOEx should be propagated as is
      throw e;
    }
    catch (Exception e) {
      // All other (Velocity exceptions are RuntimeExcptions!) to be wrapped, but preserve cause too
      throw new IOException("Template processing error: " + e.getMessage(), e);
    }
  }

  protected Template getTemplate(final String templateName) {
    // NOTE: Velocity's ClasspathResourceLoader goes for TCCL 1st, then would fallback to "system"
    // (in this case the classloader where Velocity is loaded) classloader
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      if (templateName.startsWith("/")) {
        return velocity.getEngine().getTemplate(templateName);
      }
      else {
        return velocity.getEngine().getTemplate(
            getClass().getPackage().getName().replace(".", "/") + "/" + templateName);
      }
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Cannot get the template with name " + String.valueOf(templateName), e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }

  // =

  public static class CollectionEntryComparator
      implements Comparator<CollectionEntry>
  {
    @Override
    public int compare(final CollectionEntry o1, final CollectionEntry o2) {
      if (o1.isCollection()) {
        if (o2.isCollection()) {
          // 2 directories, do a path compare
          return o1.getName().compareTo(o2.getName());
        }
        else {
          // first item is a dir, second is a file, dirs always win
          return -1;
        }
      }
      else if (o2.isCollection()) {
        // first item is a file, second is a dir, dirs always win
        return 1;
      }
      else {
        // 2 files, do a path compare
        return o1.getName().compareTo(o2.getName());
      }
    }
  }

  public static class CollectionEntry
  {
    private final String name;

    private final boolean collection;

    private final String resourceUri;

    private final Date lastModified;

    private final long size;

    private final String description;

    public CollectionEntry(final String name, final boolean collection, final String resourceUri,
        final Date lastModified, final long size, final String description)
    {
      this.name = checkNotNull(name);
      this.collection = collection;
      this.resourceUri = checkNotNull(resourceUri);
      this.lastModified = checkNotNull(lastModified);
      this.size = size;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public boolean isCollection() {
      return collection;
    }

    public String getResourceUri() {
      return resourceUri;
    }

    public Date getLastModified() {
      return lastModified;
    }

    public long getSize() {
      return size;
    }

    public String getDescription() {
      return description;
    }
  }
}
