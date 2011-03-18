// Copyright (C) 2009 The Android Open Source Project
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

package com.google.gerrit.httpd;

import com.google.gerrit.common.data.GerritConfig;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.ssh.SshInfo;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import org.eclipse.jgit.lib.Config;

import javax.servlet.ServletContext;
import java.net.MalformedURLException;

class GerritConfigProvider implements Provider<GerritConfig> {
  private final Config cfg;
  private final SshInfo sshInfo;

  private final ServletContext servletContext;

  @Inject
  GerritConfigProvider(@GerritServerConfig final Config gsc,
      final SshInfo si, final ServletContext sc) {
    cfg = gsc;
    sshInfo = si;
    servletContext = sc;
  }


  private GerritConfig create() throws MalformedURLException {
    final GerritConfig config = new GerritConfig();

    config.setUseContributorAgreements(cfg.getBoolean("auth",
        "contributoragreements", false));
    config.setGitDaemonUrl(cfg.getString("gerrit", null, "canonicalgiturl"));

    config.setDocumentationAvailable(servletContext
        .getResource("/Documentation/index.html") != null);


    if (sshInfo != null && !sshInfo.getHostKeys().isEmpty()) {
      config.setSshdAddress(sshInfo.getHostKeys().get(0).getHost());
    }

    config.setBackgroundColor(getThemeColor("backgroundColor", "#FFFFFF"));
    config.setTextColor(getThemeColor("textColor", "#000000"));
    config.setTrimColor(getThemeColor("trimColor", "#D4E9A9"));
    config.setSelectionColor(getThemeColor("selectionColor", "#FFFFCC"));

    config
        .setTopMenuColor(getThemeColor("topMenuColor", config.getTrimColor()));

    return config;
  }

  private String getThemeColor(String name, String defaultValue) {
    String v = cfg.getString("theme", null, name);
    if (v == null || v.isEmpty()) {
      v = defaultValue;
    }
    if (!v.startsWith("#") && v.matches("^[0-9a-fA-F]{2,6}$")) {
      v = "#" + v;
    }
    return v;
  }

  @Override
  public GerritConfig get() {
    try {
      return create();
    } catch (MalformedURLException e) {
      throw new ProvisionException("Cannot create GerritConfig instance", e);
    }
  }
}
