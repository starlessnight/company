package smartrek.mappers;

public class ReservationMapper extends Mapper {

	@Override
	protected String appendToUrl() {
		return "/getreservation";
	}

}
