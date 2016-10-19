package org.tickets.railway;


import java.time.LocalDate;
import java.util.List;

public interface TrainsCriteria {

    String forUser();

    String fromStation();

    String toStation();

    List<LocalDate> arriveAt();
    
}
