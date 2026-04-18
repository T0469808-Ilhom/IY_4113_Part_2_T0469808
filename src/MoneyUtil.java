import java.math.BigDecimal;
import java.math.RoundingMode;

class MoneyUtil {
    public static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}