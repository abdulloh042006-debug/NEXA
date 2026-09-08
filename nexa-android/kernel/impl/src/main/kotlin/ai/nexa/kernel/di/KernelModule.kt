package ai.nexa.kernel.di

import ai.nexa.kernel.blackboard.Blackboard
import ai.nexa.kernel.blackboard.InMemoryBlackboard
import ai.nexa.kernel.chat.ChatSessionPort
import ai.nexa.kernel.chat.DefaultChatSessionPort
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class KernelModule {
    @Binds
    abstract fun bindChatSessionPort(implementation: DefaultChatSessionPort): ChatSessionPort

    companion object {
        @Provides
        @Singleton
        fun provideBlackboard(): Blackboard = InMemoryBlackboard()
    }
}
