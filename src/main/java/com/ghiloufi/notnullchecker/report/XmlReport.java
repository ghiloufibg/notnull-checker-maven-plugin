package com.ghiloufi.notnullchecker.report;

import com.ghiloufi.notnullchecker.validation.ViolationCandidate;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlReport {

    public void write(Path outputXml, List<ViolationCandidate> violations) {

        Map<String, List<ViolationCandidate>> byFile =
                violations.stream()
                        .collect(Collectors.groupingBy(ViolationCandidate::fileName,
                                LinkedHashMap::new,
                                Collectors.toList()));

        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        try (OutputStream os = Files.newOutputStream(outputXml)) {
            XMLStreamWriter w = factory.createXMLStreamWriter(os, "UTF-8");

            w.writeStartDocument("UTF-8", "1.0");
            w.writeStartElement("checkstyle");
            w.writeAttribute("version", "1.0.0");

            for (var entry : byFile.entrySet()) {
                w.writeStartElement("file");
                w.writeAttribute("name", entry.getKey());

                for (ViolationCandidate v : entry.getValue()) {
                    String line = extractLine(v.position());
                    w.writeEmptyElement("error");
                    w.writeAttribute("line", line);
                    w.writeAttribute("severity", "error");
                    w.writeAttribute("message", v.kind().value() + " – " + v.name());
                    w.writeAttribute("source", "com.ghiloufi.notnullchecker");
                }

                w.writeEndElement();
            }

            w.writeEndElement();
            w.writeEndDocument();
            w.flush();

        } catch (XMLStreamException | IOException e) {
            throw new UncheckedIOException(new IOException("Impossible de générer le XML", e));
        }
    }

    private String extractLine(String position) {
        return position.split("[:-]")[0];
    }
}
