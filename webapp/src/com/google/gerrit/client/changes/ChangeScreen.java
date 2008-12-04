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

package com.google.gerrit.client.changes;

import com.google.gerrit.client.data.ChangeDetail;
import com.google.gerrit.client.data.ChangeInfo;
import com.google.gerrit.client.reviewdb.Change;
import com.google.gerrit.client.rpc.ScreenLoadCallback;
import com.google.gerrit.client.ui.Screen;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class ChangeScreen extends Screen {
  private Change.Id changeId;
  private ChangeInfo changeInfo;

  private ChangeInfoBlock infoBlock;
  private DisclosurePanel descriptionPanel;
  private Label description;

  private DisclosurePanel dependenciesPanel;
  private ChangeTable dependencies;
  private ChangeTable.Section dependsOn;
  private ChangeTable.Section neededBy;

  private DisclosurePanel approvalsPanel;
  private ApprovalTable approvals;

  public ChangeScreen(final Change.Id toShow) {
    changeId = toShow;
  }

  public ChangeScreen(final ChangeInfo c) {
    this(c.getId());
    changeInfo = c;
  }

  @Override
  public Object getScreenCacheToken() {
    return getClass();
  }

  @Override
  public Screen recycleThis(final Screen newScreen) {
    final ChangeScreen s = (ChangeScreen) newScreen;
    changeId = s.changeId;
    changeInfo = s.changeInfo;
    return this;
  }

  @Override
  public void onLoad() {
    if (descriptionPanel == null) {
      addStyleName("gerrit-ChangeScreen");

      infoBlock = new ChangeInfoBlock();
      add(infoBlock);

      description = new Label();
      description.setStyleName("gerrit-ChangeScreen-Description");

      descriptionPanel = new DisclosurePanel(Util.C.changeScreenDescription());
      descriptionPanel.setContent(description);
      descriptionPanel.setWidth("100%");
      add(descriptionPanel);

      dependencies = new ChangeTable();
      dependsOn = new ChangeTable.Section(Util.C.changeScreenDependsOn());
      neededBy = new ChangeTable.Section(Util.C.changeScreenNeededBy());
      dependencies.addSection(dependsOn);
      dependencies.addSection(neededBy);

      dependenciesPanel =
          new DisclosurePanel(Util.C.changeScreenDependencies());
      dependenciesPanel.setContent(dependencies);
      dependenciesPanel.setWidth("95%");
      add(dependenciesPanel);

      approvals = new ApprovalTable();
      approvalsPanel = new DisclosurePanel(Util.C.changeScreenApprovals());
      approvalsPanel.setContent(wrap(approvals));
      dependenciesPanel.setWidth("95%");
      add(approvalsPanel);
    }

    displayTitle(changeInfo != null ? changeInfo.getSubject() : null);
    super.onLoad();

    Util.DETAIL_SVC.changeDetail(changeId,
        new ScreenLoadCallback<ChangeDetail>() {
          public void onSuccess(final ChangeDetail r) {
            // TODO Actually we want to cancel the RPC if detached.
            if (isAttached()) {
              display(r);
            }
          }
        });
  }

  private void displayTitle(final String subject) {
    final StringBuffer titleBuf = new StringBuffer();
    if (LocaleInfo.getCurrentLocale().isRTL()) {
      if (subject != null) {
        titleBuf.append(subject);
        titleBuf.append(" :");
      }
      titleBuf.append(Util.M.changeScreenTitleId(changeId.get()));
    } else {
      titleBuf.append(Util.M.changeScreenTitleId(changeId.get()));
      if (subject != null) {
        titleBuf.append(": ");
        titleBuf.append(subject);
      }
    }
    setTitleText(titleBuf.toString());
  }

  private void display(final ChangeDetail detail) {
    if (changeInfo == null) {
      // We couldn't set the title correctly when we loaded the page
      // into the browser, update it now that we have the full detail.
      //
      displayTitle(detail.getChange().getSubject());
    }
    infoBlock.display(detail);
    description.setText(detail.getDescription());
    approvals.display(detail.getApprovals());

    descriptionPanel.setOpen(true);
    approvalsPanel.setOpen(true);
  }

  private static FlowPanel wrap(final Widget w) {
    final FlowPanel p = new FlowPanel();
    p.add(w);
    return p;
  }
}
