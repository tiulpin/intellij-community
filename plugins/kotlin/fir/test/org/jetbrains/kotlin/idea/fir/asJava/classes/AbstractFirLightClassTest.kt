// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.fir.asJava.classes

import com.intellij.util.ThrowableRunnable
import org.jetbrains.kotlin.executeOnPooledThreadInReadAction
import org.jetbrains.kotlin.idea.caches.resolve.LightClassTestCommon
import org.jetbrains.kotlin.idea.caches.resolve.PsiElementChecker
import org.jetbrains.kotlin.idea.caches.resolve.findClass
import org.jetbrains.kotlin.idea.core.script.ScriptConfigurationManager
import org.jetbrains.kotlin.idea.fir.findUsages.doTestWithFIRFlagsByPath
import org.jetbrains.kotlin.idea.fir.invalidateCaches
import org.jetbrains.kotlin.idea.test.KotlinLightCodeInsightFixtureTestCase
import org.jetbrains.kotlin.idea.test.KotlinTestUtils
import org.jetbrains.kotlin.idea.test.KotlinWithJdkAndRuntimeLightProjectDescriptor
import org.jetbrains.kotlin.idea.test.runAll
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

abstract class AbstractFirLightClassTest : KotlinLightCodeInsightFixtureTestCase() {

    override fun isFirPlugin(): Boolean = true

    fun doTest(path: String) = doTestWithFIRFlagsByPath(path) {
        doTestImpl()
    }

    override fun tearDown() {
        runAll(
            ThrowableRunnable { project.invalidateCaches() },
            ThrowableRunnable { super.tearDown() },
        )
    }

    private fun doTestImpl() {
        val fileName = fileName()
        val extraFilePath = when {
            fileName.endsWith(fileExtension) -> fileName.replace(fileExtension, ".extra" + fileExtension)
            else -> error("Invalid test data extension")
        }

        val testFiles = if (File(testDataPath, extraFilePath).isFile) listOf(fileName, extraFilePath) else listOf(fileName)

        myFixture.configureByFiles(*testFiles.toTypedArray())
        if ((myFixture.file as? KtFile)?.isScript() == true) {
            error { "FIR for scripts does not supported yet" }
            ScriptConfigurationManager.updateScriptDependenciesSynchronously(myFixture.file)
        }

        val ktFile = myFixture.file as KtFile
        val testData = testDataFile()

        val actual = executeOnPooledThreadInReadAction {
            LightClassTestCommon.getActualLightClassText(
                testData,
                { fqName ->
                    findClass(fqName, ktFile, project)?.apply {
                        PsiElementChecker.checkPsiElementStructure(this)
                    }
                },
                { it }
            )
        }

        KotlinTestUtils.assertEqualsToFile(KotlinTestUtils.replaceExtension(testData, "java"), actual)
    }

    override fun getProjectDescriptor() = KotlinWithJdkAndRuntimeLightProjectDescriptor.INSTANCE

    open val fileExtension = ".kt"
}