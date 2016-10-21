package org.tickets.misc;


import java.time.LocalDate;
import java.util.Comparator;

public final class LocalDateComparator {
    private LocalDateComparator() {}

    public static final Comparator<LocalDate> COMPARATOR =
            Comparator.comparing(LocalDate::getYear)
                    .thenComparing(LocalDate::getDayOfYear);
}
