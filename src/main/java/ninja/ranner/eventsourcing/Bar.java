package ninja.ranner.eventsourcing;

import java.util.*;

public class Bar extends EventSourcedAggregate<BarEvent> {

    private final Map<String, Tenant> tenants = new HashMap<>();

    public static Bar rebuild(List<BarEvent> events) {
        var bar = new Bar();
        events.forEach(bar::rootApply);
        return bar;
    }

    @Override
    protected void apply(BarEvent barEvent) {

        switch (barEvent) {
            case BarEvent.TenantEntered(String tenantName) -> tenants
                    .put(tenantName, registerEntity(new Tenant(tenantName, this)));
            case BarEvent.TenantLeft(String tenantName) -> tenants
                    .remove(tenantName);

            // The bar can ignore the TenantAgeVerified and DrinkOrdered events,
            // because the Tenant entity handles them.
            case BarEvent.TenantAgeVerified _,
                 BarEvent.DrinkOrdered _ -> {}
        }

    }

    // Commands

    public void enter(String tenantName) {
        enqueue(new BarEvent.TenantEntered(tenantName));
    }

    public void leave(String tenantName) {
        enqueue(new BarEvent.TenantLeft(tenantName));
    }

    public void tenantShowsId(String tenantName, int age) {
        tenants.get(tenantName).showId(age);
    }

    public void orderDrink(String tenantName, String drinkName, double price) {
        tenants.get(tenantName).orderDrink(drinkName, price);
    }

    // Queries

    public Set<String> tenantNames() {
        return tenants.keySet();
    }

    public boolean isAllowedToOrderDrinks(String tenantName) {
        return tenants.get(tenantName).isAllowedToOrderDrinks();
    }

    public double tabTotal(String tenantName) {
        return tenants.get(tenantName).tabTotal();
    }

    static class Tenant extends Entity<BarEvent> {
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
                case BarEvent.TenantEntered _, BarEvent.TenantLeft _ -> {
                }
                case BarEvent.TenantAgeVerified(String tenantName, int tenantAge) -> {
                    if (tenantName.equals(name)) {
                        this.age = tenantAge;
                    }
                }
                case BarEvent.DrinkOrdered(String tenantName, _, double price) -> {
                    if (tenantName.equals(name) && isAllowedToOrderDrinks()) {
                        this.tabTotal += price;
                    }
                }
            }
        }

        // Commands

        public void showId(int age) {
            enqueue(new BarEvent.TenantAgeVerified(name, age));
        }

        public void orderDrink(String drinkName, double price) {
            enqueue(new BarEvent.DrinkOrdered(name, drinkName, price));
        }

        // Queries

        public String name() {
            return name;
        }

        private boolean isAllowedToOrderDrinks() {
            return age >= 21;
        }

        public double tabTotal() {
            return tabTotal;
        }
    }
}
