package be.unamur.infom453.iam

import java.io.InputStream
import scala.io.Source
import cats.effect.{ContextShift, IO}
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.io.Buf


class App(implicit S: ContextShift[IO]) extends Endpoint.Module[IO] {

  case class Message(name: String)

  def index: Endpoint[IO, Buf] = get(pathEmpty) {
    val stream: InputStream = getClass.getResourceAsStream("/index.html")
    val content = Source.fromInputStream(stream).mkString
    Ok(Buf.Utf8(content))
  }

  def indexPage = classpathAsset("/index.html")
  def cssGlobal = classpathAsset("/global.css")
  def cssBundle = classpathAsset("/build/bundle.css")
  def jsBundle  = classpathAsset("/build/bundle.js")

  def hello: Endpoint[IO, Message] = get("hello" :: path[String]) { name: String =>
    Ok(Message(name))
  }

  final def service: Service[Request, Response] = Bootstrap
    .serve[Text.Html](index)
    .serve[Text.Css](cssGlobal :+: cssBundle)
    .serve[Application.Javascript](jsBundle)
    .serve[Application.Json](hello)
    .toService

}
