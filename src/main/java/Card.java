import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Card {

    int num;
    Color color;

    static List<Color> pointColor = List.of(Color.HEART,Color.SPADE,Color.CLUB,Color.DIAMOND);


    public static Card parse(String cardStr) {
        String patStr = ".*广告.*";
        Pattern pattern = Pattern.compile(patStr);
        Matcher matcher = pattern.matcher(cardStr);
        if (matcher.find()) {
            return Card.newCard(-1, Color.ADV);
        }

        patStr = ".*小王.*";
        pattern = Pattern.compile(patStr);
        matcher = pattern.matcher(cardStr);
        if (matcher.find()) {
            return Card.newCard(-1, Color.LITTLE_G);
        }

        patStr = ".*大王.*";
        pattern = Pattern.compile(patStr);
        matcher = pattern.matcher(cardStr);
        if (matcher.find()) {
            return Card.newCard(-1, Color.BIG_G);
        }

        patStr = "^(红桃|梅花|方片|黑桃)([1-9]$|1[0-3]$)";
        pattern = Pattern.compile(patStr);
        matcher = pattern.matcher(cardStr);
        if (!matcher.find()) {
            return null;
        } else {

            String colorStr = matcher.group(1);
            Color color;
            switch (colorStr) {
                case "梅花":
                    color = Color.CLUB;
                    break;
                case "红桃":
                    color = Color.HEART;
                    break;
                case "方片":
                    color = Color.DIAMOND;
                    break;
                case "黑桃":
                    color = Color.SPADE;
                    break;
                default:
                    return null;
            }
            int val = Integer.parseInt(matcher.group(2));
            return Card.newCard(val, color);
        }
    }

    public static int getTopGASize(ArrayList<Card> cardsDeck) {
        int ret = 0;
        Iterator<Card> iterator = cardsDeck.iterator();
        while (iterator.hasNext()){
            if (!pointColor.contains(iterator.next().color)){
                ret++;
            }else{
                break;
            }
        }
        return ret;
    }
    public static int getBottomGASize(ArrayList<Card> cardsDeck) {
        int ret = 0;
        ListIterator<Card> iterator = cardsDeck.listIterator(cardsDeck.size());
        while (iterator.hasPrevious()){
            if (!pointColor.contains(iterator.previous().color)){
                ret++;
            }else{
                break;
            }
        }
        return ret;
    }

    public static int getPointCardsNumber(ArrayList<Card> cardsDeck) {
        return cardsDeck.size() - getTopGASize(cardsDeck) - getBottomGASize(cardsDeck);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return num == card.num && color == card.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, color);
    }

    enum Color {
        HEART {
            @Override
            public String toString() {
                return "红桃";
            }
        },
        DIAMOND {
            @Override
            public String toString() {
                return "方片";
            }
        },
        SPADE {
            @Override
            public String toString() {
                return "黑桃";
            }
        },
        CLUB {
            @Override
            public String toString() {
                return "梅花";
            }
        },
        BIG_G {
            @Override
            public String toString() {
                return "大王";
            }
        },
        LITTLE_G {
            @Override
            public String toString() {
                return "小王";
            }
        },
        ADV {
            @Override
            public String toString() {
                return "广告牌";
            }
        }
    }

    private Card(int num, Color color) {
        this.num = num;
        this.color = color;
    }

    public static Card newCard(int num, Color color) {
        return new Card(num, color);
    }

    @Override
    public String toString() {
        if (color.equals(Color.ADV) || color.equals(Color.BIG_G) || color.equals(Color.LITTLE_G)) {
            return color.toString();
        }
        return color.toString() + Integer.toString(num);
    }
}
