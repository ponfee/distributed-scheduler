/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.core.enums;

import static cn.ponfee.scheduler.core.enums.ExecuteState.*;

/**
 * Task operate type
 * 
 * @author Ponfee
 */
public enum Operations {

    /**
     * Trigger from WAITING to EXECUTING
     */
    TRIGGER(WAITING, EXECUTING),

    /**
     * Pause from EXECUTING to PAUSED
     */
    PAUSE(EXECUTING, PAUSED),

    /**
     * Exception cancel from EXECUTING to EXECUTE_EXCEPTION
     */
    EXCEPTION_CANCEL(EXECUTING, EXECUTE_EXCEPTION),

    /**
     * Collision cancel from EXECUTING to EXECUTE_COLLISION
     */
    COLLISION_CANCEL(EXECUTING, EXECUTE_COLLISION),

    /**
     * Manual cancel from EXECUTING to MANUAL_CANCELED
     */
    MANUAL_CANCEL(EXECUTING, MANUAL_CANCELED),

    ;

    private final ExecuteState sourceState;
    private final ExecuteState targetState;

    Operations(ExecuteState sourceState, ExecuteState targetState) {
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    public ExecuteState sourceState() {
        return sourceState;
    }

    public ExecuteState targetState() {
        return targetState;
    }

    public static Operations of(int ordinal) {
        // Operations.class.getEnumConstants()[ordinal];
        return Operations.values()[ordinal];
    }
}