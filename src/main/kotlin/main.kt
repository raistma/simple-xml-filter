import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.XMLFilterImpl
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult

fun main(args: Array<String>) {
    val tagName = args[0]
    val attrName = args[1]
    val pattern = args[2]
    val filePath = args[3]
    xmlTest(tagName, attrName, pattern, filePath)
}

fun xmlTest(tagName: String, attrName: String, tcPattern: String, filePath: String) {
    val regex = Regex(tcPattern)
    val reader = object : XMLFilterImpl(SAXParserFactory.newInstance().newSAXParser().xmlReader) {
        var skip = false
        override fun startElement(uri: String?, localName: String?, qName: String?, atts: Attributes?) {
            if (!skip) {
                if (qName == tagName) {
                    atts?.let {
                        repeat(atts.length) {i ->
                            if (atts.getQName(i) == attrName) {
                                if(!regex.containsMatchIn(it.getValue(i))) {
                                    skip = true;
                                } else {
                                    super.startElement(uri, localName, qName, atts)
                                }
                            }
                        }

                    }
                } else
                    super.startElement(uri, localName, qName, atts)
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            if(!skip) {
                super.endElement(uri, localName, qName)
            } else if(qName == tagName) {
                skip = false
            }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            if(!skip)
                super.characters(ch, start, length)
        }
    }
    val source = SAXSource(reader, InputSource(filePath))
    val result = StreamResult(System.out)
    TransformerFactory.newInstance().newTransformer().transform(source, result)
}