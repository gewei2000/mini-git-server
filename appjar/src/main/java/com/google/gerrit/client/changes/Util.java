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

import com.google.gerrit.client.reviewdb.Change;
import com.google.gwt.core.client.GWT;
import com.google.gwtjsonrpc.client.JsonUtil;

public class Util {
  public static final ChangeConstants C = GWT.create(ChangeConstants.class);
  public static final ChangeMessages M = GWT.create(ChangeMessages.class);

  public static final ChangeDetailService DETAIL_SVC;
  public static final ChangeListService LIST_SVC;

  static {
    DETAIL_SVC = GWT.create(ChangeDetailService.class);
    JsonUtil.bind(DETAIL_SVC, "rpc/ChangeDetailService");

    LIST_SVC = GWT.create(ChangeListService.class);
    JsonUtil.bind(LIST_SVC, "rpc/ChangeListService");
  }

  public static String toLongString(final Change.Status status) {
    if (status == null) {
      return "";
    } else if (status == Change.Status.NEW) {
      return C.statusLongNew();
    } else if (status == Change.Status.MERGED) {
      return C.statusLongMerged();
    } else if (status == Change.Status.ABANDONED) {
      return C.statusLongAbandoned();
    } else {
      return status.name();
    }
  }
}