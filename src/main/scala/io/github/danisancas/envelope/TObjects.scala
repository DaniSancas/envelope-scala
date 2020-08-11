package io.github.danisancas.envelope

case class TChat(id: Int, `type`: String, title: Option[String], username: Option[String], first_name: Option[String],
                 last_name: Option[String])
case class TUser(id: Int, is_bot: Boolean, first_name: String, last_name: Option[String], username: Option[String],
                 language_code: Option[String])
case class TInMessage(message_id: Int, from: Option[TUser], date: Int, chat: TChat, forward_from: Option[TUser],
                      forward_from_chat: Option[TChat], forward_from_message_id: Option[Int],
                      forward_signature: Option[String], forward_date: Option[Int], reply_to_message: Option[TInMessage],
                      edit_date: Option[Int], text: Option[String])
case class TUpdate(update_id: Int, message: Option[TInMessage], edited_message: Option[TInMessage],
                   channel_post: Option[TInMessage], edited_channel_post: Option[TInMessage])

case class TOutMessage(text: String, chat_id: Int, reply_to_message_id: Option[Int] = None,
                           method: String = "sendMessage", parse_mode: Option[String] = None,
                           disable_web_page_preview: Option[Boolean] = None,
                           disable_notification: Option[Boolean] = None)
