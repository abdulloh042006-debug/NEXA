package ai.nexa.core.data.settings

import ai.nexa.core.proto.LocalSettings
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Single-process persistence seam for the future core:data singleton binding (SPEC section 8.2).
 * The owner supplies a stable app-private file, application scope and IO dispatcher; creates only
 * one store per file in the main process; and cancels/joins that scope before reopening the file.
 * No Hilt graph, independent job, silent corruption replacement or historical migration is installed.
 * Corrupt/unsupported data stays untouched for a future guided recovery adapter (SPEC section 17).
 */
internal fun createLocalSettingsStore(
    file: File,
    scope: CoroutineScope,
    ioDispatcher: CoroutineDispatcher,
): DataStore<LocalSettings> {
    requireNotNull(scope.coroutineContext[Job]) { "LocalSettings requires an owned lifecycle Job" }
    return DataStoreFactory.create(
        serializer = LocalSettingsSerializer,
        scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
        produceFile = { file },
    )
}
