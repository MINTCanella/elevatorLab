import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

public class PeopleControl implements Runnable {
    private final int ONE_MINUTE = ElevatorGUI.ONE_MINUTE;
    private static final int FLOORS = ElevatorGUI.FLOORS;
    private static final int APARTMENTS_PER_FLOOR = ElevatorGUI.APARTMENTS_PER_FLOOR;
    private final int OBJECT_PER_FLOOR = ElevatorGUI.OBJECT_PER_FLOOR;
    private static int LEAVING_PERCENTAGE;
    static GeneratePeople.Apartments[][] building;
    static JLabel[][] cellPanel;
    static JLabel listLabel;
    private static final Queue<People>[] waitingList = new ConcurrentLinkedQueue[FLOORS];
    private static final Queue<People> waitingUpList = new ConcurrentLinkedQueue<>();
    private static final ArrayList<Integer> passengerCallOutside = new ArrayList<>();
    private static final ArrayList<Integer> serviceCallOutside = new ArrayList<>();
    private static final ArrayList<People> missingPeople = new ArrayList<>();
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
        PeopleControl.building = building;
        PeopleControl.cellPanel = cellPanel;
        this.listLabel = listLabel;
        for (int i = 0; i < FLOORS; i++) {
            waitingList[i] = new ConcurrentLinkedQueue<>();
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

    public static int getListCount(Queue<People> waitingList) {
        return waitingList.stream().mapToInt(missingPerson -> missingPerson.count).sum();
    }

    public static synchronized Queue<People> getWaitingList(int floor) {
        return waitingList[floor];
    }

    public static synchronized Queue<People> getWaitingUpList() {
        return waitingUpList;
    }

    public static void updateWaitingList(int floor, Queue<People> list) {
        waitingList[floor] = new ConcurrentLinkedQueue<>(list);
        updateCellPanel(floor);
    }


    public static synchronized ArrayList<Integer> callOutside(String type) {
        if (type.equals("Passenger")) return passengerCallOutside;
        else return serviceCallOutside;
    }

    public static void updateCellPanel(int floor, int apartments) {
        SwingUtilities.invokeLater(() -> cellPanel[floor][apartments].setText(building[floor][apartments].people + " | " + building[floor][apartments].missingPeople));
        SwingUtilities.invokeLater(() -> cellPanel[floor][APARTMENTS_PER_FLOOR].setText(String.valueOf(getListCount(waitingList[floor]))));
    }

    public static void updateCellPanel(int floor) {
        SwingUtilities.invokeLater(() -> cellPanel[floor][APARTMENTS_PER_FLOOR].setText(String.valueOf(getListCount(waitingList[floor]))));
    }

    public void updateWaitingUpCell() {
        SwingUtilities.invokeLater(() -> cellPanel[0][OBJECT_PER_FLOOR - 1].setText(String.valueOf(getListCount(waitingUpList))));
    }

    public static void updateListCell(ArrayList<People> missingPeople) {
        StringBuilder people = new StringBuilder();
        for (People missingPerson : missingPeople) {
            people.append(missingPerson.floor).append("-").append(missingPerson.apartments).append("(").append(missingPerson.count).append("), ");
        }
        SwingUtilities.invokeLater(() -> listLabel.setText(String.valueOf(people)));
    }

    public static void goingOutside(Queue<People> waitingList) {
        int size = waitingList.size();
        for (int people = 0; people < size; people++) {
            missingPeople.add(waitingList.poll());
        }
        updateListCell(missingPeople);
    }

    public static synchronized void unCallingElevator(int floor, String type) {
        if (type.equals("Passenger")) passengerCallOutside.removeAll(Collections.singleton(floor));
        else if (type.equals("Service")) serviceCallOutside.removeAll(Collections.singleton(floor));
    }

    public static synchronized void callingElevator(int floor, String type) {
        if (floor == 0) return;
        if (type.equals("Passenger") && !passengerCallOutside.contains(floor)) passengerCallOutside.add(floor);
        else if (type.equals("Service") && !serviceCallOutside.contains(floor)) serviceCallOutside.add(floor);
    }

    // Имитация выхода людей из квартиры в подъезд
    public synchronized void apartmentsLeaving() {
        for (int floor = 0; floor < FLOORS; floor++) {
            for (int apartement = 0; apartement < APARTMENTS_PER_FLOOR; apartement++) {
                if (random.nextInt(100) <= LEAVING_PERCENTAGE / 5 && building[floor][apartement].people > 0) {
                    int peopleLeave = 1 + random.nextInt(building[floor][apartement].people);
                    building[floor][apartement].people -= peopleLeave;
                    building[floor][apartement].missingPeople += peopleLeave;
                    waitingList[floor].add(new People(floor, apartement, peopleLeave));
                    updateCellPanel(floor, apartement);
                    callingElevator(floor, "Passenger");
                }
            }
        }
        // Люди с 0 этажа сразу могут выйти на улицу
        goingOutside(waitingList[0]);
        updateCellPanel(0);
    }

    // Имитация прихода людей к себе в дом
    public synchronized void houseComing() {
        for (int i = missingPeople.size() - 1; i >= 0; i--) {
            People missingPerson = missingPeople.get(i);
            if (random.nextInt(100) <= missingPerson.chanceComeBack) {
                int peopleLeave = 1 + random.nextInt(missingPerson.count);
                if (missingPerson.floor == 0) apartmentsComing(0, missingPerson.apartments, peopleLeave);
                else waitingUpList.add(new People(missingPerson, peopleLeave));
                missingPerson.count -= peopleLeave;
                if (missingPerson.count == 0) missingPeople.remove(i);
            } else missingPerson.chanceComeBack++;
        }
        assert waitingUpList.peek() != null;
        updateWaitingUpCell();
    }

    // Имитация прихода людей в квартиру
    public static synchronized void apartmentsComing(int floor, int apartments, int count) {
        building[floor][apartments].people += count;
        building[floor][apartments].missingPeople -= count;
        updateCellPanel(floor, apartments);
    }

    public void run() {
        SwingWorker<Object, Object> peopleWorker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(ONE_MINUTE * 5);
                while (!isCancelled()) {
                    try {
                        apartmentsLeaving();
                    } catch (Exception ignored) {
                    }
                    try {
                        houseComing();
                    } catch (Exception ignored) {
                    }
                    Thread.sleep(ONE_MINUTE * 30);
                }
                return null;
            }

        };
        peopleWorker.execute();
    }

}
