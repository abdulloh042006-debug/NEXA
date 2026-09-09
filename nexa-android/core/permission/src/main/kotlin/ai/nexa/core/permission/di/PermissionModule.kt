package ai.nexa.core.permission.di

import ai.nexa.core.permission.AuthorizationAuditObserver
import ai.nexa.core.permission.AuthorizationClock
import ai.nexa.core.permission.CapabilityEngine
import ai.nexa.core.permission.GrantStore
import ai.nexa.core.permission.InMemoryGrantStore
import ai.nexa.core.permission.RuntimePermissionState
import ai.nexa.core.permission.RuntimePermissionStatePort
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {
    @Binds
    abstract fun bindGrantStore(implementation: InMemoryGrantStore): GrantStore

    companion object {
        @Provides
        @Singleton
        fun provideAuthorizationClock(): AuthorizationClock = AuthorizationClock(System::currentTimeMillis)

        @Provides
        @Singleton
        fun provideRuntimePermissionState(): RuntimePermissionStatePort =
            RuntimePermissionStatePort { RuntimePermissionState.UNAVAILABLE }

        @Provides
        @Singleton
        fun provideAuthorizationAuditObserver(): AuthorizationAuditObserver = AuthorizationAuditObserver {}

        @Provides
        @Singleton
        fun provideCapabilityEngine(
            store: GrantStore,
            runtimePermissions: RuntimePermissionStatePort,
            clock: AuthorizationClock,
            audit: AuthorizationAuditObserver,
        ): CapabilityEngine = CapabilityEngine(store, runtimePermissions, clock, audit)
    }
}
