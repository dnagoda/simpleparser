package nagoda.simpleparser

import io.circe.{Decoder, HCursor}
import nagoda.simpleparser.SimpleParserCore._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.scalatest.BeforeAndAfterEach

class SimpleParserAPISpec extends UnitSpec with BeforeAndAfterEach {
  val service = SimpleParserAPI.service

  override def afterEach() {
    SimpleStore.buffer.clear()
  }

  "POST records" should "respond with BadRequest when no body is provided" in {
    val response = service.run(Request(method=Method.POST, uri=uri"/records")).unsafeRunSync
    response.status should equal (Status.BadRequest)
  }

  "POST records" should "respond with BadRequest when an invalid body is provided" in {
    val invalidBody = BAD_DATE_TEST_DATA.mkString(DELIMITERS(PIPE).toString)
    val response = service.run(Request(method=Method.POST, uri=uri"/records").withEntity(invalidBody)).unsafeRunSync
    response.status should equal (Status.BadRequest)
  }

  val validBody = TEST_DATA.head.mkString(DELIMITERS(PIPE).toString)

  "POST records" should "respond with OK when a valid body is provided" in {
    val response = service.run(Request(method=Method.POST, uri=uri"/records").withEntity(validBody)).unsafeRunSync()
    response.status should equal (Status.Ok)
  }

  "POST records" should "add records to local buffer" in {
    SimpleStore.buffer.length should equal (0)
    val response = service.run(Request(method=Method.POST, uri=uri"/records").withEntity(validBody)).unsafeRunSync()
    SimpleStore.buffer.length should equal (1)
    val record = SimpleStore.buffer.head
    record.lastName should equal (TEST_DATA.head(0))
    record.firstName should equal (TEST_DATA.head(1))
    record.email should equal (TEST_DATA.head(2))
    record.favoriteColor should equal (TEST_DATA.head(3))
    DEFAULT_DATE_FORMAT.format(record.dateOfBirth) should equal (TEST_DATA.head(4))
  }

  val sortableData = Seq(
      UserData("IsDead", "Zeb", "zeb@hotmail.org", "Black", DEFAULT_DATE_FORMAT.parse("12/21/1972")),
      UserData("Lastname", "Firstname", "abe@hotmail.com", "Pink", DEFAULT_DATE_FORMAT.parse("3/21/1975")),
      UserData("Vin", "Mel", "mel@hotmail.net", "Orange", DEFAULT_DATE_FORMAT.parse("6/1/1967"))
  )

  implicit val decodeUserData: Decoder[UserData] = new Decoder[UserData] {
    final def apply(c: HCursor): Decoder.Result[UserData] =
      for {
        lastName <- c.downField("lastName").as[String]
        firstName <- c.downField("firstName").as[String]
        email <- c.downField("email").as[String]
        favoriteColor <- c.downField("favoriteColor").as[String]
        dateOfBirth <- c.downField("dateOfBirth").as[String]
      } yield {
        UserData(lastName, firstName, email, favoriteColor, DEFAULT_DATE_FORMAT.parse(dateOfBirth))
      }
  }

  "GET records/email" should "return a JSON list of records sorted by email" in {
    SimpleStore.buffer.appendAll(sortableData)
    val response = service.run(Request(method=Method.GET, uri=uri"/records/email")).unsafeRunSync()
    val responseData = response.asJsonDecode[List[UserData]].unsafeRunSync()
    responseData.length shouldBe (3)
    responseData(0).email shouldBe ("abe@hotmail.com")
    responseData(1).email shouldBe ("mel@hotmail.net")
    responseData(2).email shouldBe ("zeb@hotmail.org")
  }

  "GET records/birthdate" should "return a JSON list of records sorted by date of birth" in {
    SimpleStore.buffer.appendAll(sortableData)
    val response = service.run(Request(method=Method.GET, uri=uri"/records/birthdate")).unsafeRunSync()
    val responseData = response.asJsonDecode[List[UserData]].unsafeRunSync()
    responseData.length shouldBe (3)
    responseData(0).dateOfBirth shouldBe (DEFAULT_DATE_FORMAT.parse("6/1/1967"))
    responseData(1).dateOfBirth shouldBe (DEFAULT_DATE_FORMAT.parse("12/21/1972"))
    responseData(2).dateOfBirth shouldBe (DEFAULT_DATE_FORMAT.parse("3/21/1975"))
  }

  "GET records/name" should "return a JSON list of records sorted by name (lastname, firstname)" in {
    SimpleStore.buffer.appendAll(sortableData)
    SimpleStore.buffer.append(UserData(
      "Vin", "Al", "al@libertyrecords.com", "red", DEFAULT_DATE_FORMAT.parse("1/1/1958")
    ))
    val response = service.run(Request(method=Method.GET, uri=uri"/records/name")).unsafeRunSync()
    val responseData = response.asJsonDecode[List[UserData]].unsafeRunSync()
    responseData.length shouldBe (4)
    responseData(0).lastName shouldBe ("IsDead")
    responseData(1).lastName shouldBe ("Lastname")
    responseData(2).lastName shouldBe ("Vin")
    responseData(2).firstName shouldBe ("Al")
    responseData(3).lastName shouldBe ("Vin")
    responseData(3).firstName shouldBe ("Mel")
  }
}
