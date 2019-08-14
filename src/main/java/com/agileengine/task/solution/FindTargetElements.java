package com.agileengine.task.solution;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FindTargetElements {

    private static void parse(String originalFilePath, String diffFIlePass) throws IOException {
        Document originalDoc = Jsoup.connect(originalFilePath).get();
        Document diffDoc = Jsoup.connect(diffFIlePass).get();
        String elementId = "make-everything-ok-button";

        Optional<Element> elementById = Optional.of(originalDoc.getElementById(elementId));

        Optional<String> attributesOpt = elementById.map(button ->
                button.attributes().asList().stream()
                        .map(attr -> attr.getKey() + " = " + attr.getValue())
                        .collect(Collectors.joining(", "))
        );
        String[] attributesKeyValueArray = attributesOpt.map(attrs -> attrs.split(",\\s"))
                .orElseGet(() -> new String[1]);
        Map<String, String> attributes = Arrays.stream(attributesKeyValueArray)
                .collect(Collectors.toMap(a -> a.split(" = ")[0], b -> b.split(" = ")[1]));

        System.out.println("Found path : " + searchForElement(diffDoc, attributes));
    }

    private static StringBuilder searchForElement(Document document, Map<String, String> attributes) {
        StringBuilder path = new StringBuilder();
        attributes.forEach((k, v) -> {
            Elements elementsByAttribute = document.getElementsByAttribute(k);

            elementsByAttribute.stream()
                    .filter(element -> element.attributes().asList()
                            .stream()
                            .anyMatch(attribute -> attribute.getKey().equals(k) && attribute.getValue().equals(v)))
                    .forEach(foundElement -> path.append(getElementPath(foundElement)));
        });
        return path;
    }

    private static StringBuilder getElementPath(Element element) {
        StringBuilder path = new StringBuilder();
        Element parent = element.parent();
        path.append(element.tag().getName()).append(" > ");
        while (parent != null) {
            parent = parent.parent();
            path.append(Optional.ofNullable(parent)
                    .map(p -> p.tag().getName() + " > ")
                    .orElse(""));
        }
        return path;
    }

    public static void main(String[] args) throws IOException {

        String original = Optional.ofNullable(args[0])
                .orElseThrow(() -> new RuntimeException("Link to the original file is not provided"));

        String diff = Optional.ofNullable(args[1])
                .orElseThrow(() -> new RuntimeException("Link to the diff file is not provided"));

        parse(original, diff);
    }
}