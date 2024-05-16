import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Timepiece implements Runnable {
    private final DateTimeFormatter TIME_FORMATTER = ElevatorGUI.TIME_FORMATTER;
    private final JLabel timeLabel;
    private LocalTime time;
    private final int ONE_MINUTE = ElevatorGUI.ONE_MINUTE;

    public Timepiece(JLabel timeLabel, LocalTime time) {
        this.timeLabel = timeLabel;
        this.time = time;
    }

    public void run() {
        SwingWorker<Void, LocalTime> timerWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    if (time.getMinute() == 0) {
                        PeopleControl.changePercentage(time.getHour());
                    }
                    time = time.plusMinutes(1);
                    if (time.equals(LocalTime.MAX)) {
                        time = LocalTime.MIN;
                    }
                    publish(time);
                    Thread.sleep(ONE_MINUTE);
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