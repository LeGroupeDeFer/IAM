package be.unamur.infom453.iam

import org.scalatest.FunSuite

class MainTest extends FunSuite {
  test("healthcheck") {
    pending
    //assert(Main.healthcheck(Input.get("/")).awaitValueUnsafe() == Some("OK"))
  }

  test("helloWorld") {
    pending
    //assert(Main.helloWorld(Input.get("/hello")).awaitValueUnsafe() == Some(Main.Message("World")))
  }

  test("hello") {
    pending
    //assert(Main.hello(Input.get("/hello/foo")).awaitValueUnsafe() == Some(Main.Message("foo")))
  }
}