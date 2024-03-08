package org.oddjob.jobs.structural;

import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;
import org.oddjob.values.types.ComparisonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @oddjob.description Switch Based on a predicate.
 *
 * @author Rob Gordon
 */
public class SwitchJob extends StructuralJob<Object>
        implements Runnable, Stateful, Resettable, Structural, Stoppable {

    private static final long serialVersionUID = 20050806;

    private static final Logger logger = LoggerFactory.getLogger(SwitchJob.class);

    /**
     * @oddjob.property
     * @oddjob.description The value to check.
     * @oddjob.required No, but the check value is not null will fail.
     */
    private transient Object value;

    private transient Predicate<Object>[] predicates;

    private transient ArooaValue[] switches;

    private static final StateOperator stateOperator =
            new StateOperator() {
                final StateOperator stateOperator = new WorstStateOp();
                @Override
                public StateEvent evaluate(StateEvent... states) {
                    // Because only one job will be executed we ignore all
                    // ready states.
                    StateEvent[] events = Arrays.stream(states)
                            .filter(stateEvent -> !stateEvent.getState().isReady())
                            .toArray(StateEvent[]::new);
                    return stateOperator.evaluate(events);
                }
            };

    /**
     * @oddjob.property jobs
     * @oddjob.description The child jobs.
     * @oddjob.required At least one.
     */
    @ArooaComponent
    public void setJobs(int index, Object job) {
        if (job == null) {
            childHelper.removeChildAt(index);
        } else {
            childHelper.insertChild(index, job);
        }
    }

    @Override
    protected StateOperator getInitialStateOp() {
        return stateOperator;
    }

    protected void execute() {

        if (childHelper.size() < 1) {
            return;
        }

        ArooaValue[] switches = this.switches;
        if (switches != null) {
            if (this.predicates != null) {
                logger().warn("Predicates are being overridden with switches");
            }
            Predicate<Object>[] predicates = new Predicate[switches.length];
            for (int i = 0; i < predicates.length; ++i) {
                ComparisonType<Object> comparisonType = new ComparisonType<>();
                ArooaValue theSwitch = Objects.requireNonNull(switches[i],
                        "Switch " + i + " can not be null.");
                comparisonType.setArooaSession(getArooaSession());
                comparisonType.setEq(theSwitch);
                predicates[i] = comparisonType.toValue();
                this.predicates = predicates;
            }
        }

        Predicate<Object>[] predicates = Objects.requireNonNull(this.predicates,
                "No Predicates");

        Object value = this.value;

        logger.info("Checking [{}] against predicates {}", value, Arrays.toString(predicates));

        int i = 0;
        while (i < predicates.length) {

            Predicate<Object> predicate = predicates[i];
            if (predicate.test(value)) {
                logger.debug("[{}] {} passed, job index={}", value, predicate, i);
                break;
            }
            else {
                logger.debug("[{}] {} failed", value, predicate);
            }
            ++i;
        }

        Object[] children = childHelper.getChildren();
        if (children.length <= i) {
            throw new IllegalArgumentException("No child job at index " + i);
        }

        Object child = children[i];
        if (!(child instanceof Stateful)) {
            logger().info("Child [" + child +
                    "] is not Stateful - ignoring.");
            return;
        }
        if (!(child instanceof Runnable)) {
            logger().info("Child [" + child +
                    "] is not Runnable - ignoring.");
            return;
        }

        logger.info("Running child {}, {}", i, child);

        ((Runnable) child).run();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Predicate<Object>[] getPredicates() {
        return predicates;
    }

    public void setPredicates(Predicate<Object>[] predicates) {
        this.predicates = predicates;
    }

    public ArooaValue[] getSwitches() {
        return switches;
    }

    public void setSwitches(ArooaValue[] switches) {
        this.switches = switches;
    }
}

