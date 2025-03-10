// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.codeVision

import com.intellij.codeInsight.codeVision.settings.PlatformCodeVisionIds
import com.intellij.codeInsight.codeVision.ui.model.CodeVisionPredefinedActionEntry
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.TextRange
import org.jetbrains.annotations.Nls

/**
 * @see CodeVisionProviderFactory
 * If invalidation and calculation should be bound to daemon, then use @see [com.intellij.codeInsight.hints.codeVision.DaemonBoundCodeVisionProvider]
 * Otherwise implement [CodeVisionProvider] directly
 *
 * If you want to implement multiple providers with same meaning (for example, for different languages)
 * and group them in settings window, then @see [com.intellij.codeInsight.codeVision.settings.CodeVisionGroupSettingProvider]
 * Also @see [PlatformCodeVisionIds]
 */
interface CodeVisionProvider<T> {
  companion object {
    val EP_NAME = "com.intellij.codeInsight.codeVisionProvider"
    val providersExtensionPoint = ExtensionPointName.create<CodeVisionProvider<*>>(EP_NAME)
  }

  /**
   * Computes some data on UI thread, before the background thread invocation
   */
  fun precomputeOnUiThread(editor: Editor): T

  /**
   *  Called during code vision update process
   *  Return true if [computeForEditor] should be called
   *  false otherwise
   */
  fun shouldRecomputeForEditor(editor: Editor, uiData: T) = true

  /**
   * Should return text ranges and applicable hints for them, invoked on background thread.
   *
   * Note that this method is not executed under read action.
   */
  fun computeForEditor(editor: Editor, uiData: T): List<Pair<TextRange, CodeVisionEntry>>

  /**
   * Handle click on a lens at given range
   * [java.awt.event.MouseEvent] accessible with [codeVisionEntryMouseEventKey] data key from [CodeVisionEntry]
   */
  fun handleClick(editor: Editor, textRange: TextRange, entry: CodeVisionEntry){
    if (entry is CodeVisionPredefinedActionEntry) entry.onClick(editor)
  }

  /**
   * Handle click on an extra action on a lens at a given range
   */
  fun handleExtraAction(editor: Editor, textRange: TextRange, actionId: String) = Unit

  /**
   * User-visible name
   */
  @get:Nls
  val name: String

  /**
   * Used for the default sorting of providers
   */
  val relativeOrderings: List<CodeVisionRelativeOrdering>

  /**
   * Specifies default anchor for this provider
   */
  val defaultAnchor: CodeVisionAnchorKind

  /**
   * Internal id
   */
  val id: String


  /**
   * Uses to group provider in settings panel and to share same behavior (like position and ext.) and description
   * @see PlatformCodeVisionIds
   * To group different provider implement @see [com.intellij.codeInsight.codeVision.settings.CodeVisionGroupSettingProvider]
   */
  val groupId: String
    get() = id
}