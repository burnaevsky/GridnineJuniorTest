package com.gridnine.testing;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

class Program{
    public static void main(String[] args) {
        ArrayList<Flight> flights1 = new ArrayList<>(FlightBuilder.createFlights());
        ArrayList<Flight> flights2 = new ArrayList<>(FlightBuilder.createFlights());
        ArrayList<Flight> flights3 = new ArrayList<>(FlightBuilder.createFlights());

        FlightFilter.upToTheCurrentTime(flights1);
        FlightFilter.arrivalEarlierThanDeparture(flights2);
        FlightFilter.landingTimeExceedsTwoHours(flights3);

        System.out.println(flights1);
        System.out.println("------");
        System.out.println(flights2);
        System.out.println("------");
        System.out.println(flights3);

    }
}

class FlightFilter{
    /**
     * Filter1: departure up to the current time
     */
    static void upToTheCurrentTime(List<Flight> flights){
        LocalDateTime currentDateTime = LocalDateTime.now();
        for (int i = 0; i < flights.size(); i++) {
            for(Segment segment : flights.get(i).getSegments()){
                if(segment.getDepartureDate().isBefore(currentDateTime)){
                    flights.remove(i);
                }
            }
        }
    }

    /**
     * Filter2: having segments with an arrival date earlier than the departure date
     */
    static void arrivalEarlierThanDeparture(List<Flight> flights){
        for (int i = 0; i < flights.size(); i++) {
            for(Segment segment : flights.get(i).getSegments()){
                if(segment.getArrivalDate().isBefore(segment.getDepartureDate()))
                    flights.remove(i);
            }
        }
    }

    /**
     * Filter3: the total time spent on the ground exceeds two hours
     */
    static void landingTimeExceedsTwoHours(List<Flight> flights){
        for (int i = 0; i < flights.size(); i++) {
            long landingTime = 0;
            if(flights.get(i).getSegments().size() < 2){
                continue;
            }
            else {
                for (int j = 1; j < flights.get(i).getSegments().size(); j++) {
                    long l1 = flights.get(i).getSegments().get(j - 1).getArrivalDate().toInstant(ZoneOffset.ofTotalSeconds(0)).getEpochSecond();
                    long l2 = flights.get(i).getSegments().get(j).getDepartureDate().toInstant(ZoneOffset.ofTotalSeconds(0)).getEpochSecond();
                    landingTime += l2 - l1;
                }
            }
            if(landingTime >= 7200){
                flights.remove(i);
            }
        }
    }
}

/**
 * Factory class to get sample list of flights.
 */
class FlightBuilder {


    static List<Flight> createFlights() {
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        return Arrays.asList(
            //A normal flight with two hour duration
            createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2)),
            //A normal multi segment flight
            createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(5)),
            //A flight departing in the past
            createFlight(threeDaysFromNow.minusDays(6), threeDaysFromNow),
            //A flight that departs before it arrives
            createFlight(threeDaysFromNow, threeDaysFromNow.minusHours(6)),
            //A flight with more than two hours ground time
            createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                threeDaysFromNow.plusHours(5), threeDaysFromNow.plusHours(6)),
            //Another flight with more than two hours ground time
            createFlight(threeDaysFromNow, threeDaysFromNow.plusHours(2),
                threeDaysFromNow.plusHours(3), threeDaysFromNow.plusHours(4),
                threeDaysFromNow.plusHours(6), threeDaysFromNow.plusHours(7)));
    }

    private static Flight createFlight(final LocalDateTime... dates) {
        if ((dates.length % 2) != 0) {
            throw new IllegalArgumentException(
                "you must pass an even number of dates");
        }
        List<Segment> segments = new ArrayList<>(dates.length / 2);
        for (int i = 0; i < (dates.length - 1); i += 2) {
            segments.add(new Segment(dates[i], dates[i + 1]));
        }
        return new Flight(segments);
    }
}

/**
 * Bean that represents a flight.
 */
class Flight {
    private final List<Segment> segments;

    Flight(final List<Segment> segs) {
        segments = segs;
    }

    List<Segment> getSegments() {
        return segments;
    }

    @Override
    public String toString() {
        return segments.stream().map(Object::toString)
            .collect(Collectors.joining(" "));
    }
}

/**
 * Bean that represents a flight segment.
 */
class Segment {
    private final LocalDateTime departureDate;

    private final LocalDateTime arrivalDate;

    Segment(final LocalDateTime dep, final LocalDateTime arr) {
        departureDate = Objects.requireNonNull(dep);
        arrivalDate = Objects.requireNonNull(arr);
    }

    LocalDateTime getDepartureDate() {
        return departureDate;
    }

    LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return '[' + departureDate.format(fmt) + '|' + arrivalDate.format(fmt)
            + ']';
    }
}