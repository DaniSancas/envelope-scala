package io.github.danisancas.envelope

import scala.util.Try

trait TelegramWrapper[Req, Upd, Msg, Out, Resp] {

  def decodeInput(input: String): Try[Upd]

  def convertToMessage(update: Upd): Try[Msg]

  def run(request: Req)(message: Msg => Try[Out]): Resp
}
