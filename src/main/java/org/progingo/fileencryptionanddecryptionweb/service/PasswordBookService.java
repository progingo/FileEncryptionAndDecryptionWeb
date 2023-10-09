package org.progingo.fileencryptionanddecryptionweb.service;

import com.alibaba.fastjson2.JSON;
import org.progingo.fileencryptionanddecryptionweb.domain.PasswordData;
import org.progingo.fileencryptionanddecryptionweb.util.Respond;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Service
public class PasswordBookService {

    private String name,password;
    private String key;
    private String workPath;//工作目录


    public Respond add(String name,String password,String workPath){
        this.name = getMD5(name);
        this.password = getMD5(password);
        this.workPath = workPath;
        return createPasswordBook();
    }

    private Respond createPasswordBook(){
        String headFile = workPath + "\\" + this.name + ".pk";

        File file = new File(workPath);
        if (!file.exists() || !file.isDirectory()){//判断目录是否存在
            //不存在或者为一个文件
            file.mkdirs();
        }
        //存在或者已经创建好
        File file1 = new File(headFile);
        if (file1.exists() && file1.isFile()){
            //在该工作目录下有该用户的pk文件
            return Respond.fail("该目录下已经有跟您用户名一样的用户在此创建了密码本，如果是您创建的可以直接在上一步直接登录，如果不是您的建议您更改路径存放密码本或者更改用户名");
        }
        //如果没问题那么path就将是接下来的工作目录
        try {
            System.out.println("正在创建专属您的密码本...");
            file1.createNewFile();
            FileOutputStream fos = new FileOutputStream(file1,true);
            fos.write(getMD5(this.name + this.password).getBytes());
            String pkey = getUUID();//真正的key是随机生成的(pkey只是为了防止与类中的key同名混淆)
            this.key = pkey;
            //将key与this.password做AES加密并追加.pk
            pkey = AESEncrypt(pkey,this.password);
            fos.write(pkey.getBytes());
            fos.close();
        } catch (Exception e){
            System.out.println(e);
            return Respond.fail("创建密码本失败");
        }
        System.out.println("创建成功");
        System.out.println("正在制作初始节点，请稍后...");

        PasswordData data = new PasswordData();
        data.setState(-1);//初始节点专属state
        data.setFrom("by：progingo~  V1.0");
        data.setZh("欢迎使用密码本");
        //对初始化节点进行加密
        String[] str = jiami(data);
        if (str == null){
            System.out.println("创建初始化节点失败");
            return Respond.fail("创建初始化节点失败");
        }

        //将初始节点的文件名和uid存放到.pk文件中
        try {
            FileOutputStream fos = new FileOutputStream(file1,true);
            fos.write(str[0].getBytes());
            fos.write(str[1].getBytes());
            fos.close();
        } catch (Exception e) {
            System.out.println("记录初始节点失败");
            return Respond.fail("记录初始节点失败");
        }
        System.out.println("创建初始化节点成功!");

        return Respond.ok();
    }


    //----------------------------------------------------------------------------------------------------
    /*
    新加密模块，给对象，返回加密后文件名和uid字符串数组
     */
    private String[] jiami(PasswordData passwordData){
        String nextFileName = getUUID();//节点的文件名
        String nextUUID = getUUID();//解密节点的uuid
        //创建.propd文件
        String filePath = this.workPath + "\\" + nextFileName + ".propd";
        File file = new File(filePath);
        while (file.exists()){//保证没有重名文件
            nextFileName = getUUID();
            filePath = this.workPath + "\\" + nextFileName + ".propd";
            file = new File(filePath);
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("创建密文失败");
            return null;
        }
        //对象转json
        String dataJson = JSON.toJSONString(passwordData);
        //对字符串进行加密
        byte[] aesEncryptNoBase;
        try {
            aesEncryptNoBase = AESEncryptNoBase(dataJson, getMD5(this.key + nextUUID));
        } catch (Exception e) {
            System.out.println("文件加密出错");
            return null;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(aesEncryptNoBase);
            outputStream.close();
        } catch (Exception e) {
            System.out.println("写入密文失败");
            return null;
        }
        return new String[]{nextFileName,nextUUID};
    }

    //----------MD5------------------------------------------------------------------------------------------
    private String getMD5(String password){
        String hex = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            //转换成十六进制字符串
            hex = bytesToHex(digest);
        }catch (Exception e){
            System.out.println(e);
        }

        return hex;
    }

    private String bytesToHex(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes){
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                builder.append('0');
            builder.append(hex);
        }
        return builder.toString();
    }

    //----------AES------------------------------------------------------------------------------------------
    private String AESEncrypt(String text,String key) throws Exception {
        // 创建AES加密算法实例(根据传入指定的秘钥进行加密)
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

        // 初始化为加密模式，并将密钥注入到算法中
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // 将传入的文本加密
        byte[] encrypted = cipher.doFinal(text.getBytes());
        //生成密文
        // 将密文进行Base64编码，方便传输
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES算法解密
     * @Param:base64Encrypted密文
     * @Param:key密钥
     * */
    private String AESDecrypt(String base64Encrypted,String key)throws Exception{
        // 创建AES解密算法实例
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

        // 初始化为解密模式，并将密钥注入到算法中
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // 将Base64编码的密文解码
        byte[] encrypted = Base64.getDecoder().decode(base64Encrypted);

        // 解密
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }


    private byte[] AESEncryptNoBase(String text,String key) throws Exception {
        // 创建AES加密算法实例(根据传入指定的秘钥进行加密)
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

        // 初始化为加密模式，并将密钥注入到算法中
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // 将传入的文本加密
        return cipher.doFinal(text.getBytes());
    }

    /**
     * AES算法解密
     * @Param:base64Encrypted密文
     * @Param:key密钥
     * */
    private String AESDecryptNoBase(byte[] Encrypted,String key)throws Exception{
        // 创建AES解密算法实例
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");

        // 初始化为解密模式，并将密钥注入到算法中
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // 解密
        byte[] decrypted = cipher.doFinal(Encrypted);
        return new String(decrypted);
    }

    //----------UUIID------------------------------------------------------------------------------------------
    private String getUUID(){
        String key = UUID.randomUUID().toString();//真正的密码是随机生成的
        String[] split = key.split("-");
        StringBuilder builder = new StringBuilder();
        for (String s : split)
            builder.append(s);
        key = builder.toString();
        return key;
    }

}
