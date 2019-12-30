package roberta.heartbeep.Utilities;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.Calendar;

public class Utils {

    public static String getCurrentTime(){
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + " " + c.get(Calendar.DATE) + "/" + (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.YEAR);
    }

    public static LocalDate getCurrentWeekStart(){
        LocalDate date = LocalDate.now();
        date = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return date;
    }

    public static String timeToString(LocalDateTime time){
        return time.format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"));
    }

}
