package com.trolp.lookupcar;

import java.util.ArrayList;
import java.util.List;

public class ParkStateMachine {
	private List<ParkStateChangeListener> listeners;

	public static class UserStateData {
		private Object data;
		public <T> UserStateData(T data) {
			this.data = data;
		}

		public <T> T getData() {
			return (T)data;
		}
	}

	public enum ParkStates {
		START,
		PARKABLE,
		PARKED,
		FINDING;
	}

	public enum ParkStateEvent {
		READY,
		PARK,
		UNPARK,
		FIND,
		FOUND;
	}

	private volatile ParkStates currentState = ParkStates.START;
	private volatile ParkStates previousState = currentState;
	private boolean started = true;
	private final ParkStates[][] resolve = new ParkStates[][]{
			{ParkStates.PARKABLE, ParkStates.PARKED, ParkStates.START, ParkStates.START, ParkStates.START},
			{ParkStates.PARKABLE, ParkStates.PARKED, ParkStates.PARKABLE, ParkStates.PARKABLE, ParkStates.PARKABLE},
			{ParkStates.PARKED, ParkStates.PARKED, ParkStates.PARKABLE, ParkStates.FINDING, ParkStates.PARKED},
			{ParkStates.FINDING, ParkStates.FINDING, ParkStates.FINDING, ParkStates.FINDING, ParkStates.PARKED}};

	public ParkStateMachine() {
		listeners = new ArrayList<ParkStateChangeListener>();
	}

	public void begin() {
		if(started) {
			notifyListeners(ParkStates.START, ParkStates.START, null);
			started = false;
		}
	}

	public void ready(UserStateData data) {
		processState(ParkStateEvent.READY, data);
	}

	public void park(UserStateData data) {
		processState(ParkStateEvent.PARK, data);
	}

	public void unpark(UserStateData data) {
		processState(ParkStateEvent.UNPARK, data);
	}

	public void find(UserStateData data) {
		processState(ParkStateEvent.FIND, data);
	}

	public void found(UserStateData data) {
		processState(ParkStateEvent.FOUND, data);
	}

	private synchronized void processState(ParkStateEvent event, UserStateData data) {
		begin();

		ParkStates from = currentState;
		ParkStates to = resolve[currentState.ordinal()][event.ordinal()];
		notifyListeners(from, to, data);
	}

	private synchronized void notifyListeners(ParkStates from, ParkStates to, UserStateData data) {
		for(ParkStateChangeListener listener : listeners) {
			listener.onBeforeStateChange(from, to, data);
		}

		for(ParkStateChangeListener listener : listeners) {
			listener.onStateChange(from, to, data);
		}

		for(ParkStateChangeListener listener : listeners) {
			listener.onAfterStateChange(from, to, data);
		}
		previousState = currentState;
		currentState = to;
	}

	public void addStateChangeListener(ParkStateChangeListener listener) {
		listeners.add(listener);
	}

	public void removeStateChangeListener(ParkStateChangeListener listener) {
		listeners.add(listener);
	}

	public synchronized ParkStates getPreviousState() {
		return previousState;
	}

	public synchronized ParkStates getCurrentState() {
		return currentState;
	}
}
