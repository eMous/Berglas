import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class OptimizedSort {
    static int low = 10;
    static int high = 44;
    public static final Float totalPossibleVal = 1f; // 约定13张牌对应的选择分布值的和是10

    private static void fixDistributionMap(Map<Integer, Float> toFix) {
        Float totalToFix = toFix.values().stream().reduce(Float::sum).get();
        Float toDiv = totalToFix / totalPossibleVal;
        for (Integer key : toFix.keySet()) {
            Float oldVal = toFix.get(key);
            Float newVal = oldVal / toDiv;
            toFix.put(key, newVal);
        }
    }

    static Map<Integer, Float> defaultSayPointDistribution = new HashMap<>() {{
        put(1, 2f);
        put(2, 2f);
        put(3, 2.5f);
        put(4, 3f);
        put(5, 4f);
        put(6, 4.5f);
        put(7, 5f);
        put(8, 4f);
        put(9, 4.2f);
        put(10, 2.5f);
        put(11, 2f);
        put(12, 0.1f);
        put(13, 0.1f);
        fixDistributionMap(this);
    }};


    public static void main(String[] args) {


//        generateAndTest(52, 3, 1, 13, 42,defaultSayPointDistribution);
//        generateAndTest(52, 3, 1, 7, 42, defaultSayPointDistribution);
        generateAndTest(52, 3, 3, 7, 45, defaultSayPointDistribution);

        int pointCardSize = 52;
        int topAGSize = 3;
        int bottomAGSize = 3;
        System.out.printf("Test Bilibili %d %d %d cards %d %d\n", pointCardSize, topAGSize, bottomAGSize, low, high);

        ArrayList<Card> deck;

        if (System.getProperty("os.name").startsWith("Mac OS")) {
            deck = ExcelHandler.getCardsFromExcel("src/Berglas.xlsx", 0);
            // 苹果
        } else {
            // windows
            deck = ExcelHandler.getCardsFromExcel("src\\Berglas.xlsx", 0);
        }
        Calc.benchMark(deck, true, defaultSayPointDistribution);
        System.out.println("");


        low = 7;
        high = 45;
        List<Integer> sortedPositions = generatePositions(52,3,3, true);
        deck = generateDeckByBestPositions(sortedPositions,52,3,3, defaultSayPointDistribution);
        Calc.benchMark(deck, true, defaultSayPointDistribution);
        ExcelHandler.WriteCardsToExcel(deck);
    }

    private static void generateAndTest(int pointCardSize, int topAGSize, int bottomAGSize, int lowHint, int highHint, Map<Integer, Float> pointDistribution) {
        low = lowHint;
        high = highHint;

        for (boolean hint :
                Arrays.asList(true)) {
            System.out.printf("Test My Optimized %d %d %d cards %d %d for hint:%b \n", pointCardSize, topAGSize, bottomAGSize, low, high, hint);
            List<Integer> sortedPositions = generatePositions(pointCardSize, topAGSize, bottomAGSize, hint);
            ArrayList<Card> deck = generateDeckByBestPositions(sortedPositions, pointCardSize, topAGSize, bottomAGSize, pointDistribution);


            System.out.printf("example: ");
            Calc.benchMark(deck, true, pointDistribution);
//            System.out.printf("example: ");
//            Calc.benchMark(deck, false, pointDistribution);
        }
        System.out.println("");

    }

    private static List<Integer> generatePositions(int pointCardSize, int topAGSize, int bottomAGSize, boolean withHint) {
        int wholeSize = pointCardSize + topAGSize + bottomAGSize;
        List<Integer> NormalNG = getPosListFromCardDeckView(wholeSize, topAGSize, bottomAGSize, GetCard.Method.NORMAL_NG);
        List<Integer> NormalNGNext = getPosListFromCardDeckView(wholeSize, topAGSize, bottomAGSize, GetCard.Method.NORMAL_NG_NEXT);
        List<Integer> NormalYGNext = getPosListFromCardDeckView(wholeSize, topAGSize, bottomAGSize, GetCard.Method.NORMAL_YG_NEXT);
        List<Integer> NormalYG = getPosListFromCardDeckView(wholeSize, topAGSize, bottomAGSize, GetCard.Method.NORMAL_YG);
        List<Integer> FaceNG = getPosListFromCardDeckView(wholeSize, topAGSize, bottomAGSize, GetCard.Method.FACE_NG);
        List<Integer> FaceNGNext = getPosListFromCardDeckView(wholeSize, topAGSize, bottomAGSize, GetCard.Method.FACE_NG_NEXT);
        List<Integer> FaceYG = getPosListFromCardDeckView(wholeSize, topAGSize, bottomAGSize, GetCard.Method.FACE_YG);
        List<Integer> FaceYGNext = getPosListFromCardDeckView(wholeSize, topAGSize, bottomAGSize, GetCard.Method.FACE_YG_NEXT);

        List<List<Integer>> matrix = Arrays.asList(NormalNG, NormalNGNext, NormalYG, NormalYGNext, FaceNG, FaceNGNext, FaceYG, FaceYGNext);
        List<TreeSet<Integer>> posList = Stream.generate(TreeSet<Integer>::new).limit(pointCardSize).collect(Collectors.toList());
        for (int i = 0; i < posList.size(); ++i) {
            // 角标对于的元素i 是 第 i+1 张 有用牌 （背面朝上模式）的可达范围（观众报的翻牌数集合), low high 可以用来设置 ”默认观众不会报 小于等于 low的翻牌数，以及 大于等于 high的翻牌数“
            // 从而优化成功率 （部分元素对应的可达翻牌数集合的元素个数将会减少，而每个位置的可达翻牌数集合的元素个数将作为高概率牌选点的指标）
            for (List<Integer> list : matrix) {
                Integer pos = list.get(i);
                // 因为pos是观众报的翻牌数，所以他一定要满足1-52
                if (pos >= 1 && pos <= 52) {
                    if (withHint && pos >= low && pos <= high) {
                        posList.get(i).add(pos);
                    }
                }
            }
        }


        List<Integer> sortedPositions = getExtremePosition(posList);
        return sortedPositions;
    }

    public static List<Integer> getPosListFromCardDeckView(int wholeDeckSize, int topGADVSize, int bottomGADVSize, GetCard.Method method) {
        // 所有位置(对应非(Ghost/ADV)牌)的翻牌次数，根据不同的翻牌方式
        int nonGACardSize = wholeDeckSize - topGADVSize - bottomGADVSize;
        if (Objects.equals(method, GetCard.Method.NORMAL_YG)) {
            // 这个是默认G/A牌不摘除（且牌背向上）的翻牌方式
            // 所以第一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 topGADVSize + 1,
            // 所以最后一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 "第一张 非(Ghost/ADV)牌 对应的翻牌次数" + (总非GA牌数 - 1)

            int firstNonGACardPos = topGADVSize + 1;
            int lastNonGACardPos = firstNonGACardPos + (nonGACardSize - 1);
            return IntStream.rangeClosed(firstNonGACardPos, lastNonGACardPos).boxed().collect(Collectors.toList());
        }
        if (Objects.equals(method, GetCard.Method.NORMAL_YG_NEXT)) {
            // 这个是默认G/A牌不摘除（且牌背向上）的翻牌方式, 且默认真正目标牌在 翻牌位置 + 1 (翻拍位置为目标位置-1)
            // 所以第一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 topGADVSize + 1 - 1,
            // 所以最后一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 "第一张 非(Ghost/ADV)牌 对应的翻牌次数" + (总非GA牌数 - 1)

            int firstNonGACardPos = topGADVSize + 1 - 1;
            int lastNonGACardPos = firstNonGACardPos + (nonGACardSize - 1);
            return IntStream.rangeClosed(firstNonGACardPos, lastNonGACardPos).boxed().collect(Collectors.toList());
        }
        if (Objects.equals(method, GetCard.Method.NORMAL_NG)) {
            // 这个是默认G/A牌摘除（且牌背向上）的翻牌方式
            // 所以第一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 1,
            // 所以最后一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 "第一张 非(Ghost/ADV)牌 对应的翻牌次数" + (总非GA牌数 - 1)
            int firstNonGACardPos = 1;
            int lastNonGACardPos = firstNonGACardPos + (nonGACardSize - 1);
            return IntStream.rangeClosed(firstNonGACardPos, lastNonGACardPos).boxed().collect(Collectors.toList());
        }
        if (Objects.equals(method, GetCard.Method.NORMAL_NG_NEXT)) {
            // 这个是默认G/A牌摘除（且牌背向上）的翻牌方式, 且默认真正目标牌在 翻牌位置 + 1 (翻拍位置为目标位置-1)
            // 所以第一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 1 - 1,
            // 所以最后一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 "第一张 非(Ghost/ADV)牌 对应的翻牌次数" + (总非GA牌数 - 1)
            int firstNonGACardPos = 1 - 1;
            int lastNonGACardPos = firstNonGACardPos + (nonGACardSize - 1);
            return IntStream.rangeClosed(firstNonGACardPos, lastNonGACardPos).boxed().collect(Collectors.toList());
        }


        if (Objects.equals(method, GetCard.Method.FACE_NG)) {
            // 这个是默认G/A牌摘除（且牌正向上， 从下往上数）的翻牌方式
            // 所以（正序数）最后一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 1
            // 所以（正序数）第一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 "第最后一张 非(Ghost/ADV)牌 对应的翻牌次数" + (总非GA牌数 - 1)

            int lastNonGACardPos = 1;
            int firstNonGACardPos = lastNonGACardPos + (nonGACardSize - 1);

            // 因为rangeClosed方法自身的毛病必须把first和last换一下
            return IntStream.rangeClosed(lastNonGACardPos, firstNonGACardPos).boxed().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }
        if (Objects.equals(method, GetCard.Method.FACE_NG_NEXT)) {
            // 这个是默认G/A牌摘除（且牌正向上， 从下往上数）的翻牌方式 , 且默认真正目标牌在 翻牌位置 + 1 (翻拍位置为目标位置-1)
            // 所以（正序数）最后一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 1 - 1
            // 所以（正序数）第一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 "第最后一张 非(Ghost/ADV)牌 对应的翻牌次数" + (总非GA牌数 - 1)

            int lastNonGACardPos = 1 - 1;
            int firstNonGACardPos = lastNonGACardPos + (nonGACardSize - 1);

            // 因为rangeClosed方法自身的毛病必须把first和last换一下
            return IntStream.rangeClosed(lastNonGACardPos, firstNonGACardPos).boxed().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }

        if (Objects.equals(method, GetCard.Method.FACE_YG)) {
            // 这个是默认G/A牌不摘除（且牌正向上， 从下往上数）的翻牌方式
            // 所以（正序数）最后一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 bottomGADVSize + 1,
            // 所以（正序数）第一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 "第最后一张 非(Ghost/ADV)牌 对应的翻牌次数" + (总非GA牌数 - 1)

            int lastNonGACardPos = bottomGADVSize + 1;
            int firstNonGACardPos = lastNonGACardPos + (nonGACardSize - 1);
            // 因为rangeClosed方法自身的毛病必须把first和last换一下
            return IntStream.rangeClosed(lastNonGACardPos, firstNonGACardPos).boxed().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }
        if (Objects.equals(method, GetCard.Method.FACE_YG_NEXT)) {
            // 这个是默认G/A牌不摘除（且牌正向上， 从下往上数）的翻牌方式 , 且默认真正目标牌在 翻牌位置 + 1 (翻拍位置为目标位置-1)
            // 所以（正序数）最后一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 bottomGADVSize + 1 - 1,
            // 所以（正序数）第一张 非(Ghost/ADV)牌 对应的翻牌次数 应该是 "第最后一张 非(Ghost/ADV)牌 对应的翻牌次数" + (总非GA牌数 - 1)

            int lastNonGACardPos = bottomGADVSize + 1 - 1;
            int firstNonGACardPos = lastNonGACardPos + (nonGACardSize - 1);
            // 因为rangeClosed方法自身的毛病必须把first和last换一下
            return IntStream.rangeClosed(lastNonGACardPos, firstNonGACardPos).boxed().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        }
        return null;
    }

    private static ArrayList<Card> generateDeckByBestPositions(List<Integer> sortedIndexs, int pointCardSize, int topGASize, int bottomGASize, Map<Integer, Float> pointDistribution) {
        ArrayList<Card> ret = new ArrayList<>();
        assert topGASize <= 3 && topGASize >= 0;
        ArrayList<Card> topGA = new ArrayList<>();
        if (topGASize == 1) {
            topGA = new ArrayList<>(Arrays.asList(Card.newCard(-1, Card.Color.ADV)));
        } else if (topGASize == 2) {
            topGA = new ArrayList<>(Arrays.asList(Card.newCard(-1, Card.Color.BIG_G), Card.newCard(-1, Card.Color.LITTLE_G)));
        } else if (topGASize == 3) {
            topGA = new ArrayList<>(Arrays.asList(Card.newCard(-1, Card.Color.ADV), Card.newCard(-1, Card.Color.BIG_G), Card.newCard(-1, Card.Color.LITTLE_G)));
        }
        ret.addAll(topGA);
        ret.addAll(Collections.nCopies(pointCardSize, Card.newCard(1000, Card.Color.SPADE)));
        sortedIndexs = sortedIndexs.stream().map(integer -> integer + topGASize).toList();
        for (Integer index : sortedIndexs) {
            List<Integer> order = pointDistribution.keySet().stream().sorted((key, key2) -> -pointDistribution.get(key).compareTo(pointDistribution.get(key2))).toList();
            Card card = randomGenerateACard(ret, order);
            assert card != null;
            ret.set(index, card);
        }

        assert bottomGASize <= 3 && bottomGASize >= 0;
        ArrayList<Card> bottomGA = new ArrayList<>();
        if (bottomGASize == 1) {
            bottomGA = new ArrayList<>(Arrays.asList(Card.newCard(-1, Card.Color.ADV)));
        } else if (bottomGASize == 2) {
            bottomGA = new ArrayList<>(Arrays.asList(Card.newCard(-1, Card.Color.LITTLE_G), Card.newCard(-1, Card.Color.BIG_G)));
        } else if (bottomGASize == 3) {
            bottomGA = new ArrayList<>(Arrays.asList(Card.newCard(-1, Card.Color.LITTLE_G), Card.newCard(-1, Card.Color.BIG_G), Card.newCard(-1, Card.Color.ADV)));
        }
        ret.addAll(bottomGA);
        return ret;
    }

    private static Card randomGenerateACard(ArrayList<Card> ret, List<Integer> order) {
        for (Integer point : order) {
            List<Card> samePointCards = ret.stream().filter(card -> card != null && card.num == point).toList();
            HashSet<Card.Color> fullColors = new HashSet<Card.Color>(List.of(Card.Color.values()));
            fullColors.removeAll(new HashSet<>(List.of(Card.Color.ADV, Card.Color.LITTLE_G, Card.Color.BIG_G)));
            if (null != samePointCards) {
                Set<Card.Color> colorsExist = samePointCards.stream().map(card -> card.color).collect(Collectors.toSet());
                fullColors.removeAll(colorsExist);
                if (fullColors.size() > 0) {
                    List<Card.Color> list = fullColors.stream().toList();
                    Random rand = new Random();
                    return Card.newCard(point, list.get(rand.nextInt(list.size())));
                }
            } else {
                // Deck里没有同值卡
                List<Card.Color> list = fullColors.stream().toList();
                Random rand = new Random();
                return Card.newCard(point, list.get(rand.nextInt(list.size())));
            }
        }
        return null;
    }

    private static List<Integer> getExtremePosition(List<TreeSet<Integer>> posList) {
        ArrayList<Integer> ret = new ArrayList<>();
        int valuableIndexLow = 0;
        int valuableIndexHigh = -1;
        for (int i = 0; i < posList.size(); ++i) {
            // 从正数的第一个有点数的位置开始，找可达翻牌数集合元素个数非零的位置，将他设置为最低的有价值位置。
            // （如果一个可达位置都没有，那这个位置就被理解成是无价值。 无价值的位置和任何位置组合到一起都不会影响到被组合的位置的价值）
            // 比如说 如果默认 观众不会说 小于20 大于 30 的位置， 那么 第一张非王非广告牌，就是无价值的，那他就会被最不优先的被占有 （或者说他将被最不可能被念到的点数占用，点数的优先权 默认是从7开始向两边均匀递减）
            if (posList.get(i).size() != 0) {
                valuableIndexLow = i;
                break;
            }
        }
        for (int i = posList.size() - 1; i >= 0; --i) {
            if (posList.get(i).size() != 0) {
                valuableIndexHigh = i;
                break;
            }
        }
        // 有价值的位置池 （一般情况它们是中间的位置）
        HashSet<Integer> availableValuePool = IntStream.rangeClosed(valuableIndexLow, valuableIndexHigh).boxed().collect(Collectors.toCollection(HashSet::new));
        // 价值为0的位置池 （一般情况它们是两侧的位置，比如1，52这种），当有价值池不足4元素时，无价值池会补充元素进入有价值池
        HashSet<Integer> availableNoValuePool = IntStream.rangeClosed(0, posList.size() - 1).boxed().collect(Collectors.toCollection(HashSet::new));
        availableNoValuePool.removeAll(availableValuePool);

        ArrayList<ArrayList<Integer>> combinationsToTry = new ArrayList<>();
        // 第一次进入while就是求最有价值的4个位置（一般来说，它们将被用来放4个7，但这是后面方法要做的）
        while (availableNoValuePool.size() + availableValuePool.size() > 0) {
            // 先使用有用值
            int grabFromNoVal = 4 - availableValuePool.size();
            if (grabFromNoVal > 4) grabFromNoVal = 4;
            if (grabFromNoVal < 0) grabFromNoVal = 0;
            Iterator<Integer> iter = availableNoValuePool.iterator();
            while (grabFromNoVal > 0 && iter.hasNext()) {
                grabFromNoVal--;
                // 从no val中取
                availableValuePool.add(iter.next());
                iter.remove();
            }


            List<Integer> valuablePosList = availableValuePool.stream().toList();
            if (combinationsToTry.size() < 1) {
                combinationsToTry = allCombinations(valuablePosList, valuablePosList.size() >= 4 ? 4 : valuablePosList.size());
                combinationsToTry.sort((c1, c2) -> {
                    HashSet c1PosesSet = c1.stream().flatMap(pos -> posList.get(pos).stream()).collect(Collectors.toCollection(HashSet<Integer>::new));
                    HashSet c2PosesSet = c2.stream().flatMap(pos -> posList.get(pos).stream()).collect(Collectors.toCollection(HashSet<Integer>::new));
                    if (c1PosesSet.size() > c2PosesSet.size()) return -1;
                    if (c1PosesSet.size() == c2PosesSet.size()) return 0;
                    else return 1;
                });
            }

            // 绝大多数情况是4个，如果pointCards的个数不是4的倍数，那最后一组则会小于4个
            List<Integer> posCombination = combinationsToTry.get(0);
            ret.addAll(posCombination);
            availableValuePool.removeAll(posCombination);
            combinationsToTry.removeIf(list -> {
                HashSet set = new HashSet<>(list);
                set.removeAll(posCombination);
                return set.size() != 4;
            });
        }
        return ret;
    }

    public static ArrayList<ArrayList<Integer>> allCombinations(List<Integer> list, int num) {
        ArrayList<ArrayList<Integer>> ret = new ArrayList<>();
        if (num == 1) {
            return new ArrayList(list.stream().map(val -> new ArrayList(Arrays.asList(val))).collect(Collectors.toList()));
        }
        for (int i = 0; i < list.size() && list.size() - i > num - 1; ++i) {
            ArrayList<ArrayList<Integer>> result = allCombinations(list.subList(i + 1, list.size()), num - 1);
            int finalI = i;
            result.forEach(each -> {
                each.add(0, list.get(finalI));
            });
            ret.addAll(result);
        }
        return ret;
    }

    public static List<List<Integer>> listPermutations(List<Integer> list) {

        if (list.size() == 0) {
            List<List<Integer>> result = new ArrayList<List<Integer>>();
            result.add(new ArrayList<Integer>());
            return result;
        }

        List<List<Integer>> returnMe = new ArrayList<List<Integer>>();

        Integer firstElement = list.remove(0);

        List<List<Integer>> recursiveReturn = listPermutations(list);
        for (List<Integer> li : recursiveReturn) {

            for (int index = 0; index <= li.size(); index++) {
                List<Integer> temp = new ArrayList<Integer>(li);
                temp.add(index, firstElement);
                returnMe.add(temp);
            }

        }
        return returnMe;
    }
}
