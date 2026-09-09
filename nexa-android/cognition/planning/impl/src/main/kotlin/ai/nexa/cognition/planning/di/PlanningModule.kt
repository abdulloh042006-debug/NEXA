package ai.nexa.cognition.planning.di

import ai.nexa.cognition.planning.DefaultPlanningPort
import ai.nexa.cognition.planning.DefaultPlanAuthorizationPort
import ai.nexa.cognition.planning.JsonPlanCompiler
import ai.nexa.cognition.planning.api.PlanAuthorizationPort
import ai.nexa.cognition.planning.api.PlanCompiler
import ai.nexa.cognition.planning.api.PlanningPort
import ai.nexa.kernel.inference.InferenceOrchestratorPort
import ai.nexa.router.api.RoutingEnvironmentPort
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlanningModule {
    @Binds
    abstract fun bindPlanAuthorizationPort(implementation: DefaultPlanAuthorizationPort): PlanAuthorizationPort

    @Binds
    abstract fun bindPlanCompiler(implementation: JsonPlanCompiler): PlanCompiler

    companion object {
        @Provides
        @Singleton
        fun providePlanningPort(
            orchestrator: InferenceOrchestratorPort,
            compiler: PlanCompiler,
            environment: RoutingEnvironmentPort,
        ): PlanningPort = DefaultPlanningPort(orchestrator, compiler, environment)
    }
}
