package ninja.ranner.bar.domain;

import ninja.ranner.bar.eventsourcing.NestedEntity;

class Tenant extends NestedEntity<BarEvent> {
    private final String name;
    private Integer age;
    private double tabTotal = 0;

    Tenant(String name, Bar bar) {
        super(bar::enqueue);
        this.name = name;
    }

    @Override
    protected void apply(BarEvent barEvent) {
        switch (barEvent) {
            case BarEvent.TenantAgeVerified(String tenantName, int tenantAge) -> {
                if (tenantName.equals(name)) {
                    this.age = tenantAge;
                }
            }
            case BarEvent.DrinkOrdered(String tenantName, _, double price) -> {
                if (tenantName.equals(name)) {
                    this.tabTotal += price;
                }
            }

            // We don't care about these events.
            case BarEvent.TenantEntered _, BarEvent.TenantLeft _ -> {}
        }
    }

    // Commands

    public void showId(int age) {
        enqueue(new BarEvent.TenantAgeVerified(name, age));
    }

    public void orderDrink(String drinkName, double price) {
        ensureIdWasShown();
        ensureDrinkingAge();

        enqueue(new BarEvent.DrinkOrdered(name, drinkName, price));
    }

    private void ensureDrinkingAge() {
        if (!isAllowedToOrderDrinks()) {
            throw new IllegalStateException("%s (%d) cannot order drinks. Must be 21 or older.".formatted(name, age));
        }
    }

    private void ensureIdWasShown() {
        if (age == null) {
            throw new IllegalStateException("%s cannot order drinks. Age not verified.".formatted(name));
        }
    }

    // Queries

    public String name() {
        return name;
    }

    public boolean isAllowedToOrderDrinks() {
        return age != null && age >= 21;
    }

    public double tabTotal() {
        return tabTotal;
    }
}
