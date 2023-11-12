package tools_test;

import net.sourceforge.jdistlib.disttest.TestKind;
import net.sourceforge.jdistlib.math.VectorMath;
import net.sourceforge.jdistlib.util.Utilities;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class KS {
    @Test
    public void test() {
        double[] X = new double[]{20,22,24,26};
        double[] Y = new double[]{20,22,23.99};

        TestKind kind = TestKind.TWO_SIDED;
        boolean isExact = true;
        int nX = X.length;
        int nY = Y.length;
        int n_total = nX + nY;
        Set<Double> set = new HashSet();
        double[] w = Utilities.c(X, Y);
        int[] ow = Utilities.order(w);
        double[] z = new double[n_total];
        double cs = 0.0;

        int new_n;
        for(new_n = 0; new_n < n_total; ++new_n) {
            cs += ow[new_n] + 1 <= nX ? 1.0 / (double)nX : -1.0 / (double)nY;
            z[new_n] = cs;
            set.add(w[new_n]);
        }

        new_n = set.size();
        if (new_n < n_total) {
            double[] dz = new double[n_total];
            double[] new_z = new double[new_n];
            System.arraycopy(w, 0, dz, 0, n_total);
            Utilities.sort(dz);
            dz = VectorMath.diff(dz);
            int i = 0;

            for(int j = 0; i < dz.length; ++i) {
                if (dz[i] != 0.0) {
                    new_z[j++] = z[i];
                }
            }

            new_z[new_n - 1] = z[n_total - 1];
            z = new_z;
        }

        double maxDiv = Double.NaN;
        maxDiv = VectorMath.max(VectorMath.vabs(z));

        double q;
        double pval;
        if (new_n >= n_total) {
            if (nX > nY) {
                int temp = nY;
                nY = nX;
                nX = temp;
            }

            q = (0.5 + Math.floor(maxDiv * (double)nX * (double)nY - 1.0E-7)) / (double)(nX * nY);
            double[] u = new double[nY + 1];
            double dx = 1.0 / (double)nX;
            double dy = 1.0 / (double)nY;

            int i;
            for(i = 0; i <= nY; ++i) {
                u[i] = (double)i * dy > q ? 0.0 : 1.0;
            }

            for(i = 1; i <= nX; ++i) {
                double w_star = (double)i / (double)(i + nY);
                u[0] = (double)i * dx > q ? 0.0 : w_star * u[0];

                for(int j = 1; j <= nY; ++j) {
                    u[j] = Math.abs((double)i * dx - (double)j * dy) > q ? 0.0 : w_star * u[j] + u[j - 1];
                }
            }

            pval = 1.0 - u[nY];
        } else {
            q = (double)nX * ((double)nY * 1.0 / (double)(nX + nY));
            if (kind != TestKind.TWO_SIDED) {
                pval = Math.exp(-2.0 * q * maxDiv * maxDiv);
            } else {
                double tol = 1.0E-10;
                double d = Math.sqrt(q) * maxDiv;
                int k;
                double z_star;
                double w_star;
                double s;
                if (d < 1.0) {
                    k = (int)Math.sqrt(2.0 - Math.log(tol));
                    z_star = -1.2337005501361697 / (d * d);
                    w_star = Math.log(d);
                    s = 0.0;

                    for(int i = 1; i < k; i += 2) {
                        s += Math.exp((double)(i * i) * z_star - w_star);
                    }

                    pval = 1.0 - s / 0.3989422804014327;
                } else {
                    k = 1;
                    z_star = -2.0 * d * d;
                    w_star = -1.0;
                    s = 0.0;

                    double newval;
                    for(newval = 1.0; Math.abs(s - newval) > tol; ++k) {
                        s = newval;
                        newval += 2.0 * w_star * Math.exp(z_star * (double)k * (double)k);
                        w_star *= -1.0;
                    }

                    pval = 1.0 - newval;
                }
            }
        }
        int a = 0;
//        return new double[]{maxDiv, pval};
    }

}
