import java.util.Random;

public class GeneratePeople {
    public static class Apartments{
        int people, missingPeople;

        public Apartments(int people, int missingPeople) {
            this.people = people;
            this.missingPeople = missingPeople;
        }
        public Apartments(int people) {
            this.people = people;
            this.missingPeople = 0;
        }
    }
    public static Apartments[][] generatePeople(int FLOORS, int APARTMENTS_PER_FLOOR) {
        final Apartments[][] building = new Apartments[FLOORS][APARTMENTS_PER_FLOOR];
        Random random = new Random();
        for (int floor = 0; floor < FLOORS; floor++) {
            for (int apartment = 0; apartment < APARTMENTS_PER_FLOOR; apartment++) {
                int peopleCount = random.nextInt(6);
                building[floor][apartment] = new Apartments(peopleCount);
            }
        }
        return building;
    }
}
