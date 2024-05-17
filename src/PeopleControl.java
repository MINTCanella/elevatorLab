import javax.swing.*;
import java.util.*;

public class PeopleControl implements Runnable {
    private final int ONE_MINUTE = ElevatorGUI.ONE_MINUTE;
    private final int FLOORS = ElevatorGUI.FLOORS;
    private final int APARTMENTS_PER_FLOOR = ElevatorGUI.APARTMENTS_PER_FLOOR;
    private final int OBJECT_PER_FLOOR = ElevatorGUI.OBJECT_PER_FLOOR;
    private static int LEAVING_PERCENTAGE;
    GeneratePeople.Apartments[][] building;
    JLabel[][] cellPanel;
    JLabel listLabel;
    private final Queue<People>[] waitingList = new ArrayDeque[FLOORS];
    private final Queue<People> waitingUpList = new ArrayDeque<>();
    private final ArrayList<People> missingPeople = new ArrayList<>();
    static final Random random = new Random();


    public static class People {
        int floor, apartments, count, chanceComeBack;

        public People(int floor, int apartments, int count) {
            this.floor = floor;
            this.apartments = apartments;
            this.count = count;
            this.chanceComeBack = random.nextInt(10);
        }

        public People(People person, int count) {
            this.floor = person.floor;
            this.apartments = person.apartments;
            this.count = count;
            this.chanceComeBack = random.nextInt(10);
        }
    }

    public PeopleControl(GeneratePeople.Apartments[][] building, JLabel[][] cellPanel, JLabel listLabel) {
        this.building = building;
        this.cellPanel = cellPanel;
        this.listLabel = listLabel;
        for (int i = 0; i < FLOORS; i++) {
            waitingList[i] = new ArrayDeque<>();
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

    public void updateCellPanel(int floor) {
        SwingUtilities.invokeLater(() -> cellPanel[floor][APARTMENTS_PER_FLOOR].setText(String.valueOf(waitingList[floor].size())));
    }

    public void updateWaitingUpList() {
        int count = waitingUpList.stream().mapToInt(missingPerson -> missingPerson.count).sum();
        SwingUtilities.invokeLater(() -> cellPanel[0][OBJECT_PER_FLOOR - 1].setText(String.valueOf(count)));
    }

    public void updateList(ArrayList<People> missingPeople) {
        StringBuilder people = new StringBuilder();
        for (People missingPerson : missingPeople) {
            people.append(missingPerson.floor).append("-").append(missingPerson.apartments).append("(").append(missingPerson.count).append("), ");
        }
        SwingUtilities.invokeLater(() -> listLabel.setText(String.valueOf(people)));
    }

    public void run() {
        SwingWorker<Object, Object> peopleWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    Thread.sleep(ONE_MINUTE * 10);
                    // Имитация выхода людей из квартиры в подъезд
                    for (int floor = 0; floor < FLOORS; floor++) {
                        for (int apartement = 0; apartement < APARTMENTS_PER_FLOOR; apartement++) {
                            if (random.nextInt(100) <= LEAVING_PERCENTAGE / 5 && building[floor][apartement].people > 0) {
                                int peopleLeave = 1 + random.nextInt(building[floor][apartement].people);
                                building[floor][apartement].people -= peopleLeave;
                                building[floor][apartement].missingPeople += peopleLeave;
                                waitingList[floor].offer(new People(floor, apartement, peopleLeave));
                                updateCellPanel(floor, apartement);
                            }
                        }
                    }
                    // Люди с 0 этажа сразу могут выйти на улицу
                    for (int people = 0; people < waitingList[0].size(); people++) {
                        missingPeople.add(waitingList[0].poll());
                    }
                    updateList(missingPeople);
                    updateCellPanel(0);
                    // Имитация прихода людей к себе в дом
                    for (int i = missingPeople.size() - 1; i >= 0; i--) {
                        People missingPerson = missingPeople.get(i);
                        if (random.nextInt(100) <= missingPerson.chanceComeBack) {
                            int peopleLeave = 1 + random.nextInt(missingPerson.count);
                            if (missingPerson.floor == 0) {
                                building[0][missingPerson.apartments].people += peopleLeave;
                                building[0][missingPerson.apartments].missingPeople -= peopleLeave;
                                updateCellPanel(0, missingPerson.apartments);
                            } else waitingUpList.add(new People(missingPerson, peopleLeave));
                            missingPerson.count -= peopleLeave;
                            if (missingPerson.count == 0) missingPeople.remove(i);
                        } else missingPerson.chanceComeBack++;
                    }
                    updateWaitingUpList();
                }
                return null;
            }

        };
        peopleWorker.execute();
    }

}
