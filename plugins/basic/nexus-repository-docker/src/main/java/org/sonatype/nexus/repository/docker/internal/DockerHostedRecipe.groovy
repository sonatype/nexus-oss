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
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.security.SecurityHandler
import org.sonatype.nexus.repository.storage.StorageFacetImpl
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Handler
import org.sonatype.nexus.repository.view.Response
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.ViewFacet
import org.sonatype.nexus.repository.view.handlers.TimingHandler
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.LiteralMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

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

  @Inject
  Provider<DockerSecurityFacet> securityFacet

  @Inject
  Provider<ConfigurableViewFacet> viewFacet

  @Inject
  Provider<ConnectorFacetImpl> connectorFacet

  @Inject
  Provider<DockerFacet> dockerFacet

  @Inject
  Provider<StorageFacetImpl> storageFacet

  @Inject
  TimingHandler timingHandler

  @Inject
  SecurityHandler securityHandler

  @Inject
  UnitOfWorkHandler unitOfWorkHandler

  @Inject
  V1ExceptionHandler v1ExceptionHandler

  @Inject
  V1Users v1Users

  @Inject
  V1Index v1Index

  @Inject
  V1Tags v1Tags

  @Inject
  V1Images v1Images

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
    repository.attach(connectorFacet.get())
    repository.attach(dockerFacet.get())
    repository.attach(storageFacet.get())
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder()

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            LogicMatchers.or(new LiteralMatcher('/v1/users'), new LiteralMatcher('/v1/users/'))
        ))
        .handler(handler(v1Users.getUsers))
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(POST),
            LogicMatchers.or(new LiteralMatcher('/v1/users'), new LiteralMatcher('/v1/users/'))
        ))
        .handler(handler(v1Users.createUser))
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            new TokenMatcher('/v1/users/{username}')
        ))
        .handler(handler(v1Users.updateUser))
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/images'),
                new TokenMatcher('/v1/repositories/{repository}/images')
            )
        ))
        .handler(v1Index.addRepository.rcurry(true))
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/'),
                new TokenMatcher('/v1/repositories/{repository}/')
            )
        ))
        .handler(v1Index.addRepository.rcurry(false))
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            LogicMatchers.or(
                new TokenMatcher('/v1/repositories/{namespace}/{repository}/tags/{tag}'),
                new TokenMatcher('/v1/repositories/{repository}/tags/{tag}')
            )
        ))
        .handler(v1ExceptionHandler)
        .handler(v1Tags.addTag)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            new TokenMatcher('/v1/images/{image}/json'),
        ))
        .handler(v1Images.getMetadata)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            new TokenMatcher('/v1/images/{image}/json'),
        ))
        .handler(v1ExceptionHandler)
        .handler(unitOfWorkHandler)
        .handler(v1Images.putMetadata)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            new TokenMatcher('/v1/images/{image}/layer'),
        ))
        .handler(v1Images.getLayer)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            new TokenMatcher('/v1/images/{image}/layer'),
        ))
        .handler(v1ExceptionHandler)
        .handler(unitOfWorkHandler)
        .handler(v1Images.putLayer)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(PUT),
            new TokenMatcher('/v1/images/{image}/checksum'),
        ))
        .handler(v1ExceptionHandler)
        .handler(unitOfWorkHandler)
        .handler(v1Images.putChecksum)
        .create())

    builder.route(new Route.Builder().matcher(
        LogicMatchers.and(
            new ActionMatcher(GET),
            new TokenMatcher('/v1/images/{image}/ancestry'),
        ))
        .handler(v1Images.getAncestry)
        .create())

    builder.defaultHandlers(HttpHandlers.badRequest())

    facet.configure(builder.create())

    return facet
  }

  private Handler handler(final Closure action) {
    return new Handler() {
      @Override
      Response handle(@Nonnull final Context context) {
        return action.call(context)
      }
    }
  }

}
