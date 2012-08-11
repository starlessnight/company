package com.smartrek.requests;


public abstract class FetchRequest<ReturnType> extends Request {
	
	public interface Listener<ReturnType> {
		void onFinish(ReturnType result);
	}

	protected Listener<ReturnType> listener;
	
	public void setListener(Listener<ReturnType> listener) {
		this.listener = listener;
	}
	
	public ReturnType fetch() {
		return null;
	}

}