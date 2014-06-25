/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectFormPostMethod
import com.softwarementors.extjs.djn.servlet.ssm.WebContextManager
import org.apache.commons.fileupload.FileItem
import org.apache.commons.lang.StringUtils
import org.apache.maven.model.Model
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.configuration.validation.InvalidConfigurationException
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.guice.Validate
import org.sonatype.nexus.proxy.access.AccessManager
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest
import org.sonatype.nexus.proxy.maven.MavenHostedRepository
import org.sonatype.nexus.proxy.maven.MavenRepository
import org.sonatype.nexus.proxy.maven.RepositoryPolicy
import org.sonatype.nexus.proxy.maven.gav.Gav
import org.sonatype.nexus.proxy.maven.gav.GavCalculator
import org.sonatype.nexus.proxy.registry.RepositoryRegistry
import org.sonatype.nexus.rest.RepositoryURLBuilder
import org.sonatype.nexus.web.RemoteIPFinder

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.servlet.http.HttpServletRequest
import javax.validation.constraints.NotNull
import java.security.cert.X509Certificate

/**
 * Maven {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'maven_Maven')
class MavenComponent
extends DirectComponentSupport
{

  @Inject
  RepositoryURLBuilder repositoryURLBuilder

  @Inject
  @Named("protected") RepositoryRegistry protectedRepositoryRegistry

  @Inject
  @Named("maven2")
  GavCalculator gavCalculator

  /**
   * Uploads artifacts.
   */
  @DirectFormPostMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:artifacts:create')
  @Validate
  void uploadArtifacts(final @NotNull(message = '[params] may not be null') Map<String, String> params,
                       final @NotNull(message = '[files] may not be null') Map<String, FileItem> files)
  {
    if (files.size() == 0) {
      def validations = new ValidationResponse()
      validations.addValidationError(new ValidationMessage('*', 'At least one file should be uploaded'))
      throw new InvalidConfigurationException(validations)
    }

    UploadContext uploadContext = createUploadContext(params, files)
    RequestContext requestContext = createRequestContext(WebContextManager.get().request)
    MavenHostedRepository mavenRepository = protectedRepositoryRegistry.getRepositoryWithFacet(
        uploadContext.repositoryId, MavenHostedRepository.class
    )
    validateRepositoryPolicy(mavenRepository, uploadContext.version)
    boolean pomStored = uploadContext.pomAvailable
    files.each { name, file ->
      ArtifactStoreRequest request = createRequest(file, mavenRepository, uploadContext, requestContext, params)
      if (isPom(file, params)) {
        mavenRepository.artifactStoreHelper.storeArtifactPom(request, file.inputStream, null)
      }
      else {
        if (pomStored) {
          mavenRepository.artifactStoreHelper.storeArtifact(request, file.inputStream, null)
        }
        else {
          mavenRepository.artifactStoreHelper.storeArtifactWithGeneratedPom(request, uploadContext.packaging, file.inputStream, null)
          pomStored = true
        }
      }
    }
  }

  private static ArtifactStoreRequest createRequest(final FileItem file,
                                                    final MavenRepository mavenRepository,
                                                    final UploadContext uploadContext,
                                                    final RequestContext requestContext,
                                                    final Map<String, String> params)
  {
    String extension = null, classifier = null
    if (!isPom(file, params)) {
      extension = params["${file.fieldName}.extension"]
      // if extension is not given, fall-back to packaging and apply mapper
      if (StringUtils.isBlank(extension)) {
        extension = mavenRepository.artifactPackagingMapper.getExtensionForPackaging(uploadContext.packaging)
      }
      classifier = params["${file.fieldName}.classifier"]
      // clean up the classifier
      if (StringUtils.isBlank(classifier)) {
        classifier = null
      }
    }

    Gav gav = new Gav(uploadContext.groupId, uploadContext.artifactId, uploadContext.version, classifier, extension, null, null, null, false, null, false, null)
    ArtifactStoreRequest result = new ArtifactStoreRequest(mavenRepository, gav, true, false)
    result.requestContext.put(AccessManager.REQUEST_USER, requestContext.userId)
    result.requestContext.put(AccessManager.REQUEST_AGENT, requestContext.userAgent)
    result.requestContext.put(AccessManager.REQUEST_REMOTE_ADDRESS, requestContext.ipAddress)
    result.requestContext.put(AccessManager.REQUEST_CONFIDENTIAL, requestContext.secure)
    if (requestContext.secure) {
      result.requestContext.put(AccessManager.REQUEST_CERTIFICATES, requestContext.certs)
    }
    result.requestUrl = requestContext.url
    return result
  }

  private static UploadContext createUploadContext(final Map<String, String> params,
                                                   final Map<String, FileItem> files)
  {
    UploadContext context = new UploadContext(
        pomAvailable: false,
        groupId: params['groupId'],
        artifactId: params['artifactId'],
        version: params['version'],
        packaging: params['packaging'],
        repositoryId: params['repositoryId']
    )
    Collection<FileItem> poms = files.findResults { name, FileItem file ->
      return isPom(file, params) ? file : null
    }
    if (poms.size() > 1) {
      def validations = new ValidationResponse()
      validations.addValidationError(new ValidationMessage('*', 'Only one POM file can be uploaded'))
      throw new InvalidConfigurationException(validations)
    }
    FileItem pom = poms[0]
    if (pom) {
      MavenXpp3Reader reader = new MavenXpp3Reader()
      pom.inputStream.withStream { InputStream is ->
        Model model = reader.read(is)
        context.pomAvailable = true
        context.groupId = model.groupId
        context.artifactId = model.artifactId
        context.version = model.version
        context.packaging = model.packaging
      }
    }

    def validations = new ValidationResponse()
    if (!context.groupId) {
      validations.addValidationError(new ValidationMessage('groupId', 'May not be null'))
    }
    if (!context.artifactId) {
      validations.addValidationError(new ValidationMessage('artifactId', 'May not be null'))
    }
    if (!context.version) {
      validations.addValidationError(new ValidationMessage('version', 'May not be null'))
    }
    if (!context.repositoryId) {
      validations.addValidationError(new ValidationMessage('repositoryId', 'May not be null'))
    }
    if (!validations.valid) {
      throw new InvalidConfigurationException(validations)
    }
    return context
  }

  private static RequestContext createRequestContext(final HttpServletRequest request) {
    RequestContext context = new RequestContext(
        userId: SecurityUtils.subject?.principal as String,
        userAgent: request.getHeader('user-agent'),
        ipAddress: RemoteIPFinder.findIP(request),
        secure: request.secure
    )
    if (context.secure) {
      Object certArray = request.getAttribute('javax.servlet.request.X509Certificate')
      if (certArray) {
        List<X509Certificate> certs = ((X509Certificate[]) certArray) as List
        if (!certs.empty) {
          context.certs = certs
        }
      }
    }
    if (request.queryString) {
      context.url = "${request.requestURL}?${request.queryString}"
    }
    else {
      context.url = request.requestURL
    }
    return context
  }

  static void validateRepositoryPolicy(final MavenRepository mavenRepository, final String version) {
    def validations = new ValidationResponse()
    if (Gav.isSnapshot(version)) {
      if (RepositoryPolicy.RELEASE == mavenRepository.repositoryPolicy) {
        validations.addValidationError(new ValidationMessage(
            'version', 'Snapshot artifacts are not allowed by repository policy'
        ))
      }
    }
    else {
      if (RepositoryPolicy.SNAPSHOT == mavenRepository.repositoryPolicy) {
        validations.addValidationError(new ValidationMessage(
            'version', 'Release artifacts are not allowed by repository policy'
        ))
      }
    }
    if (!validations.valid) {
      throw new InvalidConfigurationException(validations)
    }
  }

  private static isPom(final FileItem file, final Map<String, String> params) {
    def extension = params["${file.fieldName}.extension"]
    def classifier = params["${file.fieldName}.classifier"]
    return extension == 'pom' && StringUtils.isEmpty(classifier)
  }

  static class UploadContext
  {
    boolean pomAvailable
    String groupId
    String artifactId
    String version
    String packaging
    String repositoryId
  }

  static class RequestContext
  {
    String userId
    String userAgent
    String ipAddress
    Boolean secure
    List<X509Certificate> certs
    String url
  }

}
