import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ElevatorGUI extends JFrame {

    // Данные для таймера
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH.mm");
    private static final LocalTime time = LocalTime.of(12, 0);

    // Данные для жилья
    private static final int FLOORS = 20;
    private static final int APARTMENTS_PER_FLOOR = 4;
    private static final int OBJECT_PER_FLOOR = APARTMENTS_PER_FLOOR + 3;

    // Графические данные
    MatteBorder border = new MatteBorder(1, 1, 1, 1, Color.BLACK);
    MatteBorder downBorder = new MatteBorder(0, 0, 1, 0, Color.BLACK);

    public ElevatorGUI() {
        // Общие настройки графического интерфейса
        setTitle("Жилой дом");
        setSize(650, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        setLayout(new BorderLayout());

        // Расположение таймера
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel timeLabel = new JLabel(time.format(TIME_FORMATTER), SwingConstants.CENTER);
        topRightPanel.add(timeLabel);
        add(topRightPanel, BorderLayout.NORTH);


        // Расположение квартир
        JPanel mainPanel = new JPanel(new GridLayout(20, 1, 0, 0));
        JLabel[][] cellPanel = new JLabel[FLOORS][OBJECT_PER_FLOOR];
        for (int floor = FLOORS - 1; floor >= 0; floor--) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            for (int apartment = 0; apartment < APARTMENTS_PER_FLOOR; apartment++) {
                cellPanel[floor][apartment] = new JLabel("", SwingConstants.CENTER);
                cellPanel[floor][apartment].setBorder(border);
                cellPanel[floor][apartment].setPreferredSize(new Dimension(75, 20));
                rowPanel.add(cellPanel[floor][apartment]);
            }
            for (int apartment = APARTMENTS_PER_FLOOR; apartment < OBJECT_PER_FLOOR; apartment++) {
                cellPanel[floor][apartment] = new JLabel("", SwingConstants.CENTER);
                cellPanel[floor][apartment].setBorder(downBorder);
                cellPanel[floor][apartment].setPreferredSize(new Dimension(50, 20));
                rowPanel.add(cellPanel[floor][apartment]);
            }
            mainPanel.add(rowPanel);
        }

        // Генерация жильцов
        GeneratePeople.Apartments[][] building = GeneratePeople.generatePeople(FLOORS, APARTMENTS_PER_FLOOR);
        for (int floor = 0; floor < FLOORS; floor++) {
            for (int apartment = 0; apartment < APARTMENTS_PER_FLOOR; apartment++) {
                cellPanel[floor][apartment].setText(building[floor][apartment].people + " | " + building[floor][apartment].missingPeople);
            }
            cellPanel[floor][APARTMENTS_PER_FLOOR].setText("0");

        }
        add(mainPanel, BorderLayout.CENTER);

        // Запуск таймера
        new Thread(new Timepiece(timeLabel, TIME_FORMATTER, time)).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ElevatorGUI::new);
    }
}
