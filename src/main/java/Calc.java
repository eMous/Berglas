
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Calc {
    public static void main(String[] args) {
//        ArrayList<Card> cards = ExcelHandler.getCardsFromExcel("C:\\Users\\anon\\Desktop\\Berglas.xlsx");
        ArrayList<Card> cards = ExcelHandler.getCardsFromExcel("src\\BerglasOptimized.xlsx", 0);
        HashSet<GetCard> getCards = sayAPoint(7, cards);
        List<Integer> poses = getCards.stream().mapToInt(r -> r.pos).boxed().sorted(Comparator.naturalOrder()).toList();
        System.out.println(poses);

        Object[] objects = sayAPosition(3, getCards, cards);
        if (objects[1] != null) {
            System.out.println(objects[0]);
            System.out.println(objects[1]);
        }

//
//        cards = ExcelHandler.getCardsFromExcel("C:\\Users\\anon\\Desktop\\BerglasOptimized.xlsx", 1);
//        System.out.println("Test My Optimized 9 40");
//        benchMark(cards, false);
//        benchMark(cards, true);
//        System.out.println("");
//
//        cards = ExcelHandler.getCardsFromExcel("C:\\Users\\anon\\Desktop\\BerglasOptimized.xlsx", 1);
//        System.out.println("Test My Optimized 10 39");
//        benchMark(cards, false);
//        benchMark(cards, true);
//        System.out.println("");
//
//        System.out.println("Test Bilibili");
//        cards = ExcelHandler.getCardsFromExcel("C:\\Users\\anon\\Desktop\\Berglas.xlsx", 0);
//        benchMark(cards, false);
//        benchMark(cards, true);
//        System.out.println("");


//        HashSet getCardSet = sayAPoint(6, cards);
//        System.out.println(positionsByPoint(getCardSet));
//
//        Object[] objs = sayAPosition(8,getCardSet,cards);
//        if (objs[0] != null && objs[1] != null){
//            System.out.println("\n"+ objs[0] + "\n"+ objs[1]);
//        }

    }

    public static TreeSet<GetCard> positionsByPoint(HashSet<GetCard> getCardSet) {
        TreeSet<GetCard> result = new TreeSet(getCardSet.stream().map(gC -> gC.pos).collect(Collectors.toSet()));
        return result;
    }

    public static HashSet<GetCard> sayAPoint(int point, List<Card> cardsDeck) {
        HashSet<GetCard> ret = new HashSet<>();
        if (point < 1 && point > 13) {
            return null;
        }

        int pointCardsNumber = Card.getPointCardsNumber(new ArrayList<Card>(cardsDeck));
        for (Card.Color color : Card.pointColor) {
            // 背有鬼
            int posNormalYG = cardsDeck.indexOf(Card.newCard(point, color)) + 1;
            if (Card.getTopGASize(new ArrayList<>(cardsDeck)) > 0) {
                if (posNormalYG >= 1 && posNormalYG <= pointCardsNumber) {
                    GetCard getCard = new GetCard(GetCard.Method.NORMAL_YG, posNormalYG);
//                if (!ret.stream().anyMatch(g->g.pos == posNormalYG)){
                    ret.add(getCard);
//                }
                }
            }
            // 背有鬼 Next
            if (Card.getTopGASize(new ArrayList<>(cardsDeck)) > 0) {
                int posNormalYGNext = posNormalYG - 1;
                if (posNormalYGNext >= 1 && posNormalYGNext <= pointCardsNumber) {
                    GetCard getCard = new GetCard(GetCard.Method.NORMAL_YG_NEXT, posNormalYGNext);
//                if (!ret.stream().anyMatch(g->g.pos == posNormalYGNext)){
                    ret.add(getCard);
//                }
                }
            }
            // 背无鬼
            int posNormalNG = posNormalYG - Card.getTopGASize(new ArrayList<>(cardsDeck));
            if (posNormalNG >= 1 && posNormalNG <= pointCardsNumber) {
                GetCard getCard = new GetCard(GetCard.Method.NORMAL_NG, posNormalNG);
//                if (!ret.stream().anyMatch(g->g.pos == posNormalNG)){
                ret.add(getCard);
//                }
            }

            // 背无鬼 Next
            int posNormalNGNext = posNormalNG - 1;
            if (posNormalNGNext >= 1 && posNormalNGNext <= pointCardsNumber) {
                GetCard getCard = new GetCard(GetCard.Method.NORMAL_NG_NEXT, posNormalNGNext);
//                if (!ret.stream().anyMatch(g->g.pos == posNormalNGNext)){
                ret.add(getCard);
//                }
            }

            // 面有鬼
            int posFaceYG = cardsDeck.size() + 1 - posNormalYG;
            if (Card.getBottomGASize(new ArrayList<>(cardsDeck)) > 0) {

                if (posFaceYG >= 1 && posFaceYG <= pointCardsNumber) {
                    GetCard getCard = new GetCard(GetCard.Method.FACE_YG, posFaceYG);
//                if (!ret.stream().anyMatch(g->g.pos == posFaceYG)){
                    ret.add(getCard);
//                }
                }
            }
            // 面有鬼 Next
            int posFaceYGNext = posFaceYG - 1;
            if (Card.getBottomGASize(new ArrayList<>(cardsDeck)) > 0) {

                if (posFaceYGNext >= 1 && posFaceYGNext <= pointCardsNumber) {
                    GetCard getCard = new GetCard(GetCard.Method.FACE_YG_NEXT, posFaceYGNext);
//                if (!ret.stream().anyMatch(g->g.pos == posFaceYGNext)){
                    ret.add(getCard);
//                }
                }
            }

            // 面无鬼
            int posFaceNG = posFaceYG - Card.getBottomGASize(new ArrayList<>(cardsDeck));
            if (posFaceNG >= 1 && posFaceNG <= pointCardsNumber) {
                GetCard getCard = new GetCard(GetCard.Method.FACE_NG, posFaceNG);
//                if (!ret.stream().anyMatch(g->g.pos == posFaceNG)){
                ret.add(getCard);
//                }
            }

            // 面无鬼 Next
            int posFaceNGNext = posFaceNG - 1;
            if (posFaceNGNext >= 1 && posFaceNGNext <= pointCardsNumber) {
                GetCard getCard = new GetCard(GetCard.Method.FACE_NG_NEXT, posFaceNGNext);
//                if (!ret.stream().anyMatch(g->g.pos == posFaceNGNext)){
                ret.add(getCard);
//                }
            }
        }
        return ret;
    }

    public static Object[] sayAPosition(int pos, HashSet<GetCard> getCards, ArrayList<Card> cardsDeck) {
        Object[] objs = new Object[2];

        for (GetCard getCard :
                getCards) {
            if (getCard.pos == pos) {
                objs[0] = getCard;
                int deckIndex;
                switch (getCard.method) {
                    case NORMAL_YG:
                        deckIndex = pos - 1;
                        objs[1] = cardsDeck.get(deckIndex);
                        return objs;
                    case NORMAL_YG_NEXT:
                        deckIndex = pos + 1 - 1;
                        objs[1] = cardsDeck.get(deckIndex);
                        return objs;
                    case NORMAL_NG:
                        deckIndex = pos + Card.getTopGASize(cardsDeck) - 1;
                        objs[1] = cardsDeck.get(deckIndex);
                        return objs;
                    case NORMAL_NG_NEXT:
                        deckIndex = pos + Card.getTopGASize(cardsDeck) + 1 - 1;
                        objs[1] = cardsDeck.get(deckIndex);
                        return objs;
                    case FACE_YG:
                        deckIndex = cardsDeck.size() + 1 - pos - 1;
                        objs[1] = cardsDeck.get(deckIndex);
                        return objs;
                    case FACE_YG_NEXT:
                        deckIndex = cardsDeck.size() + 1 - pos - 1 - 1;
                        if (deckIndex == 56) {
                            System.out.println(11);
                        }
                        objs[1] = cardsDeck.get(deckIndex);
                        return objs;
                    case FACE_NG:
                        int bottomGASize = Card.getBottomGASize(cardsDeck);
                        deckIndex = cardsDeck.size() + 1 - pos - bottomGASize - 1;
                        if (deckIndex == -1) {
                            System.out.println(11);
                            bottomGASize = Card.getBottomGASize(cardsDeck);
                        }
                        objs[1] = cardsDeck.get(deckIndex);
                        return objs;
                    case FACE_NG_NEXT:
                        bottomGASize = Card.getBottomGASize(cardsDeck);
                        deckIndex = cardsDeck.size() + 1 - pos - bottomGASize - 1 - 1;
                        objs[1] = cardsDeck.get(deckIndex);
                        return objs;
                    default:
                        return objs;
                }
            }
        }
        return objs;
    }


    public static Map<Integer, Float> benchMark(List<Card> cardDeck, boolean hint, Map<Integer, Float> sayPointDistribution) {
        HashMap<Integer, Float> ret = new HashMap<>() {{
            IntStream.rangeClosed(1, 13).forEach(i -> put(i, 0f));
        }};
        HashMap<Integer, String> print = new HashMap<>();

        for (Integer point : IntStream.rangeClosed(1, 13).boxed().toList()) {
            Set<GetCard> getCards = sayAPoint(point, cardDeck);
            Set<Integer> cardPos;
            Set<Integer> reachablePos;
            if (!hint) {
                cardPos = IntStream.rangeClosed(1, 52).boxed().collect(Collectors.toSet());
                reachablePos = getCards.stream().map(gc -> gc.pos).collect(Collectors.toSet());

            } else {
                cardPos = IntStream.rangeClosed(OptimizedSort.low, OptimizedSort.high).boxed().collect(Collectors.toSet());
                reachablePos = getCards.stream().map(gc -> gc.pos).filter(v -> v >= OptimizedSort.low && v <= OptimizedSort.high).collect(Collectors.toSet());
            }

            float sucRate = (float) reachablePos.size() / cardPos.size();
            ret.put(point, sucRate);
            DecimalFormat decimalFormat = new DecimalFormat(".00"); //构造方法的字符格式这里如果小数不足2位,会以0补足.
            print.put(point, decimalFormat.format(sucRate));
        }

        double expectation = ret.keySet().stream().mapToDouble(r -> ret.get(r) * sayPointDistribution.get(r)).sum();
        System.out.println((hint ? "   with" : "without") + " hint: " + print + "     Expectation:" + expectation);
        return ret;
    }
}
