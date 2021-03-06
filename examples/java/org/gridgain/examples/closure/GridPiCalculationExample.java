// @java.file.header

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.examples.closure;

import org.gridgain.grid.*;
import org.gridgain.grid.typedef.*;
import static org.gridgain.grid.GridClosureCallMode.*;

/**
 * This example calculates Pi number in parallel on the grid.
 *
 * @author @java.author
 * @version @java.version
 */
public final class GridPiCalculationExample {
    /** Number of calculation per node. */
    private static final int N = 10000;

    /**
     * Calculates Pi part for a given range.
     *
     * @param start Start of the <code>{start, start + N}</code> range.
     * @return Calculation for the range.
     */
    private static double calcPi(int start) {
        X.println("Calculating PI from: " + start);

        double acc = start == 0 ? 3 : 0;

        for (int i = Math.max(start, 1); i < start + N; i++)
            // Nilakantha algorithm.
            acc += 4.0 * (2 * (i % 2) - 1) / (2 * i) / (2 * i + 1) / (2 * i + 2);

        return acc;
    }

    /**
     * Starts examples and calculates Pi number on the grid.
     *
     * @param args Command line arguments - none required.
     * @throws GridException Thrown in case of any error.
     */
    public static void main(String[] args) throws GridException {
        // Typedefs:
        // ---------
        // G -> GridFactory
        // C1 -> GridClosure
        // F -> GridFunc

        G.start();

        Grid g = G.grid();

        try {
            System.out.println("Pi estimate: " +
                g.reduce(SPREAD, F.yield(F.range(0, g.size()), new C1<Integer, Double>() {
                    @Override public Double apply(Integer i) {
                        return calcPi(i * N);
                    }
                }), F.sumDoubleReducer())
            );
        }
        finally {
            G.stop(true);
        }
    }
}
