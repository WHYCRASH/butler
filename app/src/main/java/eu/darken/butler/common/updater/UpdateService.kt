package eu.darken.butler.common.updater

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateService @Inject constructor() {
    val availableUpdate: Flow<UpdateChecker.Update?> = flowOf(null)

    suspend fun startUpdate(update: UpdateChecker.Update) {}
    suspend fun viewUpdate(update: UpdateChecker.Update) {}
    suspend fun dismissUpdate(update: UpdateChecker.Update) {}
    suspend fun refresh() {}
}
