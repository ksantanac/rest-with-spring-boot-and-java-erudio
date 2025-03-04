package br.com.erudio.controllers;

import br.com.erudio.exception.UnsupportedMathOperationException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("math")
public class MathController {

    // SOMAR
    @RequestMapping("/sum/{n1}/{n2}")
    public Double sum(
            @PathVariable("n1") String n1,
            @PathVariable("n2") String n2
    ) throws Exception {

        if (!isNumeric(n1) || !isNumeric(n2)) throw new UnsupportedMathOperationException("Please set a numeric value!");
        return convertToDouble(n1) + convertToDouble(n2);
    }

    // SUBTRAIR
    @RequestMapping("/sub/{n1}/{n2}")
    public Double sub(
            @PathVariable("n1") String n1,
            @PathVariable("n2") String n2
    ) throws Exception {

        if (!isNumeric(n1) || !isNumeric(n2)) throw new UnsupportedMathOperationException("Please set a numeric value!");
        return convertToDouble(n1) - convertToDouble(n2);
    }

    // MULTIPLICAR
    @RequestMapping("/mult/{n1}/{n2}")
    public Double mult(
            @PathVariable("n1") String n1,
            @PathVariable("n2") String n2
    ) throws Exception {

        if (!isNumeric(n1) || !isNumeric(n2)) throw new UnsupportedMathOperationException("Please set a numeric value!");
        return convertToDouble(n1) * convertToDouble(n2);
    }

    // DIVIDIR
    @RequestMapping("/div/{n1}/{n2}")
    public Double div(
            @PathVariable("n1") String n1,
            @PathVariable("n2") String n2
    ) throws Exception {

        if (!isNumeric(n1) || !isNumeric(n2)) throw new UnsupportedMathOperationException("Please set a numeric value!");
        return convertToDouble(n1) / convertToDouble(n2);
    }

    // MEDIA
    @RequestMapping("/mean/{n1}/{n2}")
    public Double mean(
            @PathVariable("n1") String n1,
            @PathVariable("n2") String n2
    ) throws Exception {

        if (!isNumeric(n1) || !isNumeric(n2)) throw new UnsupportedMathOperationException("Please set a numeric value!");
        return (convertToDouble(n1) + convertToDouble(n2)) / 2;
    }

    // RAIZ
    @RequestMapping("/sqr/{number}")
    public Double sqr(@PathVariable("number") String number) throws Exception {

        if (!isNumeric(number)) throw new UnsupportedMathOperationException("Please set a numeric value!");
        return Math.sqrt(convertToDouble(number));
    }

    // =============================

    private Double convertToDouble(String strNumber) throws IllegalArgumentException {
        if (strNumber == null || strNumber.isEmpty()) throw new UnsupportedMathOperationException("Please set a numeric value!");
        String number = strNumber.replace(",", ".");

        return Double.parseDouble(number);
    }

    private boolean isNumeric(String strNumber) {
        if (strNumber == null || strNumber.isEmpty()) return false;
        String number = strNumber.replace(",", ".");

        return number.matches("[-+]?[0-9]*\\.?[0-9]+");
    }

}
