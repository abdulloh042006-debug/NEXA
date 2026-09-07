package ai.nexa.kernel.di

import ai.nexa.kernel.chat.ChatSessionPort
import ai.nexa.kernel.chat.DefaultChatSessionPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class KernelModule {
    @Binds
    abstract fun bindChatSessionPort(implementation: DefaultChatSessionPort): ChatSessionPort
}
