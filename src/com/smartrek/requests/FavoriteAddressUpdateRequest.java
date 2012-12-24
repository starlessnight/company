package com.smartrek.requests;


public final class FavoriteAddressUpdateRequest extends UpdateRequest {

	/**
	 * 
	 * @param aid Address ID
	 * @param uid User ID
	 * @param name Address name
	 * @param address Postal address
	 * @param latitude
	 * @param longitude
	 */
	public FavoriteAddressUpdateRequest(int aid, int uid, String name, String address, double latitude, double longitude) {
		super(String.format("%s/updatefavadd/?fid=%d&uid=%d"));
	}
}
