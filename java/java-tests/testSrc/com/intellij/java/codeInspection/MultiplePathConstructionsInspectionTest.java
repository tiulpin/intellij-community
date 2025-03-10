// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.java.codeInspection;

import com.intellij.codeInsight.daemon.quickFix.LightQuickFixParameterizedTestCase;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.MultiplePathConstructionsInspection;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.testFramework.IdeaTestUtil;
import org.jetbrains.annotations.NotNull;

public class MultiplePathConstructionsInspectionTest extends LightQuickFixParameterizedTestCase {
  
  @Override
  protected LocalInspectionTool @NotNull [] configureLocalInspectionTools() {
    return new LocalInspectionTool[]{new MultiplePathConstructionsInspection()};
  }

  @Override
  protected Sdk getProjectJDK() {
    return IdeaTestUtil.getMockJdk11();
  }

  @Override
  protected LanguageLevel getLanguageLevel() {
    return LanguageLevel.JDK_11;
  }

  @Override
  protected String getBasePath() {
    return "/codeInsight/daemonCodeAnalyzer/quickFix/multiplePathConstructions";
  }
}
