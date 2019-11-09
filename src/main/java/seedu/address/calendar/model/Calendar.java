package seedu.address.calendar.model;

import seedu.address.calendar.model.date.Date;
import seedu.address.calendar.model.date.MonthOfYear;
import seedu.address.calendar.model.date.ViewOnlyMonth;
import seedu.address.calendar.model.date.Year;
import seedu.address.calendar.model.event.Event;
import seedu.address.calendar.model.event.EventManager;
import seedu.address.calendar.model.event.EventQuery;
import seedu.address.calendar.model.event.exceptions.ClashException;
import seedu.address.calendar.model.event.exceptions.DuplicateEventException;
import seedu.address.calendar.model.util.CalendarStatistics;
import seedu.address.calendar.model.util.DateUtil;
import seedu.address.calendar.model.util.exceptions.NoVacationException;

import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

public class Calendar {
    private ViewOnlyMonth viewOnlyMonth;
    private boolean hasVisibleUpdates;
    private EventManager events;

    public Calendar() {
        java.util.Calendar currentDate = java.util.Calendar.getInstance();

        Year currentYear = new Year(currentDate.get(java.util.Calendar.YEAR));

        int currentUnformattedMonth = currentDate.get(java.util.Calendar.MONTH);
        MonthOfYear currentMonth = DateUtil.convertJavaMonth(currentUnformattedMonth);

        viewOnlyMonth = new ViewOnlyMonth(currentMonth, currentYear);
        hasVisibleUpdates = false;
        events = new EventManager();
    }

    public ViewOnlyMonth getMonth() {
        return ViewOnlyMonth.copy(viewOnlyMonth);
    }

    /**
     * Adds an event to the calendar
     */

    public boolean addEvent(Event event) throws DuplicateEventException, ClashException {
        events.add(event);
        updateMonthView(event);
        return true;
    }

    public boolean addIgnoreClash(Event event) throws DuplicateEventException {
        events.addIgnoreClash(event);
        updateMonthView(event);
        return true;
    }

    /**
     * Deletes an event from the calendar
     */
    public boolean deleteEvent(Event event) throws NoSuchElementException {
        events.remove(event);
        updateMonthView(event);
        return true;
    }

    public String listAll() {
        return events.listAllAsString()
                .stream()
                .reduce("", (prev, curr) -> prev + "\n" + curr)
                .trim();
    }

    public String list(EventQuery eventQuery) {
        return events.listRelevantAsString(eventQuery)
                .stream()
                .reduce("", (prev, curr) -> prev + "\n" + curr)
                .trim();
    }

    public Stream<Event> getEvents(EventQuery eventQuery) {
        return events.getEvents(eventQuery);
    }

    public Stream<Event> getEventsAtSpecificTime(EventQuery eventQuery) {
        return events.getEventsAtSpecificTime(eventQuery);
    }

    // todo: update response message to indicate that the view has also been updated

    public boolean isAvailable(EventQuery eventQuery) {
        boolean isAvailable = events.isAvailable(eventQuery);
        updateMonthView(eventQuery);
        return isAvailable;
    }

    public String suggest(EventQuery eventQuery) {
        String suggestions = events.suggest(eventQuery);
        updateMonthView(eventQuery);
        return suggestions;
    }

    public String suggest(EventQuery eventQuery, int minPeriod) {
        String suggestions = events.suggest(eventQuery, minPeriod);
        updateMonthView(eventQuery);
        return suggestions;
    }

    public ReadOnlyCalendar getCalendar() {
        List<Event> eventsCopy = events.asList();
        return new ReadOnlyCalendar(eventsCopy);
    }

    public void updateCalendar(Optional<ReadOnlyCalendar> readOnlyCalendar) throws NoSuchFileException {
        try {
            List<Event> eventList = readOnlyCalendar.get().getEventList();
            events.clear();
            for (Event event : eventList) {
                addEventFromReadable(event);
            }
        } catch (DuplicateEventException e) {
            throw e;
        } catch (NoSuchElementException e) {
            throw new NoSuchFileException("Data file not found. Will be starting with a sample AddressBook");
        }

        // get current year/month info
        java.util.Calendar currentDate = java.util.Calendar.getInstance();
        Year currentYear = new Year(currentDate.get(java.util.Calendar.YEAR));
        int currentUnformattedMonth = currentDate.get(java.util.Calendar.MONTH);
        MonthOfYear currentMonth = DateUtil.convertJavaMonth(currentUnformattedMonth);

        // update viewable month
        updateMonthView(currentMonth, currentYear);
    }

    private void addEventFromReadable(Event event) throws DuplicateEventException, NoSuchElementException {
        try {
            events.add(event);
        } catch (ClashException e) {
            events.addIgnoreClash(event);
        }
    }

    private void updateMonthView(Event event) {
        EventQuery eventQuery = event.asEventQuery();
        updateMonthView(eventQuery);
    }

    private void updateMonthView(EventQuery eventQuery) {
        ViewOnlyMonth updatedViewOnlyMonth = ViewOnlyMonth.getInstance(events, eventQuery);
        updateMonthView(updatedViewOnlyMonth);
    }

    public void updateMonthView(MonthOfYear month, Year year) {
        Date firstDateOfMonth = DateUtil.getFirstDateInMonth(month, year);
        Date lastDateOfMonth = DateUtil.getLastDateInMonth(month, year);

        EventQuery monthQuery = new EventQuery(firstDateOfMonth, lastDateOfMonth);
        ViewOnlyMonth updatedViewOnlyMonth = ViewOnlyMonth.getInstance(events, monthQuery);
        updateMonthView(updatedViewOnlyMonth);
    }

    private void updateMonthView(ViewOnlyMonth updatedViewOnlyMonth) {
        viewOnlyMonth = updatedViewOnlyMonth;
        hasVisibleUpdates = true;
    }

    /**
     * Checks whether there are any visible update that has to be shown to user.
     *
     * @return {@code true} if and only if there are visible updates that can be shown to user
     */
    public boolean hasVisibleUpdates() {
        return hasVisibleUpdates;
    }

    /**
     * Marks visible update as complete, i.e. latest visible update has been shown to user.
     */
    public void completeVisibleUpdates() {
        hasVisibleUpdates = false;
    }

    /**
     * Gets the number of days of vacation (i.e. school breaks and holidays)
     *
     * @return Number of days of vacation (i.e. school breaks and holidays)
     */
    private long getNumDaysVacation() {
        return events.getNumDaysVacation();
    }

    /**
     * Gets the number of days spent on trips
     *
     * @return Number of days spent on trips
     */
    private long getNumDaysTrip() {
        return events.getNumDaysTrip();
    }

    /**
     * Gets the number of trips.
     *
     * @return Absolute number of trips
     */
    private long getNumTrip() {
        return events.getNumTrip();
    }

    /**
     * Gets the percentage of vacation that is spent on trips.
     *
     * @return Percentage of vacation that is spent on trips
     * @throws NoVacationException If there is no vacation
     */
    private double getPercentageTrip() throws NoVacationException {
        return events.getPercentageTrip();
    }

    public CalendarStatistics getStatistics() {
        return new CalendarStatisticsManager();
    }

    /**
     * Creates a statistics object that contains statistics of {@code this} calendar.
     */
    private class CalendarStatisticsManager implements CalendarStatistics {

        public long getNumDaysVacation() {
            return Calendar.this.getNumDaysVacation();
        }

        public long getNumDaysTrip() {
            return Calendar.this.getNumDaysTrip();
        }

        public long getNumTrip() {
            return Calendar.this.getNumTrip();
        }

        public double getPercentageTrip() throws NoVacationException {
            return Calendar.this.getPercentageTrip();
        }

    }
}
