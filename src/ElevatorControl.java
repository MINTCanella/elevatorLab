import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ElevatorControl implements Runnable {
    private final int ONE_MINUTE = ElevatorGUI.ONE_MINUTE;
    private static final int FLOORS = ElevatorGUI.FLOORS;
    int APARTMENTS_PER_FLOOR = ElevatorGUI.APARTMENTS_PER_FLOOR;
    private int OBJECT_NUMBER;
    JLabel[][] cellPanel;
    private Elevator elevator;

    public static class Elevator {
        int speed, capacity, direction, passengers, floor;
        ArrayList<PeopleControl.People> people = new ArrayList<>();

        private Elevator(int speed, int capacity) {
            this.direction = -1;
            this.passengers = 0;
            this.floor = FLOORS - 1;
            this.speed = speed;
            this.capacity = capacity;
        }
    }

    public ElevatorControl(String elevatorType, JLabel[][] cellPanel) {
        this.cellPanel = cellPanel;
        if (elevatorType.equals("Passenger")) {
            elevator = new Elevator(2, 4);
            this.OBJECT_NUMBER = APARTMENTS_PER_FLOOR + 1;
        } else if (elevatorType.equals("Service")) {
            elevator = new Elevator(1, 6);
            this.OBJECT_NUMBER = APARTMENTS_PER_FLOOR + 2;
        }
        SwingUtilities.invokeLater(() -> cellPanel[FLOORS - 1][OBJECT_NUMBER].setBackground(Color.BLACK));
    }

    public void updateElevator(int floor, int prevFloor) {
        int count = elevator.people.stream().mapToInt(people -> people.count).sum();
        SwingUtilities.invokeLater(() -> cellPanel[floor][OBJECT_NUMBER].setText(String.valueOf(count)));
        SwingUtilities.invokeLater(() -> cellPanel[floor][OBJECT_NUMBER].setBackground(Color.BLACK));
        SwingUtilities.invokeLater(() -> cellPanel[prevFloor][OBJECT_NUMBER].setBackground(new Color(238, 238, 238)));
    }

    public void run() {
        SwingWorker<Object, Object> elevatorWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    if (elevator.direction == -1) {
                        for (int floor = elevator.floor - 1; floor >= 0; floor--) {
                            Thread.sleep(ONE_MINUTE / elevator.speed);
                            elevator.floor = floor;
                            updateElevator(elevator.floor, elevator.floor - elevator.direction);
                        }
                    }
                    if (elevator.direction == 1) {
                        for (int floor = 1; floor < FLOORS; floor++) {
                            Thread.sleep(ONE_MINUTE / elevator.speed);
                            elevator.floor = floor;
                            updateElevator(elevator.floor, elevator.floor - elevator.direction);
                        }
                    }
                    elevator.direction = -elevator.direction;
                }
                return null;
            }

        };
        elevatorWorker.execute();
    }
}
