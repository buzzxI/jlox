package icu.buzz.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar GenerateAst.jar [output_directory]");
            System.exit(64);
        }
        List<String> classes = defineAst(args[0], "Expr",
                Arrays.asList("Binary: Expr left, Token operator, Expr right",
                        "Unary: Token operator, Expr right",
                        "Grouping: Expr expression",
                        "Literal: Object value"));
        defineVisitor(args[0], "Expr", classes);
        defineVisitor(args[0], "Stmt", classes);
    }

    /**
     * this function will create @param: target.java under the directory @param: dir
     * target.java contains multiple inner static class specified by @param: types
     * @param dir base directory contains target file
     * @param target target file name
     * @param types specify inner static classes
     */
    public static List<String> defineAst(String dir, String target, List<String> types) {
        String full_path = dir + "/" + target + ".java";
        List<String> classes = null;

        try (PrintWriter writer = new PrintWriter(full_path, StandardCharsets.UTF_8)) {
            writer.println("package icu.buzz.lox;");
            writer.println();
            writer.println("public abstract class " + target + " {");

            classes = new ArrayList<>(types.size());
            List<String> fields = new ArrayList<>(types.size());
            for (String type : types) {
                String[] vars = type.split(":");
                classes.add(vars[0].trim());
                fields.add(vars[1].trim());
            }

            // visitor pattern
            writer.println("    public abstract <R> R accept(" + target + "Visitor<R> visitor);");
            writer.println();

            for (int i = 0; i < types.size(); i++) defineType(classes.get(i), target, fields.get(i), writer);

            writer.println("}");
        } catch (IOException e) {
            System.out.println("writer opens: " + full_path + " fail");
        }
        return classes;
    }

    /**
     * write an inner static class
     * @param className just follow the name
     * @param parentName inner class extends outer class
     * @param fields inner class fields
     * @param writer writer to print
     */
    private static void defineType(String className, String parentName, String fields, PrintWriter writer) {
        writer.println("    public static class " + className + " extends " + parentName + " {");
        String[] arrayField = fields.split(",");
        for (int i = 0; i < arrayField.length; i++) arrayField[i] = arrayField[i].trim();
        for (String field : arrayField) {
            writer.println("        private final " + field + ";");
            writer.println();
        }
        List<String> types = new ArrayList<>(arrayField.length);
        List<String> vars = new ArrayList<>(arrayField.length);

        // constructor
        writer.println("        public " + className + "(" + fields + ") {");
        for (String field : arrayField) {
            String[] tmp = field.split(" ");
            types.add(tmp[0].trim());
            tmp[1] = tmp[1].trim();
            vars.add(tmp[1]);
            writer.println("            this." + tmp[1] + " = " + tmp[1] + ";");
        }
        writer.println("        }");
        writer.println();

        // Visitor pattern.
        writer.println("        @Override");
        writer.println("        public <R> R accept(" + parentName + "Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + parentName + "(this);");
        writer.println("        }");
        writer.println();

        // Getters
        writer.println("        // getters");
        for (int i = 0; i < arrayField.length; i++) {
            char[] chars = vars.get(i).toCharArray();
            // capitalize the first letter
            chars[0] -= 'a' - 'A';
            writer.println("        public " + types.get(i) + " get" + new String(chars) + "() {");
            writer.println("            return this." + vars.get(i) + ";");
            writer.println("        }");
            writer.println();
        }

        writer.println("    }");
        writer.println();
    }

    private static void defineVisitor(String dir, String base, List<String> classes) {
        String full_path = dir + "/" + base + "Visitor.java";
        try (PrintWriter writer = new PrintWriter(full_path, StandardCharsets.UTF_8)) {
            writer.println("package icu.buzz.lox;");
            writer.println();
            writer.println("public interface " + base + "Visitor<R> {");
            for (String method : classes) writer.println("    R visit" + base + " (" + base + "." + method + " expr);");
            writer.println("}");
        } catch (IOException e) {
            System.out.println("writer opens: " + full_path + " fail");
        }
    }
}
