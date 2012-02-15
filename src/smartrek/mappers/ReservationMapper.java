package smartrek.mappers;

public class ReservationMapper extends ServerCommunicator {

	@Override
	protected String appendToUrl() {
		return "/getreservation";
	}

}
