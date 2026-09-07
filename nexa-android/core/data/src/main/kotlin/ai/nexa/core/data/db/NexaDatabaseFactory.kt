package ai.nexa.core.data.db

import android.content.Context
import androidx.room.Room
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/** Creates the encrypted primary store. Callers own the passphrase and must never persist it raw. */
object NexaDatabaseFactory {
    fun create(
        context: Context,
        passphrase: ByteArray,
    ): NexaDatabase {
        require(passphrase.isNotEmpty()) { "Database passphrase cannot be empty" }
        SQLiteDatabase.loadLibs(context.applicationContext)
        return Room.databaseBuilder(
            context.applicationContext,
            NexaDatabase::class.java,
            NexaDatabase.FILE_NAME,
        ).openHelperFactory(SupportFactory(passphrase.copyOf())).build()
    }
}
