import java.util.Objects;

public class GetCard {
    Method method;

    public GetCard(Method method, int pos) {
        this.method = method;
        this.pos = pos;
    }
    int pos;
    enum Method{
        NORMAL_NG {
            @Override
            public String toString() {
                return "背无鬼";
            }
        },
        NORMAL_NG_NEXT{
            @Override
            public String toString() {
                return "背无鬼(下张)";
            }
        },
        NORMAL_YG{
            @Override
            public String toString() {
                return "背有鬼";
            }
        },
        NORMAL_YG_NEXT{
            @Override
            public String toString() {
                return "背有鬼(下张)";
            }
        },
        FACE_NG{
            @Override
            public String toString() {
                return "面无鬼";
            }
        },
        FACE_NG_NEXT{
            @Override
            public String toString() {
                return "面无鬼(下张)";
            }
        },
        FACE_YG{
            @Override
            public String toString() {
                return "面有鬼";
            }
        },
        FACE_YG_NEXT{
            @Override
            public String toString() {
                return "面有鬼(下张)";
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetCard getCard = (GetCard) o;
        return pos == getCard.pos && method == getCard.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, pos);
    }

    @Override
    public String toString() {
        return method.toString() + ":" + pos + "  ";
    }
}
