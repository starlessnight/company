package smartrek.mappers;

import java.util.List;

import smartrek.models.Route;

public final class ReservationMapper extends Mapper {

	public List<Route> getReservations(int uid) {
		return null;
	}
	
	public void reserveRoute(Route route) {
		String url = String.format("%s/addreservations/rid=%d&credits=%d&uid=%d&start_datetime=%s&end_datetime=%s&origin_address=%s&destination_address=%s&route=%s&validated_flag=%d");
	}

}
