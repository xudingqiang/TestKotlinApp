package com.bella.testapp

import android.app.Application
import android.content.ComponentName
import androidx.window.embedding.SplitController
import androidx.window.embedding.SplitPairFilter
import androidx.window.embedding.SplitPairRule
import androidx.window.embedding.SplitRule

class TestApp : Application () {
    override fun onCreate() {
        super.onCreate()

        val splitController = SplitController.getInstance(this)

        val rule = SplitPairRule.Builder(
            setOf(
                SplitPairFilter(
                    ComponentName(this, MainActivity::class.java),
                    ComponentName(this, JniTestActivity::class.java),
                    null
                )
            )
        )
//            .setSplitRatio(0.3f) // ⭐ 核心：左右比例
//            .setMinWidthDp(600)  // >=600dp 才启用
//            .setMinSmallestWidthDp(600)
//            .setFinishPrimaryWithSecondary(SplitRule.FINISH_NEVER)
//            .setFinishSecondaryWithPrimary(SplitRule.FINISH_ALWAYS)
//            .build()
//
//        splitController.setEmbeddingRules(setOf(rule))
    }
}