package ai.nexa.router.api

/** Supplies the router with a current, explicit view of runtime constraints. */
fun interface RoutingEnvironmentPort {
    fun currentDeviceState(): RoutingDeviceState
}
