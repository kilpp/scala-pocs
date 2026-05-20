package com.example

import org.specs2.mutable.Specification
import org.specs2.matcher.DataTables

/**
 * Data tables — Specs2's signature parameterized-testing feature.
 *
 * The table is read column-by-column. The header row uses `||`,
 * data rows use `|`, and the table is terminated by `|>` which
 * applies a function to each row. Each row reports its own
 * pass/fail, so a single example yields N rows of output.
 */
class DataTablesSpec extends Specification with DataTables:

  "Calculator.add" >> {
    "obeys an arithmetic truth table" >> {
      val calc = Calculator()
      // format: off
      "a"  || "b"  | "expected" |>
        0  !!  0   ! 0          |
        1  !!  2   ! 3          |
       -5  !!  5   ! 0          |
       10  !! -3   ! 7          | { (a, b, expected) =>
          calc.add(a, b) must_== expected
      }
      // format: on
    }
  }

  "Calculator.isEven" >> {
    "matches the parity table" >> {
      val calc = Calculator()
      "n" || "isEven" |>
        0  !! true    |
        1  !! false   |
        2  !! true    |
       -3  !! false   |
      100  !! true    | { (n, expected) =>
        calc.isEven(n) must_== expected
      }
    }
  }

  "forall — assert a predicate on every element of a collection" >> {
    "all positive numbers are positive" >> {
      val xs = List(1, 2, 3, 4, 5)
      forall(xs)(n => n must be_>(0))
    }
    "atLeastOnce — at least one element satisfies the predicate" >> {
      val xs = List(1, 2, 3)
      atLeastOnce(xs)(n => n must_== 2)
    }
  }
