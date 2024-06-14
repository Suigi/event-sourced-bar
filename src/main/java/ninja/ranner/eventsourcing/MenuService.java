package ninja.ranner.eventsourcing;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

/**
 * Nullable Infrastructure Wrapper for looking up prices for menu items.
 * <p>
 * It's not really doing anything in production because this is a toy project.
 * This is just here to show-case configuring the higher-level {@link Bar} object.
 * </p>
 */
public class MenuService {
    private final HashMap<String, MenuItem> items;

    public MenuService() {
        this(null);
    }

    public MenuService(HashMap<String, MenuItem> items) {
        this.items = items;
    }

    public static MenuService configureForTest(Function<Config, Config> configure) {
        return new MenuService(configure.apply(new Config()).allItems);
    }

    public Optional<MenuItem> find(String itemName) {
        if (items == null) {
            throw new UnsupportedOperationException("Production use-case is not implemented");
        }
        return Optional.ofNullable(items.get(itemName));
    }

    public static class Config {

        private final HashMap<String, MenuItem> allItems = new HashMap<>();

        public Config add(String itemName, double price) {
            allItems.put(itemName, new MenuItem(itemName, price));
            return this;
        }
    }

    public record MenuItem(String name, double price) {}
}
