package ninja.ranner.bar.domain;

public sealed interface BarEvent permits
        BarEvent.DrinkOrdered,
        BarEvent.TenantAgeVerified,
        BarEvent.TenantEntered,
        BarEvent.TenantLeft {

    record TenantEntered(String tenantName) implements BarEvent {}

    record TenantLeft(String tenantName) implements BarEvent {}

    record TenantAgeVerified(String tenantName, int age) implements BarEvent {}

    record DrinkOrdered(String tenantName, String drinkName, double price) implements BarEvent {}
}
