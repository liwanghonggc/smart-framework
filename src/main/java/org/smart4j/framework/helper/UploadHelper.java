package org.smart4j.framework.helper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.bean.FileParam;
import org.smart4j.framework.bean.FormParam;
import org.smart4j.framework.bean.Param;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.FileUtil;
import org.smart4j.framework.util.StreamUtil;
import org.smart4j.framework.util.StringUtil;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文件上传助手类
 */
public final class UploadHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadHelper.class);

    /**
     * Apache Commons FileUpload提供的Servlet文件上传对象
     */
    private static ServletFileUpload servletFileUpload;

    /**
     * 初始化ServletFileUpload对象,设置一个临时上传文件的临时目录与上传文件最大限制
     * 需要在DispatcherServlet的init方法中调用该init方法进行初始化
     */
    public static void init(ServletContext servletContext){
        File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");

        //设置目录
        servletFileUpload = new ServletFileUpload(new DiskFileItemFactory(
                DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD, repository
        ));

        int uploadLimit = ConfigHelper.getAppUploadLimit();

        //设置上传文件大小
        if(uploadLimit != 0){
            servletFileUpload.setFileSizeMax(uploadLimit * 1024 * 1024);
        }
    }

    /**
     * 判断请求是否为multipart类型
     */
    public static boolean isMultipart(HttpServletRequest request){
        return ServletFileUpload.isMultipartContent(request);
    }

    /**
     * 创建请求对象
     */
    public static Param createParam(HttpServletRequest request) throws IOException{
        List<FormParam> formParamList = new ArrayList<>();
        List<FileParam> fileParamList = new ArrayList<>();

        try {
            //使用servltFileUpload对象来解析请求参数
            Map<String, List<FileItem>> fileItemListMap = servletFileUpload.parseParameterMap(request);
            //通过遍历所有请求参数来初始化formParamListhefileParamList
            if(CollectionUtil.isNotEmpty(fileItemListMap)){
                for(Map.Entry<String, List<FileItem>> fileItemListEntry : fileItemListMap.entrySet()){
                    String fieldName = fileItemListEntry.getKey();
                    List<FileItem> fileItemList = fileItemListEntry.getValue();
                    if(CollectionUtil.isNotEmpty(fileItemList)){
                        for(FileItem fileItem : fileItemList){
                            //遍历请求参数时,需要对FileItem对象进行判断,若为普通表单字段,则创建FormParam对象
                            if(fileItem.isFormField()){
                                String fieldValue = fileItem.getString("UTF-8");
                                formParamList.add(new FormParam(fieldName, fieldValue));
                            }else{
                                //否则为文件上传字段,通过FileUtil提供的getRealFileName来获取上传文件的真实文件名
                                //并从FileItem对象中构造FileParam对象,添加到fileParamList中
                                String fileName = FileUtil.getRealFileName(new String(fileItem.getName().getBytes(), "UTF-8"));
                                if(StringUtil.isNotEmpty(fileName)){
                                    long fileSize = fileItem.getSize();
                                    String contentType = fileItem.getContentType();
                                    InputStream inputStream = fileItem.getInputStream();
                                    fileParamList.add(new FileParam(fieldName, fileName, fileSize, contentType, inputStream));
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileUploadException e) {
            LOGGER.error("create param failure", e);
            throw new RuntimeException(e);
        }

        //最后通过formParamList和fileParamList来构造Param对象并返回
        return new Param(formParamList, fileParamList);
    }

    /**
     * 上传文件
     */
    public static void uploadFile(String basePath, FileParam fileParam){
        try {
            if(fileParam != null){
                String filePath = basePath + fileParam.getFileName();
                FileUtil.createFile(filePath);
                InputStream inputStream = new BufferedInputStream(fileParam.getInputStream());
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
                StreamUtil.copyStream(inputStream, outputStream);
            }
        } catch (Exception e) {
            LOGGER.error("upload file failure", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 批量上传文件
     */
    public static void uploadFile(String basePath, List<FileParam> fileParamList){
        try {
            if(CollectionUtil.isNotEmpty(fileParamList)){
                for(FileParam fileParam : fileParamList){
                    uploadFile(basePath, fileParam);
                }
            }
        } catch (Exception e) {
            LOGGER.error("upload file failure", e);
            throw new RuntimeException(e);
        }
    }


}
