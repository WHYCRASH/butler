package eu.darken.butler.main.core.motd

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class MotdRepo @Inject constructor() {
    val motd: Flow<MotdState?> = flowOf(null)

    suspend fun dismiss(id: Uuid) {}
}
