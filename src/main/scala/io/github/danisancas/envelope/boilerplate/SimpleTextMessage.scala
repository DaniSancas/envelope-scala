package io.github.danisancas.envelope.boilerplate

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import io.circe.parser.decode
import io.github.danisancas.envelope.{TOutMessage, TUpdate, TelegramWrapper}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class TextMessage(chatId: Int, text: String, messageId: Int)

object SimpleTextMessage extends TelegramWrapper[APIGatewayProxyRequestEvent, TUpdate, TextMessage, TOutMessage, APIGatewayProxyResponseEvent]{

  import io.circe.Printer
  import io.circe.generic.auto._
  import io.circe.syntax._

  private val printer: Printer = Printer.noSpaces.copy(preserveOrder = true, dropNullValues = true)

  override def decodeInput(input: String): Try[TUpdate] = {
    decode[TUpdate](input).toTry // Decode request using circe
}

  override def convertToMessage(update: TUpdate): Try[TextMessage] = {
    val result = for {
      msg <- update.message
      text <- msg.text
    } yield TextMessage(msg.chat.id, text, msg.message_id)

    result match {
      case Some(a) => Success(a)
      case None => Failure(new Exception("Couldn't convert to simple TextMessage"))
    }
  }

  override def run(requestEvent: APIGatewayProxyRequestEvent)
                  (runCommand: TextMessage => Try[TOutMessage]): APIGatewayProxyResponseEvent = {
    val result: Try[TOutMessage] = for {
      update <- decodeInput(requestEvent.getBody)
      message <- convertToMessage(update)
      out <- runCommand(message)
    } yield out

    result match {
      case Success(msg) => sendOkResponse(msg)
      case Failure(error) => sendErrorResponse(error.getMessage)
    }
  }

  def sendOkResponse(message: TOutMessage): APIGatewayProxyResponseEvent = {
    val response = new APIGatewayProxyResponseEvent() // Example for a response
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "application/json").asJava)
      .withBody(printer.pretty(message.asJson))

    println(s"Body: $message")
    println(s"Response: $response")

    response
  }

  def sendErrorResponse(error: String): APIGatewayProxyResponseEvent = {
    val response = new APIGatewayProxyResponseEvent() // Example for a response
      .withStatusCode(200)
      .withBody(s"Error: $error")

    println(s"Response: $response")

    response
  }

  def stripCommand(text: String): Option[(String, String)] = {
    val cmdRegex = "(\\/[a-zA-Z0-9]+)? *(.*)".r

    text match {
      case cmdRegex(cmd, rest) if cmd == null && rest == "" => Option.empty
      case cmdRegex(cmd, rest) if cmd == null => Some("", rest)
      case cmdRegex(cmd, rest) => Some(cmd, rest)
      case _ => Option.empty
    }
  }
}
