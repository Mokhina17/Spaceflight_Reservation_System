package model;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Reservation {

    // ids
    private int reservationId;
    private int companyId;      // changed from cinemaId
    private int flightId;       // changed from movieId
    private int flightDateId;   // changed from datePlaytimeId

    // reservations attributes
    private String company;     // changed from cinema
    private String flight;      // changed from movie
    private String launchTime;  // changed from time
    private int reservedSeats;
    private Date date;
    private List<Integer> seatNumbers; // New parameter for seat numbers

    // Updated Constructor
    public Reservation(int reservationId, int companyId, int flightId, int flightDateId,
                       String company, String flight, String launchTime, int reservedSeats, Date date, List<Integer> seatNumbers) {
        super();
        this.reservationId = reservationId;
        this.companyId = companyId;
        this.flightId = flightId;
        this.flightDateId = flightDateId;
        this.company = company;
        this.flight = flight;
        this.launchTime = launchTime;
        this.reservedSeats = reservedSeats;
        this.date = date;
        this.seatNumbers = seatNumbers;
    }

    public static String[] getVariableNames() {
        return new String[]{"date", "launchTime", "flight", "company", "reservedSeats", "seatNumbers"};
    }

    // GETTER and SETTER
    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public int getFlightDateId() {
        return flightDateId;
    }

    public void setFlightDateId(int flightDateId) {
        this.flightDateId = flightDateId;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getFlight() {
        return flight;
    }

    public void setFlight(String flight) {
        this.flight = flight;
    }

    public String getLaunchTime() {
        return launchTime;
    }

    public void setLaunchTime(String launchTime) {
        this.launchTime = launchTime;
    }

    public int getReservedSeats() {
        return reservedSeats;
    }

    public void setReservedSeats(int reservedSeats) {
        this.reservedSeats = reservedSeats;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    // Getter for seatNumbers
    public String getSeatNumbers() {
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            return "No seats selected"; // Handle null or empty list
        }

        return seatNumbers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    // Setter for seatNumbers
    public void setSeatNumbers(List<Integer> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    // Updated toString Method
    @Override
    public String toString() {
        return "Reservation [reservationId=" + reservationId + ", companyId=" + companyId + ", flightId=" + flightId
                + ", flightDateId=" + flightDateId + ", company=" + company + ", flight=" + flight + ", launchTime=" + launchTime
                + ", reservedSeats=" + reservedSeats + ", date=" + date + ", seatNumbers=" + seatNumbers + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Reservation other = (Reservation) obj;
        if (company == null) {
            if (other.company != null)
                return false;
        } else if (!company.equals(other.company))
            return false;
        if (companyId != other.companyId)
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (flightDateId != other.flightDateId)
            return false;
        if (flight == null) {
            if (other.flight != null)
                return false;
        } else if (!flight.equals(other.flight))
            return false;
        if (flightId != other.flightId)
            return false;
        if (reservationId != other.reservationId)
            return false;
        if (reservedSeats != other.reservedSeats)
            return false;
        if (launchTime == null) {
            return other.launchTime == null;
        } else return launchTime.equals(other.launchTime);
    }
}