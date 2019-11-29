package io.github.danisancas.envelope.boilerplate

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser.decode
import io.github.danisancas.envelope.{TOutMessage, TUpdate, TelegramWrapper}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class TextMessage(chatId: Int, text: String, messageId: Int)

object SimpleTextMessage
  extends TelegramWrapper[APIGatewayProxyRequestEvent, TUpdate, TextMessage, TOutMessage, APIGatewayProxyResponseEvent]
  with LazyLogging {

  import io.circe.Printer
  import io.circe.generic.auto._
  import io.circe.syntax._

  private val printer: Printer = Printer.noSpaces.copy(preserveOrder = true, dropNullValues = true)

  override def decodeInput(input: String): Try[TUpdate] = {
    logger.info("Decoding input JSON to TUpdate case class")
    logger.debug(s"Input: $input")
    val result = decode[TUpdate](input).toTry // Decode request using circe
    logger.debug(s"Decoded: $result")
    result
}

  override def convertToMessage(update: TUpdate): Try[TextMessage] = {
    logger.info("Converting TUpdate case class to TextMessage case class")
    logger.debug(s"TUpdate: $update")
    val result = for {
      msg <- update.message
      text <- msg.text
    } yield TextMessage(msg.chat.id, text, msg.message_id)

    result match {
      case Some(a) =>
        logger.debug(s"Converted: $result")
        Success(a)
      case None =>
        val error = "Couldn't convert to simple TextMessage"
        logger.error(error)
        Failure(new Exception(error))
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
    logger.info("Sending Ok Response")
    val response = new APIGatewayProxyResponseEvent() // Example for a response
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "application/json").asJava)
      .withBody(printer.pretty(message.asJson))

    logger.debug(s"Body: $message")
    logger.debug(s"Response: $response")

    response
  }

  def sendErrorResponse(error: String): APIGatewayProxyResponseEvent = {
    logger.info("Sending Error Response")
    val response = new APIGatewayProxyResponseEvent() // Example for a response
      .withStatusCode(200)
      .withBody(s"Error: $error")

    logger.debug(s"Response: $response")

    response
  }

  def stripCommand(text: String): Option[(String, String)] = {
    logger.info(s"Parsing command for string '$text'")
    val cmdRegex = "(\\/[a-zA-Z0-9]+)? *(.*)".r

    val result = text match {
      case cmdRegex(cmd, rest) if cmd == null && rest == "" => Option.empty
      case cmdRegex(cmd, rest) if cmd == null => Some("", rest)
      case cmdRegex(cmd, rest) => Some(cmd, rest)
      case _ => Option.empty
    }

    logger.debug(s"Parsed result: $result")

    result
  }
}
