package nagoda.simpleparser

import cats.effect._
import io.circe._
import io.circe.syntax._
import nagoda.simpleparser.SimpleParserCore._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success}

object SimpleStore {
  val buffer = new mutable.ArrayBuffer[UserData]()
}

object SimpleParserAPI extends IOApp {
  val logger = LoggerFactory.getLogger("SimpleParserAPI")

  implicit val encodeUserData: Encoder[UserData] = new Encoder[UserData] {
    final def apply(ud: UserData): Json = Json.obj(
      ("lastName", Json.fromString(ud.lastName)),
      ("firstName", Json.fromString(ud.firstName)),
      ("email", Json.fromString(ud.email)),
      ("favoriteColor", Json.fromString(ud.favoriteColor)),
      ("dateOfBirth", Json.fromString(DEFAULT_DATE_FORMAT.format(ud.dateOfBirth)))
    )
  }

  def postRecord(record: IO[String]) = {
    record flatMap {
      case sourceLine: String if sourceLine.nonEmpty => {
        detectDelimiter(sourceLine).flatMap(delimiter => parseLine(sourceLine, delimiter)) match {
          case Success(record) => {
            SimpleStore.buffer.append(record)
            Ok()
          }
          case Failure(e) => {
            // todo: check for individual errors
            logger.error(s"Unable to post record: $sourceLine", e)
            BadRequest()
          }
        }
      }
      case _ => BadRequest("Missing POST body!")
    }
  }

  def getRecords(sorter: List[UserData] => List[UserData]) = {
    Ok(sorter(SimpleStore.buffer.toList).asJson)
  }

  val service = HttpRoutes.of[IO] {
    case req @ POST -> Root / "records" => postRecord(req.as[String])
    case GET -> Root / "records" / "email" => getRecords(ud => ud.sortBy(_.email))
    case GET -> Root / "records" / "birthdate" => getRecords(ud => ud.sortBy(_.dateOfBirth))
    case GET -> Root / "records" / "name" => getRecords(ud => ud.sortBy(ud => (ud.lastName, ud.firstName)))
  }.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO](global)
      .withHttpApp(service)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
