import weaver.*

object EtaExpansionSuite extends SimpleIOSuite {
  pureTest("Eta expansion example with add") {
    def add(x: Int, y: Int) = x + y
    val addFunction         = add
    val addFunctionCurried  = add.curried

    expect.all(
      add(1, 1) == 2,
      addFunction(1, 2) == 3,
      addFunctionCurried(1)(4) == 5,
    )
  }

  pureTest("Partially-applied functions (PAFs) without multiple parameter groups") {
    def wrap(prefix: String, html: String, suffix: String) =
      prefix + html + suffix

    val html = wrap("<html>", _: String, "</html>")
    val body = wrap("<body>", _: String, "</body>")
    val div  = wrap("<div>", _: String, "</div>")
    val h1   = wrap("<h1>", _: String, "</h1>")
    val p    = wrap("<p>", _: String, "</p>")

    val composed = html compose body compose div compose p

    val template: String => String => String =
      title =>
        paragraph =>
          html(
            body(
              h1(title)
                + div(
                  p(paragraph),
                ),
            ),
          )

    val docHeader = h1
    val docBody   = div compose p
    val doc       = html compose body

    val template2: String => String => String =
      _header =>
        _body =>
          doc(
            docHeader(_header) +
              docBody(_body),
          )

    expect.all(
      wrap("<div>", "Hello, world", "</div>") == "<div>Hello, world</div>",
      div("Hello, world") == "<div>Hello, world</div>",
      html(body(div(p("Hello, world")))) == "<html><body><div><p>Hello, world</p></div></body></html>",
      composed("Hello, world") == "<html><body><div><p>Hello, world</p></div></body></html>",
      template("Header 1")("Hello, world") == "<html><body><h1>Header 1</h1><div><p>Hello, world</p></div></body></html>",
      template2("Header 1")("Hello, world") == "<html><body><h1>Header 1</h1><div><p>Hello, world</p></div></body></html>",
    )
  }
}
