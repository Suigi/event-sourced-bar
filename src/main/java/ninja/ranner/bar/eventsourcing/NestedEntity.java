package ninja.ranner.bar.eventsourcing;

import java.util.function.Consumer;

/**
 * Represents a nested Entity of an event-sourced Aggregate.
 *
 * <p>All events are enqueued to the root Aggregate.</p>
 *
 * @param <EVENT>
 */
public abstract class NestedEntity<EVENT> {

    private final Consumer<EVENT> rootEnqueue;

    public NestedEntity(Consumer<EVENT> enqueue) {
        this.rootEnqueue = enqueue;
    }

    protected void enqueue(EVENT event) {
        rootEnqueue.accept(event);
    }

    protected abstract void apply(EVENT event);
}
