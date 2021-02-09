package nagoda.simpleparser

import java.io.{File, FileWriter}

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers._

abstract class UnitSpec extends AnyFlatSpec with should.Matchers with TryValues with Inside with Inspectors {
  val TEST_DATA = List(
    List("aclastname","acfirstname","aaemail","white","1/1/2000"),
    List("ablastname","abfirstname","aaemail","white","1/1/2000"),
    List("blastname","bfirstname","bemail","blue","12/31/1999"),
    List("alastname","bfirstname","bemail","blue","12/31/1999"),
    List("zlastname","zfirstname","zemail","red","3/1/1975"),
    List("1lastname","1firstname","1email","pink","6/15/2010"),
    List("aalastname","aafirstname","aaemail","white","1/1/2000")
  )

  val BAD_DATE_TEST_DATA = List(
    List("lastname", "firstname", "email", "white", "20000101"),
  )

  val TEST_FILENAME = "tmp_test_file"

  def createRawData(sourceData: List[List[String]], delimiter: Char): List[String] = {
    sourceData.map(_.mkString(delimiter.toString))
  }

  def write(filename: String, content: String): Unit = {
    val writer = new FileWriter(filename)
    try {
      writer.write(content)
    } finally {
      writer.close
    }
  }

  def delete(filename: String): Unit = {
    val file = new File(filename)
    file.delete()
  }

  def withFile(sourceData: List[List[String]], delimiter: Char)(test: String => Any) = {
    val content = createRawData(sourceData, delimiter).mkString("\n")
    write(TEST_FILENAME, content)
    test(TEST_FILENAME)
    delete(TEST_FILENAME)
  }
}
