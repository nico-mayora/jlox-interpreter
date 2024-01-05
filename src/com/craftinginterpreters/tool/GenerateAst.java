package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static java.lang.StringTemplate.STR;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        ));
    }

    private static void defineAst(
            String outputDir, String baseName, List<String> types
    ) throws IOException {
        String path = STR."\{outputDir}/\{baseName}.java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("\tabstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineType(
            PrintWriter writer, String baseName, String className, String fieldList
    ) {
        writer.println(STR."\tpublic static class \{className} extends \{baseName} {");

        // Constructor
        writer.println(STR."\t\t\{className}(\{fieldList}) {");

        // Store parameters in fields
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println(STR."\t\t\tthis.\{name} = \{name};");
        }

        writer.println("\t\t}");

        // Visitor pattern
        writer.println();
        writer.println("\t\t@Override");
        writer.println("\t\t<R> R accept(Visitor<R> visitor) {");
        writer.println(STR."\t\t\treturn visitor.visit\{className}\{baseName}(this);");
        writer.println("\t\t}");

        // Fields
        writer.println();
        for (String field : fields) {
            writer.println(STR."\t\tfinal \{field};");
        }

        writer.println("\t}");
    }

    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types
    ) {
        writer.println("\tinterface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(STR."\t\tR visit\{typeName}\{baseName}(\{typeName} \{baseName.toLowerCase()});");
        }

        writer.println("\t}");
    }
}
