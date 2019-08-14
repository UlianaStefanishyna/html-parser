package com.agileengine;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FindTargetElements {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupFindByIdSnippet.class);

    private static void parse(String originalFilePath, String diffFIlePass) throws IOException {
        Document originalDoc = Jsoup.connect(originalFilePath).get();
        Document diffDoc = Jsoup.connect(diffFIlePass).get();
        String elementId = "make-everything-ok-button";

        Optional<Element> elementById = findElementById(originalDoc, elementId);
        Optional<String> stringifiedAttributesOpt = elementById.map(button ->
                button.attributes().asList().stream()
                        .map(attr -> attr.getKey() + " = " + attr.getValue())
                        .collect(Collectors.joining(", "))
        );
        String[] ss = stringifiedAttributesOpt.map(attrs -> {
            return attrs.split(",\\s");

        }).orElseGet(() -> new String[1]);
        Map<String, String> attributes = Arrays.stream(ss)
                .collect(Collectors.toMap(a -> a.split(" = ")[0], b -> b.split(" = ")[1]));
        searchForElement(diffDoc, attributes);
    }

    public static void main(String[] args) throws IOException {
        String original = "https://agileengine.bitbucket.io/beKIvpUlPMtzhfAy/samples/sample-0-origin.html";
        String diff = "https://agileengine.bitbucket.io/beKIvpUlPMtzhfAy/samples/sample-2-container-and-clone.html";

        parse(original, diff);
    }

    private static Optional<Element> findElementById(Document document, String targetElementId) {
        return Optional.of(document.getElementById(targetElementId));
    }

    private static void searchForElement(Document document, Map<String, String> attributes) {
        attributes.forEach((k, v) -> {
            Elements elementsByAttribute = document.getElementsByAttribute(k);
            elementsByAttribute.stream().filter(element -> Optional.of(element.getElementById(v)).isPresent()).forEach(System.out::println);
        });
    }
}