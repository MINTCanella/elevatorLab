import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Collections.max;


public class ElevatorControl implements Runnable {
    private final int ONE_MINUTE = ElevatorGUI.ONE_MINUTE;
    private static final int FLOORS = ElevatorGUI.FLOORS;
    int APARTMENTS_PER_FLOOR = ElevatorGUI.APARTMENTS_PER_FLOOR;
    private int OBJECT_NUMBER;
    JLabel[][] cellPanel;
    private Elevator elevator;
    private final String type;
    private static final ArrayList<Integer> callInsidePassenger = new ArrayList<>();
    private static final ArrayList<Integer> callInsideService = new ArrayList<>();
    int maxCallFloor;


    public static class Elevator {
        int speed, capacity, direction, floor;
        Queue<PeopleControl.People> people = new ConcurrentLinkedQueue<>();

        private Elevator(int speed, int capacity) {
            this.direction = 0;
            this.floor = FLOORS - 1;
            this.speed = speed;
            this.capacity = capacity;
        }
    }

    public ElevatorControl(String elevatorType, JLabel[][] cellPanel) {
        this.cellPanel = cellPanel;
        this.type = elevatorType;
        if (type.equals("Passenger")) {
            elevator = new Elevator(2, 4);
            this.OBJECT_NUMBER = APARTMENTS_PER_FLOOR + 1;
        } else if (type.equals("Service")) {
            elevator = new Elevator(1, 15);
            this.OBJECT_NUMBER = APARTMENTS_PER_FLOOR + 2;
        }
        SwingUtilities.invokeLater(() -> cellPanel[FLOORS - 1][OBJECT_NUMBER].setBackground(Color.BLACK));
    }

    public static ArrayList<Integer> getCallInside(String type) {
        if (type.equals("Passenger")) return callInsidePassenger;
        return callInsideService;
    }

    public int getElevatorCount() {
        return elevator.people.stream().mapToInt(people -> people.count).sum();
    }

    public void updateElevator(int floor, int prevFloor) {
        SwingUtilities.invokeLater(() -> cellPanel[floor][OBJECT_NUMBER].setText(String.valueOf(getElevatorCount())));
        SwingUtilities.invokeLater(() -> cellPanel[prevFloor][OBJECT_NUMBER].setBackground(new Color(238, 238, 238)));
        SwingUtilities.invokeLater(() -> cellPanel[floor][OBJECT_NUMBER].setBackground(Color.BLACK));
    }

    // Взять людей с этажа
    public void getPeople(int floor, int listType) {
        Queue<PeopleControl.People> waitingList;
        if (listType == 0) waitingList = PeopleControl.getWaitingList(floor);
        else waitingList = PeopleControl.getWaitingUpList();
        int size = waitingList.size();
        for (int person = 0; person < size; person++) {
            if (getElevatorCount() >= elevator.capacity) {
                if (type.equals("Passenger")) {
                    PeopleControl.callingElevator(elevator.floor, "Service");
                }
                break;
            }
            assert waitingList.peek() != null;
            elevator.people.add(new PeopleControl.People(waitingList.peek().floor, waitingList.peek().apartments, waitingList.peek().count));
            waitingList.poll();
        }
        if (waitingList.isEmpty()) {
            PeopleControl.unCallingElevator(floor, type);
        }
        PeopleControl.updateWaitingList(floor, waitingList);
    }

    // Проверка вызовов
    public void callChecking() {
        ArrayList<Integer> callOutsideList = PeopleControl.callOutside(type);
        if (elevator.floor == 0 && !PeopleControl.getWaitingUpList().isEmpty()) {
            getPeople(elevator.floor, 1);
            for (PeopleControl.People people : elevator.people) {
                getCallInside(type).add(people.floor);
            }
            elevator.direction = 1;
            maxCallFloor = max(getCallInside(type));
        } else if (callOutsideList.contains(elevator.floor)) {
            elevator.direction = -1;
            getPeople(elevator.floor, 0);
            PeopleControl.unCallingElevator(elevator.floor, type);
        } else if (!callOutsideList.isEmpty()) {
            maxCallFloor = max(callOutsideList);
            if (maxCallFloor > elevator.floor) elevator.direction = 1;
            else elevator.direction = -1;
        }
    }

    // Спуск лифта
    public void downElevator(int callFloor) throws InterruptedException {
        for (int floor = elevator.floor - 1; floor >= callFloor; floor--) {
            elevator.floor = floor;
            Thread.sleep(ONE_MINUTE / elevator.speed);
            Queue<PeopleControl.People> callOutsideList = PeopleControl.getWaitingList(floor);
            if (!callOutsideList.isEmpty()) {
                updateElevator(elevator.floor, elevator.floor - elevator.direction);
                getPeople(floor, 0);
                PeopleControl.unCallingElevator(floor, type);
                Thread.sleep(ONE_MINUTE);
            }
            updateElevator(elevator.floor, elevator.floor - elevator.direction);
        }
        elevator.direction = 0;
    }

    // Подъем лифта
    public void upElevator(int callFloor) throws InterruptedException {
        for (int floor = elevator.floor + 1; floor <= callFloor; floor++) {
            Thread.sleep(ONE_MINUTE / elevator.speed);
            if (getCallInside(type).contains(floor)) {
                for (PeopleControl.People person : elevator.people) {
                    if (floor == person.floor) {
                        PeopleControl.apartmentsComing(person.floor, person.apartments, person.count);
                    }
                }
                getCallInside(type).removeAll(Collections.singleton(floor));
                Thread.sleep(ONE_MINUTE);
            }
            elevator.floor = floor;
            updateElevator(elevator.floor, elevator.floor - elevator.direction);
        }
        elevator.direction = 0;
    }

    public void run() {
        SwingWorker<Object, Object> elevatorWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    if (getCallInside(type).isEmpty()) {
                        if (elevator.direction == 0) {
                            Thread.sleep(ONE_MINUTE);
                            callChecking();
                        }
                        if (elevator.direction == -1) {
                            downElevator(maxCallFloor);
                            getCallInside(type).add(0);

                        } else if (elevator.direction == 1) {
                            try {
                                upElevator(maxCallFloor);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                    if (getCallInside(type).contains(0)) {
                        elevator.direction = -1;
                        downElevator(0);
                        PeopleControl.goingOutside(elevator.people);
                        updateElevator(elevator.floor, elevator.floor - elevator.direction);
                        getCallInside(type).removeAll(Collections.singleton(0));

                    }
                }
                return null;
            }

        };
        elevatorWorker.execute();
    }
}
