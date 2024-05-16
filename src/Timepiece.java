import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Timepiece implements Runnable {
    private final DateTimeFormatter TIME_FORMATTER;
    private LocalTime time;

    private final JLabel timeLabel;

    public Timepiece(JLabel timeLabel, DateTimeFormatter TIME_FORMATTER, LocalTime time) {
        this.timeLabel = timeLabel;
        this.TIME_FORMATTER = TIME_FORMATTER;
        this.time = time;
    }

    public void run() {
        SwingWorker<Void, LocalTime> timerWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    time = time.plusMinutes(1);
                    if (time.equals(LocalTime.MAX)) {
                        time = LocalTime.MIN;
                    }
                    publish(time);
                    Thread.sleep(500);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<LocalTime> chunks) {
                LocalTime latestTime = chunks.getLast();
                SwingUtilities.invokeLater(() -> timeLabel.setText(latestTime.format(TIME_FORMATTER)));
            }
        };

        timerWorker.execute();
        }

}