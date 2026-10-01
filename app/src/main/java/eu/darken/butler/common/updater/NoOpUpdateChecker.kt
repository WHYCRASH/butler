package eu.darken.butler.common.updater

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpUpdateChecker @Inject constructor() : UpdateChecker {
    override suspend fun getLatest(channel: UpdateChecker.Channel): UpdateChecker.Update? = null
    override suspend fun startUpdate(update: UpdateChecker.Update) {}
    override suspend fun viewUpdate(update: UpdateChecker.Update) {}
    override suspend fun dismissUpdate(update: UpdateChecker.Update) {}
    override suspend fun isDismissed(update: UpdateChecker.Update): Boolean = true
    override suspend fun isCheckSupported(): Boolean = false
    override fun isEnabledByDefault(): Boolean = false
}
