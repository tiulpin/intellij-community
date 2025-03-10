// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.idea.KotlinBundle
import org.jetbrains.kotlin.idea.util.safeAnalyzeNonSourceRootCode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.prefixExpressionVisitor
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.bindingContextUtil.isUsedAsExpression
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class UnusedUnaryOperatorInspection : AbstractKotlinInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = prefixExpressionVisitor(fun(prefix) {
        if (prefix.baseExpression == null) return
        val operationToken = prefix.operationToken
        if (operationToken != KtTokens.PLUS && operationToken != KtTokens.MINUS) return

        // hack to fix KTIJ-196 (unstable `USED_AS_EXPRESSION` marker for KtAnnotationEntry)
        if (prefix.isInAnnotationEntry) return
        val context = prefix.safeAnalyzeNonSourceRootCode(BodyResolveMode.PARTIAL_WITH_CFA)
        if (context == BindingContext.EMPTY || prefix.isUsedAsExpression(context)) return
        val operatorDescriptor = prefix.operationReference.getResolvedCall(context)?.resultingDescriptor as? DeclarationDescriptor ?: return
        if (!KotlinBuiltIns.isUnderKotlinPackage(operatorDescriptor)) return

        holder.registerProblem(prefix, KotlinBundle.message("unused.unary.operator"), RemoveUnaryOperatorFix())
    })

    private class RemoveUnaryOperatorFix : LocalQuickFix {
        override fun getName() = KotlinBundle.message("remove.unary.operator.fix.text")

        override fun getFamilyName() = name

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val prefixExpression = descriptor.psiElement as? KtPrefixExpression ?: return
            val baseExpression = prefixExpression.baseExpression ?: return
            prefixExpression.replace(baseExpression)
        }
    }
}

private val KtPrefixExpression.isInAnnotationEntry: Boolean
    get() = parentsWithSelf.takeWhile { it is KtExpression }.last().parent?.parent?.parent is KtAnnotationEntry
