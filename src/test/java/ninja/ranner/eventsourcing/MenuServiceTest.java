package ninja.ranner.eventsourcing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuServiceTest {

    @Test
    void returnsConfiguredMenuItem() {
        MenuService menuService = MenuService.configureForTest(c -> c.add("Pizza", 7.99));

        assertThat(menuService.find("Pizza"))
                .contains(new MenuService.MenuItem("Pizza", 7.99));
    }

}