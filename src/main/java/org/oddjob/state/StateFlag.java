package org.oddjob.state;

/**
 * Flags to represent basic state.
 */
public enum StateFlag {

    /**
     * Is a job ready to be executed.
     */
    READY,

    /**
     * Is the job executing. This is normally when the thread of
     * execution is still within the job.
     */
    EXECUTING,

    /**
     * Can a job be stopped? This is a catch all for jobs
     * that are active or executing.
     */
    STOPPABLE,

    /**
     * Is a job or service complete?
     */
    COMPLETE,

    /**
     * Is a job or service incomplete. The implication of incomplete is
     * that it could be retried to be complete at some future date.
     */
    INCOMPLETE,

    /**
     * Is a job in an exception state. This is generally due to an
     * unexpected error, as opposed to incomplete which in some way
     * is expected.
     */
    EXCEPTION,

    /**
     * The job is destroyed. It is no longer available for anything.
     */
    DESTROYED
}
