package nagoda.simpleparser

import SimpleParserCore._
import cats.implicits._

class SimpleParserCoreSpec extends UnitSpec {
  "detectDelimiter" should "return PIPE when file is PIPE delimited" in {
    val line = TEST_DATA(0).mkString(DELIMITERS(PIPE).toString)
    detectDelimiter(line).success.value should equal (DELIMITERS(PIPE))
  }

  "detectDelimiter" should "return COMMA when file is COMMA delimited" in {
    val line = TEST_DATA(0).mkString(DELIMITERS(COMMA).toString)
    detectDelimiter(line).success.value should equal (DELIMITERS(COMMA))
  }

  "detectDelimiter" should "return SPACE when file is SPACE delimited" in {
    val line = TEST_DATA(0).mkString(DELIMITERS(SPACE).toString)
    detectDelimiter(line).success.value should equal (DELIMITERS(SPACE))
  }

  "detectDelimiter" should "throw Exception when delimiter is unknown" in {
    val UNKNOWN_DELIMITER = ":"
    val line = TEST_DATA(0).mkString(UNKNOWN_DELIMITER)
    detectDelimiter(line).failure.exception should have message UNKNOWN_DELIMITER_MSG
  }

  "parse" should "return a List of UserData objects" in {
    val rawData = createRawData(TEST_DATA, DELIMITERS(PIPE))
    val userData = rawData.map(parseLine(_, DELIMITERS(PIPE))).sequence.success.value
    userData.length should equal (TEST_DATA.length)
    userData.zip(TEST_DATA).foreach {
      case (userData, expectedData) => {
        userData shouldBe a [UserData]
        userData.lastName should equal(expectedData(0))
        userData.firstName should equal(expectedData(1))
        userData.email should equal(expectedData(2))
        userData.favoriteColor should equal(expectedData(3))
        userData.dateOfBirth should equal(DEFAULT_DATE_FORMAT.parse(expectedData(4)))
      }
    }
  }

  "parse" should "throw Exception when date is in unexpected format" in {
    val rawData = createRawData(BAD_DATE_TEST_DATA, DELIMITERS(PIPE))
    rawData.map(parseLine(_, DELIMITERS(PIPE))).sequence.failure.exception shouldBe a [Throwable]
  }
}
