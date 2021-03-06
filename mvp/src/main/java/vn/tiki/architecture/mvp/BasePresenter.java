package vn.tiki.architecture.mvp;

import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Giang Nguyen on 8/25/17.
 */

public class BasePresenter<V extends Mvp.View> implements Mvp.Presenter<V> {

  @NonNull private Queue<ViewAction<V>> pendingViewActions = new LinkedList<>();
  @Nullable private V view;
  private boolean attached;
  private boolean detached;

  /**
   * Return view. NOTE: view is nullable.
   *
   * @return View
   */
  @Nullable public V getView() {
    return view;
  }

  /**
   * Return view if view != null or throw exception otherwise.
   *
   * @return View
   */
  @NonNull protected V getViewOrThrow() {
    final V view = getView();
    if (view == null) {
      throw new IllegalStateException("view not attached");
    }
    return view;
  }

  /**
   * Send action to view when view is attached.
   *
   * @param action to run.
   */
  @MainThread protected void sendToView(ViewAction<V> action) {
    if (view == null) {
      pendingViewActions.add(action);
    } else {
      action.call(view);
    }
  }

  @CallSuper
  @Override public void attach(V view) {
    this.view = view;
    if (pendingViewActions.isEmpty()) {
      return;
    }

    do {
      final ViewAction<V> action = pendingViewActions.remove();
      action.call(view);
    } while (pendingViewActions.size() > 0);
    attached = true;
    detached = false;
  }

  @CallSuper
  @Override public void detach() {
    this.view = null;
    attached = false;
    detached = true;
  }

  @Override public void destroy() {

  }

  public boolean isAttached() {
    return attached;
  }

  public boolean isDetached() {
    return detached;
  }
}
