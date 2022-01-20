import javax.swing.plaf.synth.SynthLookAndFeel;
import java.util.*;

public class Presentation {
    public static void main(String[] args) {
        ArrayList<Card> cards;
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            cards = ExcelHandler.getCardsFromExcel("src/BerglasOptimized.xlsx", 0);
            // 苹果
        } else {
            // windows
//            cards = ExcelHandler.getCardsFromExcel("C:\\Users\\anon\\Desktop\\BerglasOptimized.xlsx", 0);
//            cards = ExcelHandler.getCardsFromExcel("src\\BerglasOptimized.xlsx", 0);
            cards = ExcelHandler.getCardsFromExcel("src\\BerglasOptimized.xlsx", 0);
        }
        while (true){
            Scanner input = new Scanner(System.in);
            System.out.println("Say a Point:");
            int point = input.nextInt();

            HashSet<GetCard> getCards = Calc.sayAPoint(point, cards);
            List<Integer> poses = getCards.stream().mapToInt(r -> r.pos).boxed().sorted(Comparator.naturalOrder()).toList();
            System.out.println(poses);

            System.out.println("Say a Position:");
            int pos = input.nextInt();
            Object[] objects = Calc.sayAPosition(pos, getCards, cards);
            if (objects[1] != null) {
                System.out.println(objects[0]);
                System.out.println(objects[1]);
            }else{
                System.out.println("No available position...");
            }
        }
    }

}
