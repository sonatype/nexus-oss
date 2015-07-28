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
package org.sonatype.nexus.repository.docker.internal

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.RecipeSupport
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.docker.DockerFacet
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.partial.PartialFetchHandler
import org.sonatype.nexus.repository.search.SearchFacet
import org.sonatype.nexus.repository.security.SecurityHandler
import org.sonatype.nexus.repository.storage.StorageFacetImpl
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Matcher
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.ViewFacet
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler
import org.sonatype.nexus.repository.view.handlers.TimingHandler
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.LiteralMatcher
import org.sonatype.nexus.repository.view.matchers.PrefixMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

import org.eclipse.aether.util.version.GenericVersionScheme
import org.eclipse.aether.version.InvalidVersionSpecificationException
import org.eclipse.aether.version.VersionScheme

import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.POST
import static org.sonatype.nexus.repository.http.HttpMethods.PUT

/**
 * Docker hosted repository recipe.
 *
 * @since 3.0
 */
@Named(DockerHostedRecipe.NAME)
@Singleton
class DockerHostedRecipe
    extends RecipeSupport
{
  public static final String NAME = 'docker-hosted'

  private static final VersionScheme SCHEME = new GenericVersionScheme()

  private static final VERSION_0_10 = SCHEME.parseVersion('0.10')

  @Inject
  Provider<DockerSecurityFacet> securityFacet

  @Inject
  Provider<ConfigurableViewFacet> viewFacet

  @Inject
  Provider<RepositoryReverseProxyFacetImpl> repositoryReverseProxyFacet

  @Inject
  Provider<DockerFacet> dockerFacet

  @Inject
  Provider<StorageFacetImpl> storageFacet

  @Inject
  Provider<SearchFacet> searchFacet

  @Inject
  TimingHandler timingHandler

  @Inject
  SecurityHandler securityHandler

  @Inject
  PartialFetchHandler partialFetchHandler

  @Inject
  ConditionalRequestHandler conditionalRequestHandler

  @Inject
  UnitOfWorkHandler unitOfWorkHandler

  @Inject
  V1Handlers v1Handlers

  @Inject
  DockerHostedRecipe(@Named(HostedType.NAME) final Type type,
                     @Named(DockerFormat.NAME) final Format format)
  {
    super(type, format)
  }

  @Override
  void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(securityFacet.get())
    repository.attach(configure(viewFacet.get()))
    repository.attach(repositoryReverseProxyFacet.get())
    repository.attach(dockerFacet.get())
    repository.attach(storageFacet.get())
    repository.attach(searchFacet.get())
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder()

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new PrefixMatcher('/v1/'),
            new Matcher() {
              @Override
              boolean matches(final Context context) {
                // only allow docker versions >= 0.10
                def dockerVersion = V1Handlers.parseUserAgent(context)[DockerAttributes.P_DOCKER_VERSION]
                try {
                  return dockerVersion && VERSION_0_10.compareTo(SCHEME.parseVersion(dockerVersion)) > 0
                }
                catch (InvalidVersionSpecificationException e) {
                  return true
                }
              }
            }
        ))
        .handler(timingHandler)
        .handler(v1Handlers.v1Exception)
        .handler(v1Handlers.oldDocker)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            LogicMatchers.or(new LiteralMatcher('/v1/users'), new LiteralMatcher('/v1/users/'))
        ))
        .handler(timingHandler)
        .handler(conditionalRequestHandler)
        .handler(v1Handlers.getUsers)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(POST),
            LogicMatchers.or(new LiteralMatcher('/v1/users'), new LiteralMatcher('/v1/users/'))
        ))
        .handler(timingHandler)
        .handler(v1Handlers.createUser)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            new TokenMatcher('/v1/users/{username}')
        ))
        .handler(timingHandler)
        .handler(v1Handlers.updateUser)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            new LiteralMatcher('/v1/search')
        ))
        .handler(timingHandler)
        .handler(v1Handlers.search)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/images'),
                new TokenMatcher('/v1/repositories/{repository}/images')
            )
        ))
        .handler(timingHandler)
        .handler(conditionalRequestHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.getImagesIndex)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/images'),
                new TokenMatcher('/v1/repositories/{repository}/images')
            )
        ))
        .handler(timingHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.putImagesIndex.rcurry(true))
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/'),
                new TokenMatcher('/v1/repositories/{repository}/')
            )
        ))
        .handler(timingHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.putImagesIndex.rcurry(false))
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/tags'),
                new TokenMatcher('/v1/repositories/{repository}/tags')
            )
        ))
        .handler(timingHandler)
        .handler(conditionalRequestHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.getTags)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/tags/{tag}'),
                new TokenMatcher('/v1/repositories/{repository}/tags/{tag}')
            )
        ))
        .handler(timingHandler)
        .handler(conditionalRequestHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.getTag)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/tags/{tag}'),
                new TokenMatcher('/v1/repositories/{repository}/tags/{tag}')
            )
        ))
        .handler(timingHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.putTag)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            new TokenMatcher('/v1/images/{layerId}/json'),
        ))
        .handler(timingHandler)
        .handler(conditionalRequestHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.getLayerMetadata)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            new TokenMatcher('/v1/images/{layerId}/json'),
        ))
        .handler(timingHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.putLayerMetadata)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            new TokenMatcher('/v1/images/{layerId}/layer'),
        ))
        .handler(timingHandler)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.getLayerContent)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            new TokenMatcher('/v1/images/{layerId}/layer'),
        ))
        .handler(timingHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.putLayer)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            new TokenMatcher('/v1/images/{layerId}/checksum'),
        ))
        .handler(timingHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.putLayerChecksum)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            new TokenMatcher('/v1/images/{layerId}/ancestry'),
        ))
        .handler(timingHandler)
        .handler(conditionalRequestHandler)
        .handler(v1Handlers.v1Exception)
        .handler(unitOfWorkHandler)
        .handler(v1Handlers.getLayerAncestry)
        .create())

    builder.defaultHandlers(HttpHandlers.badRequest())

    facet.configure(builder.create())

    return facet
  }

}
