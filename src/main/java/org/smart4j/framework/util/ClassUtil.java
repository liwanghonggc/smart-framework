package org.smart4j.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类操作工具类
 */
public final class ClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);

    /**
     * 获取类加载器
     */
    public static ClassLoader getClassLoader(){
        //获取当前线程中的ClassLoader
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载类,isInitialized是否执行类的静态代码块
     */
    public static Class<?> loadClass(String className, boolean isInitialized){
        Class<?> cls;
        try {
            cls = Class.forName(className, isInitialized, getClassLoader());
        } catch (ClassNotFoundException e) {
            LOGGER.error("load class failure", e);
            throw new RuntimeException(e);
        }
        return cls;
    }

    /**
     * 加载类,isInitialized为false,可以提高类的加载性能,
     * 此处建议为true,否则后面使用时可能有些功能未正常加载
     */
    public static Class<?> loadClass(String className){
        return loadClass(className, true);
    }

    /**
     * 获取指定包名下的所有类,根据包名并将其转换为文件路径,读取class文件或者jar包,获取指定的类名取加载
     */
    public static Set<Class<?>> getClassSet(String packageName){
        Set<Class<?>> classSet = new HashSet<>();
        try {
            //packageName org.smart4j.framework.bean
            String newPackageName = packageName.replace(".", "/");
            //newPackageName org/smart4j/framework/bean
            Enumeration<URL> urls = getClassLoader().getResources(newPackageName);
            while(urls.hasMoreElements()){
                //url file:/D:/Software/IDEA/Projects/smartframework/target/classes/org/smart4j/framework/bean
                URL url = urls.nextElement();
                if(url != null){
                    //protocol file
                    String protocol = url.getProtocol();
                    if(protocol.endsWith("file")){
                        //packagePath /D:/Software/IDEA/Projects/smartframework/target/classes/org/smart4j/framework/bean
                        String packagePath = url.getPath().replaceAll("%20","");
                        addClass(classSet, packagePath, packageName);
                    }else if(protocol.endsWith("jar")){
                        JarURLConnection jarURLConnection = (JarURLConnection)url.openConnection();
                        if(jarURLConnection != null){
                            JarFile jarFile = jarURLConnection.getJarFile();
                            if(jarFile != null){
                                Enumeration<JarEntry> jarEntries = jarFile.entries();
                                while(jarEntries.hasMoreElements()){
                                    JarEntry jarEntry = jarEntries.nextElement();
                                    String jarEntryName = jarEntry.getName();
                                    if(jarEntryName.endsWith(".class")){
                                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                        doAddClass(classSet, className);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("get class set failure", e);
            throw new RuntimeException(e);
        }
        return classSet;
    }

    private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName){
        //只接受.class文件或者目录
        File[] files = new File(packagePath).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
            }
        });

        for (File file : files){
            //fileName 如Data.class
            String fileName = file.getName();
            if(file.isFile()){
                //className Data
                String className = fileName.substring(0, fileName.lastIndexOf("."));
                if(StringUtil.isNotEmpty(packageName)){
                    //className org.smart4j.framework.bean.Data
                    className = packageName + "." + className;
                }
                doAddClass(classSet, className);
            }else {
                String subPackagePath = fileName;
                if(StringUtil.isNotEmpty(packagePath)){
                    subPackagePath = packagePath + "/" + subPackagePath;
                }
                String subPackageName = fileName;
                if(StringUtil.isNotEmpty(packageName)){
                    subPackageName = packageName + "." + subPackageName;
                }
                addClass(classSet, subPackagePath, subPackageName);
            }
        }
    }

    private static void doAddClass(Set<Class<?>> classSet, String className){
        Class<?> cls = loadClass(className, false);
        classSet.add(cls);
    }
}
