package org.oddjob.state;

import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.persist.Persistable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base functionality for changing {@link State}. This utility class is
 * responsible for notifying the change with the {@link StateHandler},
 * changing the icon, and persisting the job if necessary.
 *
 * @param <S> The type of State.
 * @author rob
 */
public class BaseStateChanger<S extends State> implements StateChanger<S> {

    private static final Logger logger = LoggerFactory.getLogger(BaseStateChanger.class);

    private final StateHandler<S> stateHandler;
    private final IconHelper iconHelper;
    private final Persistable persistable;
    private final S exceptionState;

    public BaseStateChanger(StateHandler<S> stateHandler,
                            IconHelper iconHelper, Persistable persistable,
                            S exceptionState) {
        this.stateHandler = stateHandler;
        this.iconHelper = iconHelper;
        this.persistable = persistable;
        this.exceptionState = exceptionState;
    }

    @Override
    public void setState(S state) {
        setState(state, StateInstant.now());
    }

    @Override
    public void setState(S state, StateInstant stateInstant) {
        if (state == stateHandler.getState()) {
            return;
        }

        stateHandler.setState(state, stateInstant);
        iconHelper.changeIcon(StateIcons.iconFor(state));

        try {

            if (new IsSaveable().test(state)) {
                persistable.persist();
            }

            stateHandler.fireEvent();

        } catch (ComponentPersistException e) {
            logger.error("Failed persisting state " + state, e);
            setStateException(e);
        }
    }

    @Override
    public void setStateException(Throwable t) {
        setStateException(t, StateInstant.now());
    }

    @Override
    public void setStateException(Throwable t, StateInstant instant) {
        if (exceptionState == stateHandler.getState()) {
            return;
        }

        stateHandler.setStateException(exceptionState, t, instant);
        iconHelper.changeIcon(IconHelper.EXCEPTION);

        if (IsSaveable.state(exceptionState) &&
                !(t instanceof ComponentPersistException)) {
            try {
                persistable.persist();
            } catch (ComponentPersistException e) {
                logger.error("Failed persisting state " + exceptionState, e);
                stateHandler.setStateException(exceptionState, e, instant);
            }
        }

        stateHandler.fireEvent();
    }


}
