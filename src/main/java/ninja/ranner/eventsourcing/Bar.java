package ninja.ranner.eventsourcing;

import java.util.*;
import java.util.List;
import java.util.function.Function;

public class Bar extends EventSourcedAggregate<BarEvent> {

    private final Map<String, Tenant> tenants = new HashMap<>();
    private final MenuService menuService;

    public Bar() {
        this(new MenuService());
    }

    private Bar(MenuService menuService) {
        this.menuService = menuService;
    }

    public static Bar rebuild(List<BarEvent> events) {
        var bar = new Bar();
        return rebuild(bar, events);
    }

    private static Bar rebuild(Bar bar, List<BarEvent> events) {
        events.forEach(bar::rootApply);
        return bar;
    }

    public static Bar configureForTest(Function<Config, Config> configure) {
        Config config = configure.apply(new Config());
        Bar bar = new Bar(MenuService.configureForTest(config.menuItemConfig));
        return rebuild(bar, config.events);
    }

    public static Bar configureForTest() {
        return configureForTest(Function.identity());
    }

    public static class Config {
        private final MenuService.Config menuItemConfig = new MenuService.Config();
        private final List<BarEvent> events = new ArrayList<>();

        public Config addMenuItem(String itemName, double itemPrice) {
            menuItemConfig.add(itemName, itemPrice);
            return this;
        }

        public Config rebuildFrom(BarEvent... events) {
            this.events.addAll(Arrays.stream(events).toList());
            return this;
        }
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
                 BarEvent.DrinkOrdered _ -> {
            }
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

    public void orderDrink(String tenantName, String drinkName) {
        tenants.get(tenantName).orderDrink(drinkName, menuService.find(drinkName).price());
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
                    if (tenantName.equals(name)) {
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
            ensureIsWasShown();
            ensureDrinkingAge();

            enqueue(new BarEvent.DrinkOrdered(name, drinkName, price));
        }

        private void ensureDrinkingAge() {
            if (!isAllowedToOrderDrinks()) {
                throw new IllegalStateException("%s (%d) cannot order drinks. Must be 21 or older.".formatted(name, age));
            }
        }

        private void ensureIsWasShown() {
            if (age == null) {
                throw new IllegalStateException("%s cannot order drinks. Age not verified.".formatted(name));
            }
        }

        // Queries

        public String name() {
            return name;
        }

        private boolean isAllowedToOrderDrinks() {
            return age != null && age >= 21;
        }

        public double tabTotal() {
            return tabTotal;
        }
    }
}
