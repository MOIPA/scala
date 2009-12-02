/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$

/** <p>
 *   This program generates the <code>ProductN</code>, <code>TupleN</code> <code>FunctionN</code> classes, where
 *   <code>0 &lt;= N &lt;+ MAXWIDTH</code>.
 *  </p>
 *  <p>
 *    usage: <code>scala -classpath ... genprod PATH</code>
 *    where <code>PATH</code> is the desired output directory
 *  </p>
 *
 *  @author  Burak Emir, Stephane Micheloud, Geoffrey Washburn, Paul Phillips
 *  @version 1.1
 */
object genprod {
  val MAX_ARITY = 22
  def arities = (1 to MAX_ARITY).toList

  class Group(val name: String) {
    def className(i: Int) = name + i
    def fileName(i: Int) = className(i) + ".scala"
  }

  def productFiles  = arities map Product.make
  def tupleFiles    = arities map Tuple.make
  def functionFiles = (0 :: arities) map Function.make
  def allfiles      = productFiles ::: tupleFiles ::: functionFiles

  trait Arity extends Group {
    def i: Int    // arity

    def typeArgsString(xs: Seq[String]) = xs.mkString("[", ", ", "]")

    def to              = (1 to i).toList
    def s               = if (i == 1) "" else "s"
    def className       = name + i
    def fileName        = className + ".scala"
    def targs           = to map ("T" + _)
    def vdefs           = to map ("v" + _)
    def xdefs           = to map ("x" + _)
    def mdefs           = to map ("_" + _)
    def invariantArgs   = typeArgsString(targs)
    def covariantArgs   = typeArgsString(targs map ("+" + _))
    def contraCoArgs    = typeArgsString((targs map ("-" + _)) ::: List("+R"))
    def fields          = List.map2(mdefs, targs)(_ + ":" + _) mkString ","
    def funArgs         = List.map2(vdefs, targs)(_ + ":" + _) mkString ","

    def genprodString       = "// generated by genprod on %s %s %s".format(now, withFancy, withMoreMethods)
    def now                 = new java.util.Date().toString()
    def moreMethods         = ""
    def descriptiveComment  = ""
    def withFancy           = if (descriptiveComment.isEmpty) "" else "(with fancy comment)"
    def withMoreMethods     = if (moreMethods.isEmpty) "" else "(with extra methods)"

    def header = """
/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2009, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$

%s

package scala
""".trim.format(genprodString) + "\n\n"
  }

  def main(args: Array[String]) {
    if (args.length != 1) {
      println("please give path of output directory")
      exit(-1)
    }
    val out = args(0)
    def writeFile(node: scala.xml.Node) {
      import java.io.{File, FileOutputStream}
      import java.nio.channels.Channels
      val f = new File(out, node.attributes("name").toString)
      try {
        f.createNewFile
        val fos = new FileOutputStream(f)
        val c = fos.getChannel
        val w = Channels.newWriter(c, "utf-8")
        w.write(node.text)
        w.close
      } catch {
        case e: java.io.IOException =>
          println(e.getMessage() + ": " + out)
          exit(-1)
      }
    }

    allfiles foreach writeFile
  }
}
import genprod._


/* zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz
                             F U N C T I O N
zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz */

object FunctionZero extends Function(0) {
  override def descriptiveComment = functionNTemplate.format("currentSeconds", "anonfun0",
""" *
 *    <b>val</b> currentSeconds = () => System.currentTimeMillis() / 1000L
 *
 *    <b>val</b> anonfun0 = <b>new</b> Function0[Long] {
 *      <b>def</b> apply(): Long = System.currentTimeMillis() / 1000L
 *    }
 *
 *    println(currentSeconds())
 *    println(anonfun0())""")
  override def moreMethods = ""
}

object FunctionOne extends Function(1) {
  override def descriptiveComment = functionNTemplate.format("succ", "anonfun1",
""" *
 *    <b>val</b> succ = (x: Int) => x + 1
 *
 *    <b>val</b> anonfun1 = <b>new</b> Function1[Int, Int] {
 *      <b>def</b> apply(x: Int): Int = x + 1
 *    }
 *
 *    println(succ(0))
 *    println(anonfun1(0))""")

  override def moreMethods = """
  /** (f compose g)(x) ==  f(g(x))
   */
  def compose[A](g: A => T1): A => R = { x => apply(g(x)) }

  /** (f andThen g)(x) ==  g(f(x))
   */
  def andThen[A](g: R => A): T1 => A = { x => g(apply(x)) }
"""
}

object FunctionTwo extends Function(2) {
  override def descriptiveComment = functionNTemplate.format("max", "anonfun2",
""" *
 *    <b>val</b> max = (x: Int, y: Int) => <b>if</b> (x < y) y <b>else</b> x
 *
 *    <b>val</b> anonfun2 = <b>new</b> Function2[Int, Int, Int] {
 *      <b>def</b> apply(x: Int, y: Int): Int = <b>if</b> (x < y) y <b>else</b> x
 *    }
 *
 *    println(max(0, 1))
 *    println(anonfun2(0, 1))""")
}

object Function
{
  def make(i: Int) = apply(i)()
  def apply(i: Int) = i match {
    case 0    => FunctionZero
    case 1    => FunctionOne
    case 2    => FunctionTwo
    case _    => new Function(i)
  }
}

class Function(val i: Int) extends Group("Function") with Arity
{
  val functionNTemplate = """<p>
 * In the following example the definition of
 *    <code>%s</code> is a shorthand for the anonymous class
 *    definition <code>%s</code>:
 *  </p>
 *  <pre>
 *  <b>object</b> Main <b>extends</b> Application {
%s
 *  }</pre>"""

  def toStr() = "\"" + ("<function%d>" format i) + "\""
  def apply() = {
<file name={fileName}>{header}

/** &lt;p&gt;
 *    Function with {i} parameter{s}.
 *  &lt;/p&gt;
 *  {descriptiveComment}
 */
trait {className}{contraCoArgs} extends AnyRef {{ self =>
  def apply({funArgs}): R
  override def toString() = {toStr}
  {moreMethods}
}}
</file>
}

  private def commaXs = xdefs.mkString("(", ", ", ")")

  // (x1: T1) => (x2: T2) => (x3: T3) => (x4: T4) => apply(x1,x2,x3,x4)
  def shortCurry = {
    val body = "apply" + commaXs
    List.map2(xdefs, targs)("(%s: %s) => ".format(_, _)).mkString("", "", body)
  }

  // (x1: T1) => ((x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7) => self.apply(x1,x2,x3,x4,x5,x6,x7)).curry
  def longCurry = (List.map2(xdefs, targs)(_ + ": " + _) drop 1).mkString(
    "(x1: T1) => ((",
    ", ",
    ") => self.apply%s).curry".format(commaXs)
  )

  // f(x1,x2,x3,x4,x5,x6)  == (f.curry)(x1)(x2)(x3)(x4)(x5)(x6)
  def curryComment = { """
  /** f%s  == (f.curry)%s
   */
""".format(commaXs, xdefs map ("(" + _ + ")") mkString)
  }

  def tupleMethod = {
    def comment = """
  /* f%s == (f.tuple)(Tuple%d%s)
   */
""".format(commaXs, i, commaXs)
    def body = "case Tuple%d%s => apply%s".format(i, commaXs, commaXs)

    comment + "  def tuple: Tuple%d%s => R = {\n    %s\n  }\n".format(i, invariantArgs, body)
  }

  def curryMethod = {
    val body = if (i < 5) shortCurry else longCurry

    curryComment +
    "  def curry: %s => R = {\n    %s\n  }\n".format(
      targs mkString " => ", body
    )
  }

  override def moreMethods = curryMethod + tupleMethod
} // object Function


/* zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz
                                     T U P L E
zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz */

object Tuple
{
  def make(i: Int) = apply(i)()
  def apply(i: Int) = i match {
    case 2  => TupleTwo
    case _  => new Tuple(i)
  }
}

object TupleTwo extends Tuple(2)
{
  override def moreMethods = """
  /** Swap the elements of the tuple */
  def swap: Tuple2[T2,T1] = Tuple2(_2, _1)
"""
}

class Tuple(val i: Int) extends Group("Tuple") with Arity
{
  // prettifies it a little if it's overlong
  def mkToString() = {
    def str(xs: List[String]) = xs.mkString(""" + "," + """)
    if (i <= MAX_ARITY / 2) str(mdefs)
    else {
      val s1 = str(mdefs take (i / 2))
      val s2 = str(mdefs drop (i / 2))
      s1 + " +\n    \",\" + " + s2
    }
  }

  def apply() = {
<file name={fileName}>{header}

/** {className} is the canonical representation of a @see {Product.className(i)}
 *  {descriptiveComment}
 */
case class {className}{covariantArgs}({fields})
  extends {Product.className(i)}{invariantArgs}
{{
  override def toString() = "(" + {mkToString} + ")"
  {moreMethods}
}}
</file>}
} // object Tuple


/* zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz
                                  P R O D U C T
zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz */

object Product extends Group("Product")
{
  def make(i: Int) = apply(i)()
  def apply(i: Int) = new Product(i)
}

class Product(val i: Int) extends Group("Product") with Arity
{
  def cases = {
    val xs = for ((x, i) <- mdefs.zipWithIndex) yield "case %d => %s".format(i, x)
    val default = "case _ => throw new IndexOutOfBoundsException(n.toString())"
    "\n" + ((xs ::: List(default)) map ("    " + _ + "\n") mkString)
  }
  def proj = {
    List.map2(mdefs, targs)(
      "  /** projection of this product */\n  def %s: %s\n\n".format(_, _)
    ) mkString
  }

  def apply() = {
<file name={fileName}>{header}
object {className} {{
  def unapply{invariantArgs}(x: {className}{invariantArgs}): Option[{className}{invariantArgs}] =
    Some(x)
}}

/** {className} is a cartesian product of {i} component{s}.
 *  {descriptiveComment}
 *  @since 2.3
 */
trait {className}{covariantArgs} extends Product {{
  /**
   *  The arity of this product.
   *  @return {i}
   */
  override def productArity = {i}

  /**
   *  Returns the n-th projection of this product if 0&amp;lt;=n&amp;lt;arity,
   *  otherwise <code>null</code>.
   *
   *  @param n number of the projection to be returned
   *  @return  same as _(n+1)
   *  @throws  IndexOutOfBoundsException
   */
  @throws(classOf[IndexOutOfBoundsException])
  override def productElement(n: Int) = n match {{ {cases} }}

{proj}
{moreMethods}
}}
</file>}

}
