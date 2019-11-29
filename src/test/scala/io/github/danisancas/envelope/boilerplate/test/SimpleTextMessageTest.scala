package io.github.danisancas.envelope.boilerplate.test

import io.github.danisancas.envelope.boilerplate.SimpleTextMessage
import org.scalatest.FlatSpec

class SimpleTextMessageTest extends FlatSpec{

  "stripCommand function" should "strip command and the rest text" in {
    assert(SimpleTextMessage.stripCommand("/something") == Some("/something", ""))
    assert(SimpleTextMessage.stripCommand("/something ") == Some("/something", ""))
    assert(SimpleTextMessage.stripCommand("/something else") == Some("/something", "else"))
    assert(SimpleTextMessage.stripCommand("/something else with more text") == Some("/something", "else with more text"))
    assert(SimpleTextMessage.stripCommand(null).isEmpty)
    assert(SimpleTextMessage.stripCommand("").isEmpty)
    assert(SimpleTextMessage.stripCommand(" ").isEmpty)
    assert(SimpleTextMessage.stripCommand("  ").isEmpty)
    assert(SimpleTextMessage.stripCommand("/") == Some("", "/"))
    assert(SimpleTextMessage.stripCommand("//") == Some("", "//"))
    assert(SimpleTextMessage.stripCommand(" //") == Some("", "//"))
    assert(SimpleTextMessage.stripCommand(" / /") == Some("", "/ /"))
    assert(SimpleTextMessage.stripCommand("not a command") == Some("", "not a command"))
  }
}
