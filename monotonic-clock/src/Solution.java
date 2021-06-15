import org.jetbrains.annotations.NotNull;

public class Solution implements MonotonicClock {
    private final RegularInt ZERO_VALUE = new RegularInt(0);

    private final RegularInt w_r2l_c1 = new RegularInt(0);
    private final RegularInt w_r2l_c2 = new RegularInt(0);
    private final RegularInt w_r2l_c3 = new RegularInt(0);
    private final RegularInt w_l2r_c1 = new RegularInt(0);
    private final RegularInt w_l2r_c2 = new RegularInt(0);
    private final RegularInt w_l2r_c3 = new RegularInt(0);

    @Override
    public void write(@NotNull Time time) {
        // write left-to-right
        w_l2r_c1.setValue(time.getD1());
        w_l2r_c2.setValue(time.getD2());
        w_l2r_c3.setValue(time.getD3());

        // write right-to-left
        w_r2l_c3.setValue(time.getD3());
        w_r2l_c2.setValue(time.getD2());
        w_r2l_c1.setValue(time.getD1());
    }

    @NotNull
    @Override
    public Time read() {
        // read left-to-right
        int r_l2r_c1 = w_r2l_c1.getValue();
        int r_l2r_c2 = w_r2l_c2.getValue();
        int r_l2r_c3 = w_r2l_c3.getValue();

        // read right-to-left
        int r_r2l_c3 = w_l2r_c3.getValue();
        int r_r2l_c2 = w_l2r_c2.getValue();
        int r_r2l_c1 = w_l2r_c1.getValue();

        if (r_l2r_c1 == r_r2l_c1) {
            if (r_l2r_c2 == r_r2l_c2) {
                if (r_l2r_c3 == r_r2l_c3) {
                    return new Time(r_l2r_c1, r_l2r_c2, r_l2r_c3);
                } else {
                    return new Time(r_l2r_c1, r_l2r_c2, r_r2l_c3);
                }
            } else {
                return new Time(r_l2r_c1, r_r2l_c2, ZERO_VALUE.getValue());
            }
        } else {
            return new Time(r_r2l_c1, ZERO_VALUE.getValue(), ZERO_VALUE.getValue());
        }
    }
}