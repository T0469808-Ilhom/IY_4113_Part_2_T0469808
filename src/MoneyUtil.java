import java.math.BigDecimal;
import java.math.RoundingMode;

// A small utility class that rounds any BigDecimal value to two decimal places.
// It is used throughout the system wherever a fare or total is displayed or stored,
// so rounding is always consistent and never done inline in other methods.

class MoneyUtil {
    public static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}