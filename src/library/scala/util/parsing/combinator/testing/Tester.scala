/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2007, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package scala.util.parsing.combinator.testing

import scala.util.parsing.combinator.lexical.Lexical
import scala.util.parsing.combinator.syntactical.TokenParsers

/** Facilitates testing a given parser on various input strings.
 *<p>
 * Example use:, val syntactic = new MyParsers
 * and val parser = syntactic.term (if MyParsers extends TokenParsers with a parser called
 * `term')</p>
 *
 * @author Martin Odersky, Adriaan Moors
 */
abstract class Tester {

  val syntactic: TokenParsers { val lexical: Lexical }
  val parser: syntactic.Parser[Any]


  /** Scans a String (using a `syntactic.lexical.Scanner'), parses it using `phrase(parser)', and
   * prints the input and the parsed result to the console. 
   */
  def test(in: String) {
    Console.println("\nin : "+in)
    Console.println(syntactic.phrase[Any](parser)(new syntactic.lexical.Scanner(in)))
  }
}
