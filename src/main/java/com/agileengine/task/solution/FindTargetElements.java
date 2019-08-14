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

        Optional<Element> elementById = findElementById(originalDoc, elementId);

        Optional<String> attributesOpt = elementById.map(button ->
                button.attributes().asList().stream()
                        .map(attr -> attr.getKey() + " = " + attr.getValue())
                        .collect(Collectors.joining(", "))
        );
        String[] ss = attributesOpt.map(attrs -> attrs.split(",\\s")).orElseGet(() -> new String[1]);
        Map<String, String> attributes = Arrays.stream(ss)
                .collect(Collectors.toMap(a -> a.split(" = ")[0], b -> b.split(" = ")[1]));

        searchForElement(diffDoc, attributes);
    }

    public static void main(String[] args) throws IOException {

        String original = Optional.ofNullable(args[0])
                .orElseThrow(() -> new RuntimeException("Link to the original file is not provided"));

        String diff = Optional.ofNullable(args[1])
                .orElseThrow(() -> new RuntimeException("Link to the diff file is not provided"));

        parse(original, diff);
    }

    private static Optional<Element> findElementById(Document document, String targetElementId) {
        return Optional.of(document.getElementById(targetElementId));
    }

    private static void searchForElement(Document document, Map<String, String> attributes) {
        attributes.forEach((k, v) -> {
            Elements elementsByAttribute = document.getElementsByAttribute(k);

            elementsByAttribute.stream()
                    .filter(element -> element.attributes().asList()
                            .stream()
                            .anyMatch(attribute -> attribute.getKey().equals(k) && attribute.getValue().equals(v)))
                    .forEach(foundElement -> {
                        StringBuilder path = new StringBuilder();
                        Element parent = foundElement.parent();
                        path.append(foundElement.tag().getName()).append(" > ");
                        while (parent != null) {
                            parent = parent.parent();
                            path.append(Optional.ofNullable(parent)
                                    .map(p -> p.tag().getName() + " > ")
                                    .orElse(""));
                        }
                        System.out.println("Path to found element = " + path);
                    });
        });
    }
}