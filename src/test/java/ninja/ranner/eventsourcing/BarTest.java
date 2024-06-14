package ninja.ranner.eventsourcing;

import ninja.ranner.eventsourcing.BarEvent.DrinkOrdered;
import ninja.ranner.eventsourcing.BarEvent.TenantAgeVerified;
import ninja.ranner.eventsourcing.BarEvent.TenantEntered;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

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

        @Test
        void tenantOrdersDrinkThenPriceIsAddedToTheirTab() {
            var bar = new Bar();
            bar.enter("Bill");
            bar.tenantShowsId("Bill", 36);

            bar.orderDrink("Bill", "Cuba Libre", 12.50);

            assertThat(bar.tabTotal("Bill"))
                    .isEqualTo(12.50);
        }

        @Test
        void tenantWithoutIdCannotOrderDrink() {
            var bar = new Bar();
            bar.enter("Lisa");

            assertThatIllegalStateException()
                    .isThrownBy(() -> bar.orderDrink("Lisa", "Rum", 12.50))
                    .withMessage("Lisa cannot order drinks. Age not verified.");
        }

        @Test
        void youngTenantCannotOrderDrink() {
            var bar = new Bar();
            bar.enter("Tom");
            bar.tenantShowsId("Tom", 18);

            assertThatIllegalStateException()
                    .isThrownBy(() -> bar.orderDrink("Tom", "Rum", 12.50))
                    .withMessage("Tom (18) cannot order drinks. Must be 21 or older.");
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

        @Test
        void tenantOrderingDringEnqueuesDrinkOrderedEvent() {
            var bar = Bar.rebuild(List.of(
                    new TenantEntered("Cho"),
                    new TenantAgeVerified("Cho", 56)));

            bar.orderDrink("Cho", "White Russian", 15.90);

            assertThat(bar.uncommitedEvents())
                    .containsExactly(new DrinkOrdered("Cho", "White Russian", 15.90));
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