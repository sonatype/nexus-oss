package org.sonatype.nexus.componentviews;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * An in-memory container for all of the views currently available for handling requests.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ViewRegistry
{
  private final ConcurrentHashMap<String, View> activeViews = new ConcurrentHashMap<>();

  private final EventBus eventBus;

  @Inject
  public ViewRegistry(final EventBus eventBus) {this.eventBus = eventBus;}

  public void registerView(View view) {
    checkNotNull(view);

    final View alreadyBoundView = activeViews.putIfAbsent(view.getName(), view);

    checkState(alreadyBoundView == null, "A view is already bound to name %s", view.getName());

    eventBus.post(new ViewRegisteredEvent(this, view));
  }

  public View findViewByName(String viewName) {
    return activeViews.get(viewName);
  }

  /**
   * Unregistraters a view and fires a {@link ViewUnregisteredEvent}. The view immediately stops receiving new
   * requests.
   * Once any outstanding requests are processed, a
   */
  public void unregisterView(View view) {
    checkNotNull(view);

    final View remove = activeViews.remove(view.getName());

    eventBus.post(new ViewUnregisteredEvent(this, view));

    // TODO: Determine when the outgoing view has actually done handling all its events. unregister should return
    // immediately, with this event being fired a bit later, probably by another thread.
    eventBus.post(new ViewRequestsCompletedEvent(this, view));
  }

  @Nullable
  public View getView(String viewName) {
    return activeViews.get(viewName);
  }
}
