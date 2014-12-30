package com.metropia.tasks;

/**
 * Defines a common interface for AsyncTask callback. 
 *
 * @param <Result>
 */
public interface AsyncTaskCallback<Result> {
	public void onPreExecute();
	public void onExecute();
	public void onPostExecute(Result results);
}
