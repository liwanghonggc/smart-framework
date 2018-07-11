package org.smart4j.framework.bean;

import org.smart4j.framework.util.CastUtil;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求参数对象
 */
public class Param {

//    private Map<String, Object> paramMap;
//
//    public Param(Map<String, Object> paramMap){
//        this.paramMap = paramMap;
//    }
//
//    /**
//     * 根据参数名获取Long型参数值
//     */
//    public long getLong(String name){
//        return CastUtil.castLong(paramMap.get(name));
//    }
//
//    /**
//     * 获取所有字段信息
//     */
//    public Map<String, Object> getParamMap() {
//        return paramMap;
//    }
//
//    /**
//     * 判断参数是否为空,若为空,调用action方法时就可以不传该参数
//     */
//    public boolean isEmpty(){
//        return CollectionUtil.isEmpty(paramMap);
//    }

      private List<FormParam> formParamList;

      private List<FileParam> fileParamList;

      public Param(List<FormParam> formParamList){
          this.formParamList = formParamList;
      }

      public Param(List<FormParam> formParamList, List<FileParam> fileParamList) {
          this.formParamList = formParamList;
          this.fileParamList = fileParamList;
      }

    /**
     * 获取请求参数映射
     */
    public Map<String, Object> getParamMap(){
        Map<String, Object> paramMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(formParamList)){
            for(FormParam formParam : formParamList){
                String fieldName = formParam.getFieldName();
                Object fieldValue = formParam.getFieldValue();
                if(paramMap.containsKey(fieldName)){
                    fieldValue = paramMap.get(fieldName) + StringUtil.SEPARATOR + fieldValue;
                }
                paramMap.put(fieldName, fieldValue);
            }
        }
        return paramMap;
    }

    /**
     * 获取上传文件映射
     */
    public Map<String, List<FileParam>> getFileMap(){
        Map<String, List<FileParam>> fileMap = new HashMap<>();
        if(CollectionUtil.isNotEmpty(fileParamList)){
            for(FileParam fileParam : fileParamList){
                String fieldName = fileParam.getFieldName();
                List<FileParam> fileParamList;
                if(fileMap.containsKey(fieldName)){
                    fileParamList = fileMap.get(fieldName);
                }else {
                    fileParamList = new ArrayList<>();
                }
                fileParamList.add(fileParam);
                fileMap.put(fieldName, fileParamList);
            }
        }
        return fileMap;
    }

    /**
     * 获取所有上传文件
     */
    public List<FileParam> getFileList(String fieldName) {
        return getFileMap().get(fieldName);
    }

    /**
     * 获取唯一上传文件
     */
    public FileParam getFile(String fieldName){
        List<FileParam> fileParamList = getFileList(fieldName);
        if(CollectionUtil.isNotEmpty(fileParamList) && fileParamList.size() == 1){
            return fileParamList.get(0);
        }
        return null;
    }

    /**
     * 验证参数是否为空
     */
    public boolean isEmpty(){
        return CollectionUtil.isEmpty(formParamList) && CollectionUtil.isEmpty(fileParamList);
    }

    /**
     * 根据参数名获取String型参数值
     */
    public String getString(String name){
        return CastUtil.castString(getParamMap().get(name));
    }

    /**
     * 根据参数名获取long型参数值
     */
    public long getLong(String name){
        return CastUtil.castLong(getParamMap().get(name));
    }

    /**
     * 根据参数名获取int型参数值
     */
    public int getInt(String name){
        return CastUtil.castInt(getParamMap().get(name));
    }

    /**
     * 根据参数名获取double型参数值
     */
    public double getDouble(String name){
        return CastUtil.castDouble(getParamMap().get(name));
    }

    /**
     * 根据参数名获取String型参数值
     */
    public boolean getBoolean(String name){
        return CastUtil.castBoolean(getParamMap().get(name));
    }
}
