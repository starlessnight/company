package smartrek.mappers;

public class ReservationCommunicator extends ServerCommunicator {

	@Override
	protected String appendToUrl() {
		return "/reservation";
	}

}
