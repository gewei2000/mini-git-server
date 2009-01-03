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

import com.google.gerrit.client.Link;
import com.google.gerrit.client.reviewdb.Patch;
import com.google.gerrit.client.ui.FancyFlexTable;
import com.google.gerrit.client.ui.PatchLink;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;

import java.util.List;

public class PatchTable extends FancyFlexTable<Patch> {
  private static final int C_TYPE = 1;
  private static final int C_NAME = 2;
  private static final int C_DELTA = 3;
  private static final int C_COMMENTS = 4;
  private static final int C_DIFF = 5;
  private static final int N_DIFF = 2;

  public PatchTable() {
    table.setText(0, C_TYPE, "");
    table.setText(0, C_NAME, Util.C.patchTableColumnName());
    table.setText(0, C_DELTA, Util.C.patchTableColumnDelta());
    table.setText(0, C_COMMENTS, Util.C.patchTableColumnComments());
    table.setText(0, C_DIFF, Util.C.patchTableColumnDiff());

    final FlexCellFormatter fmt = table.getFlexCellFormatter();
    fmt.addStyleName(0, C_TYPE, S_ICON_HEADER);
    fmt.addStyleName(0, C_NAME, S_DATA_HEADER);
    fmt.addStyleName(0, C_DELTA, S_DATA_HEADER);
    fmt.addStyleName(0, C_COMMENTS, S_DATA_HEADER);
    fmt.addStyleName(0, C_DIFF, S_DATA_HEADER);
    fmt.setColSpan(0, C_DIFF, N_DIFF);
  }

  @Override
  protected Object getRowItemKey(final Patch item) {
    return item.getKey();
  }

  @Override
  protected void onOpenItem(final Patch item) {
    History.newItem(Link.toPatchSideBySide(item.getKey()));
  }

  @Override
  protected void applyDataRowStyle(final int row) {
    super.applyDataRowStyle(row);
    final CellFormatter fmt = table.getCellFormatter();
    fmt.addStyleName(row, C_TYPE, "ChangeTypeCell");

    fmt.addStyleName(row, C_NAME, S_DATA_CELL);
    fmt.addStyleName(row, C_NAME, "FilePathCell");

    fmt.addStyleName(row, C_DELTA, S_DATA_CELL);

    fmt.addStyleName(row, C_COMMENTS, S_DATA_CELL);
    fmt.addStyleName(row, C_COMMENTS, "CommentCell");

    fmt.addStyleName(row, C_DIFF + 0, S_DATA_CELL);
    fmt.addStyleName(row, C_DIFF + 0, "DiffLinkCell");

    fmt.addStyleName(row, C_DIFF + 1, S_DATA_CELL);
    fmt.addStyleName(row, C_DIFF + 1, "DiffLinkCell");
  }

  public void display(final List<Patch> list) {
    final int sz = list != null ? list.size() : 0;
    int dataRows = table.getRowCount() - 1;
    while (sz < dataRows) {
      table.removeRow(dataRows);
      dataRows--;
    }

    for (int i = 0; i < sz; i++) {
      if (dataRows <= i) {
        table.insertRow(++dataRows);
        applyDataRowStyle(i + 1);
      }
      populate(i + 1, list.get(i));
    }
  }

  private void populate(final int row, final Patch patch) {
    table.setWidget(row, C_ARROW, null);
    table.setText(row, C_TYPE, "" + patch.getChangeType().getCode());

    Widget nameLink;
    if (patch.getPatchType() == Patch.PatchType.UNIFIED) {
      nameLink = new PatchLink.SideBySide(patch.getKey().get(), patch.getKey());
    } else {
      nameLink = new PatchLink.Unified(patch.getKey().get(), patch.getKey());
    }
    if (patch.getSourceFileName() != null) {
      final String secondLine;
      if (patch.getChangeType() == Patch.ChangeType.RENAMED) {
        secondLine = Util.M.renamedFrom(patch.getSourceFileName());

      } else if (patch.getChangeType() == Patch.ChangeType.COPIED) {
        secondLine = Util.M.copiedFrom(patch.getSourceFileName());

      } else {
        secondLine = Util.M.otherFrom(patch.getSourceFileName());
      }

      final InlineLabel secondLineLabel = new InlineLabel(secondLine);
      secondLineLabel.setStyleName("SourceFilePath");

      final FlowPanel fp = new FlowPanel();
      fp.add(nameLink);
      fp.add(secondLineLabel);
      nameLink = fp;
    }
    table.setWidget(row, C_NAME, nameLink);

    table.clearCell(row, C_DELTA);

    final int cnt = patch.getCommentCount();
    if (cnt == 0) {
      table.clearCell(row, C_COMMENTS);
    } else {
      table.setText(row, C_COMMENTS, Util.M.patchTableComments(cnt));
    }

    if (patch.getPatchType() == Patch.PatchType.UNIFIED) {
      table.setWidget(row, C_DIFF + 0, new PatchLink.SideBySide(Util.C
          .patchTableDiffSideBySide(), patch.getKey()));
    } else {
      table.clearCell(row, C_DIFF + 0);
    }
    table.setWidget(row, C_DIFF + 1, new PatchLink.Unified(Util.C
        .patchTableDiffUnified(), patch.getKey()));

    setRowItem(row, patch);
  }
}