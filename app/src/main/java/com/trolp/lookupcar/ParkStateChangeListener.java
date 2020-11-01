package com.trolp.lookupcar;

import com.trolp.lookupcar.ParkStateMachine.ParkStates;
import com.trolp.lookupcar.ParkStateMachine.UserStateData;

public interface ParkStateChangeListener {
	public void onBeforeStateChange(ParkStates from, ParkStates to, UserStateData data);
	public void onStateChange(ParkStates from, ParkStates to, UserStateData data);
	public void onAfterStateChange(ParkStates from, ParkStates to, UserStateData data);
}
