# Event-Sourced Aggregate Root Example

On a recent live stream, Ted M. Young implemented his TDD Game, which uses Event Sourcing as its persistence mechanism.

We tried to learn about two different concepts at the same time:

* How does Event Sourcing work? In particular, we struggled with structuring a Root Entity and how to delegate commands and events to nested Entities.
* Learning about James Shore's Nullable Infrastructure Wrappers.

In this repository, I've tried one approach, where the nested Entities are constructed with a callback to enqueue Events of their own. All Events applied to the Root Entity will also be applied to all nested Entities.

Configuring an instance of the Root Entity that's being rebuilt from a set of Events and configures lower-level Nullable Wrappers looks like this:

```java
@Test
void tenantOrderingDrinkEnqueuesDrinkOrderedEvent() {
    var bar = Bar.configureForTest(c -> c
            .rebuildFrom(
                    new TenantEntered("Cho"),
                    new TenantAgeVerified("Cho", 56))
            .addMenuItem("White Russian", 15.90));

    bar.orderDrink("Cho", "White Russian");

    assertThat(bar.uncommitedEvents())
            .containsExactly(new DrinkOrdered("Cho", "White Russian", 15.90));
}
```