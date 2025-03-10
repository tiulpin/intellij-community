// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.highlighter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptorWithAccessors
import org.jetbrains.kotlin.descriptors.accessors
import org.jetbrains.kotlin.idea.KotlinBundle
import org.jetbrains.kotlin.idea.KotlinIcons
import org.jetbrains.kotlin.idea.highlighter.markers.LineMarkerInfos
import org.jetbrains.kotlin.idea.refactoring.getLineNumber
import org.jetbrains.kotlin.idea.util.safeAnalyzeNonSourceRootCode
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContext.*
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class KotlinSuspendCallLineMarkerProvider : LineMarkerProvider {
    private class SuspendCallMarkerInfo(callElement: PsiElement, message: String) : LineMarkerInfo<PsiElement>(
        callElement,
        callElement.textRange,
        KotlinIcons.SUSPEND_CALL,
        { message },
        null,
        GutterIconRenderer.Alignment.RIGHT,
        { message }
    ) {
        override fun createGutterRenderer(): GutterIconRenderer {
            return object : LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement>(this) {
                override fun getClickAction(): AnAction? = null
            }
        }
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: LineMarkerInfos
    ) {
        val markedLineNumbers = HashSet<Int>()

        for (element in elements) {
            ProgressManager.checkCanceled()

            if (element !is KtExpression) continue

            val containingFile = element.containingFile
            if (containingFile !is KtFile || containingFile is KtCodeFragment) {
                continue
            }

            val lineNumber = element.getLineNumber()
            if (lineNumber in markedLineNumbers) continue
            if (!element.hasSuspendCalls()) continue

            markedLineNumbers += lineNumber
            result += if (element is KtForExpression) {
                SuspendCallMarkerInfo(
                    getElementForLineMark(element.loopRange!!),
                    KotlinBundle.message("highlighter.message.suspending.iteration")
                )
            } else {
                SuspendCallMarkerInfo(getElementForLineMark(element), KotlinBundle.message("highlighter.message.suspend.function.call"))
            }
        }
    }
}

private fun KtExpression.isValidCandidateExpression(): Boolean {
    if (this is KtParenthesizedExpression) return false
    if (this is KtOperationReferenceExpression || this is KtForExpression || this is KtProperty || this is KtNameReferenceExpression) return true
    val parent = parent
    if (parent is KtCallExpression && parent.calleeExpression == this) return true
    if (this is KtCallExpression && (calleeExpression is KtCallExpression || calleeExpression is KtParenthesizedExpression)) return true
    return false
}

fun KtExpression.hasSuspendCalls(bindingContext: BindingContext = safeAnalyzeNonSourceRootCode(BodyResolveMode.PARTIAL)): Boolean {
    if (!isValidCandidateExpression()) return false

    return when (this) {
        is KtForExpression -> {
            val iteratorResolvedCall = bindingContext[LOOP_RANGE_ITERATOR_RESOLVED_CALL, loopRange]
            val loopRangeHasNextResolvedCall = bindingContext[LOOP_RANGE_HAS_NEXT_RESOLVED_CALL, loopRange]
            val loopRangeNextResolvedCall = bindingContext[LOOP_RANGE_NEXT_RESOLVED_CALL, loopRange]
            listOf(iteratorResolvedCall, loopRangeHasNextResolvedCall, loopRangeNextResolvedCall).any {
                it?.resultingDescriptor?.isSuspend == true
            }
        }
        is KtProperty -> {
            if (hasDelegateExpression()) {
                val variableDescriptor = bindingContext[DECLARATION_TO_DESCRIPTOR, this] as? VariableDescriptorWithAccessors
                val accessors = variableDescriptor?.accessors ?: emptyList()
                accessors.any { accessor ->
                    val delegatedFunctionDescriptor = bindingContext[DELEGATED_PROPERTY_RESOLVED_CALL, accessor]?.resultingDescriptor
                    delegatedFunctionDescriptor?.isSuspend == true
                }
            } else {
                false
            }
        }
        else -> {
            val resolvedCall = getResolvedCall(bindingContext)
            if ((resolvedCall?.resultingDescriptor as? FunctionDescriptor)?.isSuspend == true) true
            else {
                val propertyDescriptor = resolvedCall?.resultingDescriptor as? PropertyDescriptor
                val s = propertyDescriptor?.fqNameSafe?.asString()
                s?.startsWith("kotlin.coroutines.") == true && s.endsWith(".coroutineContext")
            }
        }
    }
}
