package org.mbach.homeautomation.actions;

/**
 * IAction class interface.
 *
 * @author Matthieu BACHELIER
 * @since 2017-10
 */
public interface IAction {
    String getStatus();

    String isConnected();

    String isProtected();

    String toggle();

    String powerOn();

    String powerOff();
}
