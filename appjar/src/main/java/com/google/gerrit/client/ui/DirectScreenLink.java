// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.client.ui;

import com.google.gerrit.client.Gerrit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Hyperlink;

/**
 * Link to a Screen which can carry richer payload.
 * <p>
 * A standard Hyperlink widget which updates the current history token when
 * activated, but subclasses are able to create the Screen instance themselves,
 * passing additional data into the Screen's constructor. This may permit the
 * screen to show some limited information early, before RPCs required to fully
 * populate it are even started.
 */
public abstract class DirectScreenLink extends Hyperlink {
  /**
   * Creates a link with its text and target history token specified.
   * 
   * @param text the hyperlink's text
   * @param historyToken the history token to which it will link
   */
  protected DirectScreenLink(final String text, final String historyToken) {
    super(text, historyToken);
  }

  @Override
  public void onBrowserEvent(final Event event) {
    if (DOM.eventGetType(event) == Event.ONCLICK) {
      History.newItem(getTargetHistoryToken(), false);
      Gerrit.display(createScreen());
      DOM.eventPreventDefault(event);
    }
  }

  /** Create the screen this link wants to display. */
  protected abstract Screen createScreen();
}