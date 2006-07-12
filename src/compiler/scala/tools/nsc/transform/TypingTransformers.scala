/* NSC -- new scala compiler
 * Copyright 2005 LAMP/EPFL
 * @author
 */
// $Id: TypingTransformers.scala 5359 2005-12-16 15:33:49Z dubochet $
package scala.tools.nsc.transform;

/** A base class for transforms. 
 *  A transform contains a compiler phase which applies a tree transformer.
 */ 
trait TypingTransformers {

  val global: Global
  import global._

  abstract class TypingTransformer(unit: CompilationUnit) extends Transformer {
    var localTyper: analyzer.Typer = analyzer.newTyper(analyzer.rootContext(unit))
    private var curTree: Tree = _

    override def atOwner[A](owner: Symbol)(trans: => A): A = {
      val savedLocalTyper = localTyper
      localTyper = localTyper.atOwner(curTree, owner)
      val result = super.atOwner(owner)(trans)
      localTyper = savedLocalTyper
      result
    }
      
    override def transform(tree: Tree): Tree = {
      curTree = tree
      tree match {
        case Template(_, _) =>
          // enter template into context chain
          atOwner(currentOwner) { super.transform(tree) }
        case _ =>
          super.transform(tree)
      }
    }
  }
}

