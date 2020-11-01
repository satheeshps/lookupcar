package com.trolp.lookupcar.utils;

public interface CompleteListener<S,F> {
	public void onSuccess(S e);
	public void onFailure(F e);
}
