import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class timestamp {

    public static void main(String[] args) throws InterruptedException {
        Timestamp ts = Timestamp.from(Instant.now());
        TimeUnit.SECONDS.sleep(5);
        Timestamp ts2 = Timestamp.from(Instant.now());
        System.out.println(ts2.getTime() - ts.getTime()); // Milliseconds
    }

}
