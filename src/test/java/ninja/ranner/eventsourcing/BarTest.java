package ninja.ranner.eventsourcing;

import ninja.ranner.eventsourcing.BarEvent.TenantAgeVerified;
import ninja.ranner.eventsourcing.BarEvent.TenantEntered;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BarTest {

    @Nested
    class Behavior {

        @Test
        void aNewBarIsEmpty() {
            var bar = new Bar();
            assertThat(bar.tenantNames())
                    .isEmpty();
        }

        @Test
        void enteredTenantIsPresentInBar() {
            var bar = new Bar();

            bar.enter("Anna");

            assertThat(bar.tenantNames())
                    .containsExactly("Anna");
        }

        @Test
        void barIsEmptyWhenTenantEntersAndLeaves() {
            var bar = new Bar();
            bar.enter("Anna");

            bar.leave("Anna");

            assertThat(bar.tenantNames())
                    .isEmpty();
        }

        @Test
        void tenantShowsIdThenAgeIsVerified() {
            var bar = new Bar();
            bar.enter("Anna");

            bar.tenantShowsId("Anna", 25);

            assertThat(bar.isAllowedToOrderDrinks("Anna"))
                    .isTrue();
        }
    }

    @Nested
    class CommandsGenerateEvents {

        @Test
        void enterEnqueuesTenantEnteredEvent() {
            var bar = new Bar();

            bar.enter("Mia");

            assertThat(bar.uncommitedEvents())
                    .containsExactly(new TenantEntered("Mia"));
        }

        @Test
        void tenantShowingIdEnqueuesTenantAgeVerifiedEvent() {
            var bar = Bar.rebuild(List.of(new TenantEntered("Cho")));

            bar.tenantShowsId("Cho", 56);

            assertThat(bar.uncommitedEvents())
                    .containsExactly(new TenantAgeVerified("Cho", 56));
        }
    }

    @Nested
    class EventsChangeState {

        @Test
        void rebuildingBarFromEventsRecreatesBarState() {
            Bar bar = Bar.rebuild(List.of(new TenantEntered("Joe")));

            assertThat(bar.tenantNames())
                    .containsExactly("Joe");
        }

        @Test
        void rebuildingBarWithTenantOfLegalDrinkingAgeRecreatesBarState() {
            Bar bar = Bar.rebuild(List.of(
                    new TenantEntered("Joe"),
                    new TenantAgeVerified("Joe", 21)));

            assertThat(bar.isAllowedToOrderDrinks("Joe"))
                    .isTrue();
        }
    }

}