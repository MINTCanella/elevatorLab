import javax.swing.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class PeopleControl implements Runnable {
    private final int ONE_MINUTE = ElevatorGUI.ONE_MINUTE;
    private final int FLOORS = ElevatorGUI.FLOORS;
    private final int APARTMENTS_PER_FLOOR = ElevatorGUI.APARTMENTS_PER_FLOOR;
    //private final int OBJECT_PER_FLOOR = ElevatorGUI.OBJECT_PER_FLOOR;
    private static int LEAVING_PERCENTAGE;
    GeneratePeople.Apartments[][] building;
    JLabel[][] cellPanel;
    private final Queue<Integer[]>[] waitingList = new LinkedList[FLOORS];

    public PeopleControl(GeneratePeople.Apartments[][] building, JLabel[][] cellPanel) {
        this.building = building;
        this.cellPanel = cellPanel;
        for (int i = 0; i < FLOORS; i++) {
            waitingList[i] = new LinkedList<>();
        }
    }

    public static void changePercentage(int hours) {
        switch (hours) {
            case 0, 1, 2, 3, 4, 5, 22, 23:
                LEAVING_PERCENTAGE = 5;
                break;
            case 18, 19, 20, 21:
                LEAVING_PERCENTAGE = 10;
                break;
            case 6:
                LEAVING_PERCENTAGE = 20;
                break;
            case 10, 11, 12, 13:
                LEAVING_PERCENTAGE = 40;
                break;
            case 7, 9, 14, 15, 16, 17:
                LEAVING_PERCENTAGE = 50;
                break;
            case 8:
                LEAVING_PERCENTAGE = 60;
                break;
        }
    }

    public void updateCellPanel(int floor, int apartments) {
        SwingUtilities.invokeLater(() -> cellPanel[floor][apartments].setText(building[floor][apartments].people + " | " + building[floor][apartments].missingPeople));
        SwingUtilities.invokeLater(() -> cellPanel[floor][APARTMENTS_PER_FLOOR].setText(String.valueOf(waitingList[floor].size())));
    }

    public void run() {
        SwingWorker<Object, Object> peopleWorker = new SwingWorker<>() {
            final Random random = new Random();

            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    Thread.sleep(ONE_MINUTE * 10);
                    for (int floor = 0; floor < FLOORS; floor++) {
                        for (int apartement = 0; apartement < APARTMENTS_PER_FLOOR; apartement++) {
                            if (random.nextInt(100) <= LEAVING_PERCENTAGE / 5 && building[floor][apartement].people > 0) {
                                building[floor][apartement].people--;
                                building[floor][apartement].missingPeople++;
                                Integer[] indexes = new Integer[]{floor, apartement};
                                waitingList[floor].add(indexes);
                                updateCellPanel(floor, apartement);
                            }
                        }
                    }
                }
                return null;
            }

        };
        peopleWorker.execute();
    }

}
