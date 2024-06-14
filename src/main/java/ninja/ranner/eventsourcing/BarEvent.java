package ninja.ranner.eventsourcing;

public sealed interface BarEvent permits
        BarEvent.TenantAgeVerified,
        BarEvent.TenantEntered,
        BarEvent.TenantLeft {

    record TenantEntered(String name) implements BarEvent {}

    record TenantLeft(String name) implements BarEvent {}

    record TenantAgeVerified(String name, int age) implements BarEvent {}
}
