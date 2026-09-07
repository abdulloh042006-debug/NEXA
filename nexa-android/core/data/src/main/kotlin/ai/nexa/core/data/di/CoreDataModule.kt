package ai.nexa.core.data.di

import ai.nexa.core.data.conversation.ConversationDao
import ai.nexa.core.data.conversation.MessageDao
import ai.nexa.core.data.crypto.DatabasePassphraseProvider
import ai.nexa.core.data.db.NexaDatabase
import ai.nexa.core.data.db.NexaDatabaseFactory
import ai.nexa.core.data.settings.createLocalSettingsStore
import ai.nexa.core.proto.LocalSettings
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {
    @Provides
    @Singleton
    fun provideLocalSettings(@ApplicationContext context: Context): DataStore<LocalSettings> =
        createLocalSettingsStore(
            file = context.dataStoreFile("local_settings.pb"),
            scope = CoroutineScope(SupervisorJob()),
            ioDispatcher = Dispatchers.IO,
        )

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        passphraseProvider: DatabasePassphraseProvider,
    ): NexaDatabase {
        val passphrase = passphraseProvider.getOrCreate()
        return try {
            NexaDatabaseFactory.create(context, passphrase)
        } finally {
            passphrase.fill(0)
        }
    }

    @Provides
    fun provideConversationDao(database: NexaDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideMessageDao(database: NexaDatabase): MessageDao = database.messageDao()
}
