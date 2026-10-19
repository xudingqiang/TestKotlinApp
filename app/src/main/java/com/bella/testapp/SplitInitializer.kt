package com.bella.testapp

import android.content.Context
import androidx.startup.Initializer
import androidx.window.embedding.RuleController

/**
 * Applies the activity-embedding split rules in `res/xml/main_split_config.xml`.
 *
 * Registered through `androidx.startup` in the manifest; the package must match the application
 * id/namespace so the provider can instantiate it by name.
 */
class SplitInitializer : Initializer<RuleController> {
    override fun create(context: Context): RuleController {
        return RuleController.getInstance(context).apply {
            setRules(RuleController.parseRules(context, R.xml.main_split_config))
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}
