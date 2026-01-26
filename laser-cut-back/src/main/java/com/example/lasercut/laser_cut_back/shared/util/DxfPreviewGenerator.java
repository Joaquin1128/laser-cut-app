package com.example.lasercut.laser_cut_back.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.kabeja.svg.SVGGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.lasercut.laser_cut_back.exception.PreviewGenerationException;

public class DxfPreviewGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DxfPreviewGenerator.class);

    private static final String PIEZA_CLASS = "pieza-principal";
    private static final String PIEZA_FILL = "#e0e0e0";
    private static final String CIRCLE_FILL = "#ffffff";
    private static final String FILL_RULE = "evenodd";
    private static final String SVG_TAG = "<svg";

    private static final String STYLE_BLOCK = "<style>." + PIEZA_CLASS + "{fill:" + PIEZA_FILL + ";fill-rule:" + FILL_RULE + "} circle{fill:" + CIRCLE_FILL + "}</style>";

    private static final Pattern PATH_PATTERN = Pattern.compile("<path\\b[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern D_ATTR_PATTERN = Pattern.compile("\\bd\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern NUM_PATTERN = Pattern.compile("[-+]?(?:\\d*\\.\\d+|\\d+)(?:[eE][-+]?\\d+)?");
    private static final Pattern CLASS_ATTR_PATTERN = Pattern.compile("\\bclass\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
    private static final String PATH_OPEN_REGEX = "<path\\b";

    private static class MatchInfo {
        int start, end;
        String tag;
        MatchInfo(int s, int e, String t) { start = s; end = e; tag = t; }
    }

    private static String marcarPathExterior(String svg, String className) {
        try {
            Matcher m = PATH_PATTERN.matcher(svg);

            List<MatchInfo> matches = new ArrayList<>();
            double bestArea = -1.0;
            int bestIndex = -1;

            int idx = 0;
            while (m.find()) {
                String tag = m.group();
                double area = areaAproxDePath(tag);
                matches.add(new MatchInfo(m.start(), m.end(), tag));
                if (area > bestArea) {
                    bestArea = area;
                    bestIndex = idx;
                }
                idx++;
            }

            if (bestIndex >= 0 && bestIndex < matches.size()) {
                MatchInfo best = matches.get(bestIndex);
                String modTag = agregarClaseAlPath(best.tag, className);
                if (!modTag.equals(best.tag)) {
                    StringBuilder sb = new StringBuilder(svg);
                    sb.replace(best.start, best.end, modTag);
                    return sb.toString();
                }
            }
        } catch (Exception ignore) {
        }
        return svg;
    }

    private static double areaAproxDePath(String pathTag) {
        try {
            Matcher dm = D_ATTR_PATTERN.matcher(pathTag);
            if (!dm.find()) return -1.0;
            String d = dm.group(1);

            Matcher nm = NUM_PATTERN.matcher(d);
            List<Double> nums = new ArrayList<>();
            while (nm.find()) {
                try {
                    nums.add(Double.parseDouble(nm.group()));
                } catch (NumberFormatException ignored) {}
            }
            if (nums.size() < 4) return -1.0;

            double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
            for (int i = 0; i + 1 < nums.size(); i += 2) {
                double x = nums.get(i);
                double y = nums.get(i + 1);
                if (x < minX) minX = x; if (x > maxX) maxX = x;
                if (y < minY) minY = y; if (y > maxY) maxY = y;
            }
            if (!Double.isFinite(minX) || !Double.isFinite(maxX) || !Double.isFinite(minY) || !Double.isFinite(maxY)) return -1.0;
            double w = Math.max(0, maxX - minX);
            double h = Math.max(0, maxY - minY);
            double area = w * h;
            return Double.isFinite(area) ? area : -1.0;
        } catch (Exception e) {
            return -1.0;
        }
    }

    private static String agregarClaseAlPath(String pathTag, String className) {
        try {
            Matcher cm = CLASS_ATTR_PATTERN.matcher(pathTag);
            if (cm.find()) {
                String classes = cm.group(1);
                if (classes != null && classes.contains(className)) return pathTag; // ya la tiene
                String nuevas = classes == null || classes.isEmpty() ? className : (classes + " " + className);
                return pathTag.substring(0, cm.start(1)) + nuevas + pathTag.substring(cm.end(1));
            } else {
                return pathTag.replaceFirst(PATH_OPEN_REGEX, "<path class=\"" + className + "\"");
            }
        } catch (Exception e) {
            return pathTag;
        }
    }

    public static String generarVistaPreviaBase64(InputStream dxfInputStream) {
        try {
            Parser parser = ParserBuilder.createDefaultParser();
            parser.parse(dxfInputStream, "");
            DXFDocument doc = parser.getDocument();

            if (doc == null) {
                logger.warn("DXFDocument es nulo, no se puede generar preview");
                throw new PreviewGenerationException("El documento DXF es nulo o no se pudo parsear.", null);
            }

            ByteArrayOutputStream svgOutput = new ByteArrayOutputStream();
            SVGGenerator generator = new SVGGenerator();

            SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = tf.newTransformerHandler();
            handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            handler.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            handler.setResult(new StreamResult(svgOutput));

            generator.generate(doc, handler, new HashMap<>());

            String svg = svgOutput.toString(StandardCharsets.UTF_8);
            if (svg.contains("NaN") || svg.trim().isEmpty()) {
                logger.warn("SVG generado inválido o vacío");
                throw new PreviewGenerationException("SVG generado vacío o inválido (contiene NaN)", null);
            }

            int svgTagStart = svg.indexOf(SVG_TAG);
            if (svgTagStart >= 0) {
                int svgTagEnd = svg.indexOf('>', svgTagStart);
                if (svgTagEnd > svgTagStart) {
                    svg = svg.substring(0, svgTagEnd + 1) + STYLE_BLOCK + svg.substring(svgTagEnd + 1);
                }
            }

            svg = marcarPathExterior(svg, "pieza-principal");

            return Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            logger.error("Error generando vista previa DXF", e);
            throw new PreviewGenerationException("Error generando vista previa DXF", e);
        }
    }

}
