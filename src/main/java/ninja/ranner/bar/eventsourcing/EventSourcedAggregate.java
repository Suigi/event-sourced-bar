package ninja.ranner.bar.eventsourcing;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Aggregate Root of an event-source Aggregate.
 *
 * <p>
 * Keeps a list of nested Entities. The nested Entities receive all applied Events
 * and are allowed to enqueue new Events through the root.
 * </p>
 *
 * @param <EVENT> the type of events that this Aggregate is persisted by.
 */
public abstract class EventSourcedAggregate<EVENT> {

    private final List<EVENT> uncommitedEvents = new ArrayList<>();
    private final List<NestedEntity<EVENT>> entities = new ArrayList<>();

    protected abstract void apply(EVENT event);

    public void enqueue(EVENT event) {
        rootApply(event);
        uncommitedEvents.add(event);
    }

    protected void rootApply(EVENT event) {
        apply(event);
        entities.forEach(nestedEntity -> nestedEntity.apply(event));
    }

    protected <T extends NestedEntity<EVENT>> T registerEntity(T entity) {
        entities.add(entity);
        return entity;
    }

    public List<EVENT> uncommitedEvents() {
        return uncommitedEvents;
    }

}
