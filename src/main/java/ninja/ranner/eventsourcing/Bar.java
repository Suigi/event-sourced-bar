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

            // The bar can ignore the TenantAgeVerified event,
            // because the Tenant entity handles it.
            case BarEvent.TenantAgeVerified _ -> {}
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
        tenants.get(tenantName).showsId(age);
    }

    // Queries

    public Set<String> tenantNames() {
        return tenants.keySet();
    }

    public boolean isAllowedToOrderDrinks(String tenantName) {
        return tenants.get(tenantName).isAllowedToOrderDrinks();
    }

    static class Tenant extends Entity<BarEvent> {
        private final String name;
        private Integer age;

        Tenant(String name, Bar bar) {
            super(bar::enqueue);
            this.name = name;
        }

        private boolean isAllowedToOrderDrinks() {
            return age >= 21;
        }

        public String name() {
            return name;
        }

        @Override
        protected void apply(BarEvent barEvent) {
            switch (barEvent) {
                case BarEvent.TenantEntered _, BarEvent.TenantLeft _ -> {}
                case BarEvent.TenantAgeVerified(String tenantName, int tenantAge) -> {
                    if (tenantName.equals(name)) {
                        this.age = tenantAge;
                    }
                }
            }
        }

        public void showsId(int age) {
            enqueue(new BarEvent.TenantAgeVerified(name, age));
        }
    }
}
