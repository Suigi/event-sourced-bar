package ninja.ranner.bar.eventsourcing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EventSourcedAggregate<EVENT> {

    private final List<EVENT> uncommitedEvents = new ArrayList<>();
    private final List<Entity<EVENT>> entities = new ArrayList<>();

    protected abstract void apply(EVENT event);

    public void enqueue(EVENT event) {
        rootApply(event);
        uncommitedEvents.add(event);
    }

    protected void rootApply(EVENT event) {
        apply(event);
        entities.forEach(entity -> entity.apply(event));
    }

    protected <T extends Entity<EVENT>> T registerEntity(T entity) {
        entities.add(entity);
        return entity;
    }

    public List<EVENT> uncommitedEvents() {
        return uncommitedEvents;
    }

    public static abstract class Entity<EVENT> {

        private final Consumer<EVENT> rootEnqueue;

        public Entity(Consumer<EVENT> enqueue) {
            this.rootEnqueue = enqueue;
        }

        protected void enqueue(EVENT event) {
            rootEnqueue.accept(event);
        }

        protected abstract void apply(EVENT event);
    }

}
