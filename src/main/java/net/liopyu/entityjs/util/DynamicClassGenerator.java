package net.liopyu.entityjs.util;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.io.*;
import java.util.jar.*;

public class DynamicClassGenerator {

    public static void main(String[] args) throws IOException {
        String className = "DynamicClass";
        String superClassName = "java/lang/Object";
        byte[] bytecode = generateBytecode(className, superClassName);

        String outputFolder = "C:\\Users\\subli\\curseforge\\minecraft\\Instances\\";
        String outputFilePath = outputFolder + "DynamicClass.class";

        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(bytecode);
        }

        // Create a JAR file
        String jarFilePath = outputFolder + "output.jar";
        createJar(jarFilePath, outputFilePath);

        System.out.println("JAR file created successfully: " + jarFilePath);
    }

    private static void generateDefaultConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private static void generateGetIntValueMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "getIntValue", "()I", null, null);
        mv.visitCode();
        mv.visitIntInsn(Opcodes.BIPUSH, 5); // Push constant 5 onto the stack
        mv.visitInsn(Opcodes.IRETURN); // Return integer value from the stack
        mv.visitMaxs(1, 0);
        mv.visitEnd();
    }

    private static byte[] generateBytecode(String className, String superClassName) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, superClassName, null);


        cw.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "serialVersionUID", "J", null, 0).visitEnd();


        cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "intValue", "I", null, null).visitEnd();
        cw.visitField(Opcodes.ACC_PUBLIC, "doubleValue", "D", null, null).visitEnd();


        generateDefaultConstructor(cw);


        generateGetIntValueMethod(cw);

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void createJar(String jarFilePath, String classFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(jarFilePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             JarOutputStream jos = new JarOutputStream(bos)) {

            File classFile = new File(classFilePath);
            String entryName = classFile.getName();

            // Add the class file to the JAR
            JarEntry entry = new JarEntry(entryName);
            jos.putNextEntry(entry);

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(classFile))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    jos.write(buffer, 0, bytesRead);
                }
            }
            jos.closeEntry();
        }
    }
}