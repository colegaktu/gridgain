// @groovy.file.header

/*
 * _________
 * __  ____/______________ ___   _______ ________
 * _  / __  __  ___/_  __ \__ | / /_  _ \__  ___/
 * / /_/ /  _  /    / /_/ /__ |/ / /  __/_  /
 * \____/   /_/     \____/ _____/  \___/ /_/
 *
 */

package org.gridgain.grover.examples

import static org.gridgain.grid.GridClosureCallMode.*
import static org.gridgain.grover.Grover.*
import org.gridgain.grover.categories.*

/**
 * Shows the world's shortest MapReduce application that calculates non-space
 * length of the input string. This example works equally on one computer or
 * on thousands requiring no special configuration or deployment.
 *
 * @author @java.author
 * @version @java.version
 */
@Typed
@Use(GroverProjectionCategory)
class GroverWorldShortestMapReduce {
    /**
     * @param args Command line arguments.
     */
    static void main(String[] args) {
        grover { ->
            def input = "World shortest mapreduce application"

            println("Non-space characters count: " +
                grid$.reduce$(SPREAD, input.split(" ").collect { { -> it.length() } }, { it.sum() })
            )
        }
    }
}
