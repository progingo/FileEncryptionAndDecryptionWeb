package org.progingo.fileencryptionanddecryptionweb.service;

import com.alibaba.fastjson2.JSON;
import org.progingo.fileencryptionanddecryptionweb.domain.PasswordData;
import org.progingo.fileencryptionanddecryptionweb.domain.PasswordDataSafe;
import org.progingo.fileencryptionanddecryptionweb.util.Respond;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

@Service
public class PasswordDataService {
    private String name,password;
    private String key;
    private String workPath;//工作目录
    private PasswordData firstNode;//头结点(初始化节点)
    private String lastNodeName;//最后一个节点的节点名
    private String lastNodeUID;//最后一个节点的UID
    private String firstNodeName;
    private String firstNodeUID;
    private List<PasswordDataSafe> passwordDataList;

    public Respond init(){
        Respond b = findPassBook(this.workPath);
        if (b.getState() != 200){
            return b;
        }

        System.out.println("解析中...");
        //抽取pk文件的内容
        char[] allPk = new char[1024];
        int len;
        String pkData;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.workPath + "\\" + this.name + ".pk"));
            len = reader.read(allPk);
            allPk = Arrays.copyOf(allPk,len);
            pkData = new String(allPk);
        } catch (Exception e) {
            return Respond.fail("pk打开失败");
        }
        String[] pkDataRes = analysisPKData(pkData);
        if (pkDataRes == null){
            return Respond.fail("解析不到信息");
        }
        try {
            this.key = AESDecrypt(pkDataRes[0], this.password);
        } catch (Exception e) {
            return Respond.fail("解析key失败");
        }
        System.out.println("解析完成");

        this.passwordDataList = new ArrayList<>();
        System.out.println("初始化节点...");
        boolean initSucc = initFirstNode(pkDataRes[1], pkDataRes[2]);
        if (!initSucc){
            return Respond.fail("初始化节点失败");
        }
        //这两个变量在添加功能时使用
        this.lastNodeName = pkDataRes[1];
        this.lastNodeUID = pkDataRes[2];
        return Respond.ok();
    }
    public Respond init(String name,String password,String workPath){
        this.name = getMD5(name);
        this.password = getMD5(password);
        this.workPath = workPath;

        Respond b = findPassBook(this.workPath);
        if (b.getState() != 200){
            return b;
        }

        System.out.println("解析中...");
        //抽取pk文件的内容
        char[] allPk = new char[1024];
        int len;
        String pkData;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.workPath + "\\" + this.name + ".pk"));
            len = reader.read(allPk);
            allPk = Arrays.copyOf(allPk,len);
            pkData = new String(allPk);
        } catch (Exception e) {
            return Respond.fail("pk打开失败");
        }
        String[] pkDataRes = analysisPKData(pkData);
        if (pkDataRes == null){
            return Respond.fail("解析不到信息");
        }
        try {
            this.key = AESDecrypt(pkDataRes[0], this.password);
        } catch (Exception e) {
            return Respond.fail("解析key失败");
        }
        System.out.println("解析完成");

        this.passwordDataList = new ArrayList<>();
        System.out.println("初始化节点...");
        boolean initSucc = initFirstNode(pkDataRes[1], pkDataRes[2]);
        if (!initSucc){
            return Respond.fail("初始化节点失败");
        }
        //这两个变量在添加功能时使用
        this.lastNodeName = pkDataRes[1];
        this.lastNodeUID = pkDataRes[2];
        return Respond.ok();
    }

    /*
    此时已经完成头结点的初始化，工作列表中有且仅有头结点，通过头结点开始初始化整个列表，将所有密码对象都导入
     */
    public Respond initPasswordList(){
        if (this.passwordDataList == null || this.passwordDataList.get(0).getState() != -1){
            return Respond.fail("初始化列表发现为非法列表，请先(重新)执行 init()");
        }

        PasswordData fistNode = this.firstNode;//防止空指针才赋值的
        String nextName = fistNode.getNextName();
        String nextUUID = fistNode.getNextUUID();

        PasswordData node;
        while (nextName != null && nextUUID != null){
            node = jiemi(nextName, nextUUID);
            if (node == null){
                break;
            }
            //这两个变量在添加功能时使用
            this.lastNodeName = nextName;
            this.lastNodeUID = nextUUID;

            PasswordDataSafe passwordDataSafe = new PasswordDataSafe();
            passwordDataSafe.setState(node.getState());
            passwordDataSafe.setZh(node.getZh());
            passwordDataSafe.setFrom(node.getFrom());
            passwordDataSafe.setBz(node.getBz());
            this.passwordDataList.add(passwordDataSafe);
            nextName = node.getNextName();
            nextUUID = node.getNextUUID();
        }
        node = null;
        return Respond.ok();
    }

    //查找密码本
    private Respond findPassBook(String path){
        File headFile;
        if (path == null || path.equals("")){
            path = "book";
        }
        String headFilePath = path + "\\" + this.name + ".pk";

        headFile = new File(headFilePath);
        if (!headFile.exists()) {
            return Respond.fail("找不到你的密码本，请确认路径或用户名");
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(headFile));
            char[] zhmmMD5 = new char[32];
            reader.read(zhmmMD5);
            if(!getMD5(this.name + this.password).equals(new String(zhmmMD5))){
                return Respond.fail("验证失败");
            }
        } catch (Exception e) {

            return Respond.fail("验证用户时出错");
        }

        this.workPath = path;
        return Respond.ok();
    }

    /*
    拿到头结点的信息解析头结点
    如果解析成功就放入工作列表中并返回true
     */
    private boolean initFirstNode(String firstNodeName,String firstNodeUID){
        String filePath = this.workPath + "\\" + firstNodeName + ".propd";
        File file = new File(filePath);
        if (!file.exists()){
            System.out.println("找不到初始节点");
            return false;
        }

        this.firstNode = jiemi(firstNodeName, firstNodeUID);
        if (this.firstNode == null)
            return false;

        if (this.firstNode.getState() != -1){
            System.out.println("头结点有误可能被篡改");
            this.firstNode = null;
            return false;
        }

        this.firstNodeName = firstNodeName;
        this.firstNodeUID = firstNodeUID;

        PasswordDataSafe passwordDataSafe = new PasswordDataSafe();
        passwordDataSafe.setState(this.firstNode.getState());
        passwordDataSafe.setZh(this.firstNode.getZh());
        passwordDataSafe.setFrom(this.firstNode.getFrom());
        passwordDataSafe.setBz(this.firstNode.getBz());
        this.passwordDataList.add(passwordDataSafe);

        return true;
    }

    //查询区----------------------------------------------------------------------------------------------------------------------------------
    public Respond showList(){
        return Respond.ok(this.passwordDataList);
    }

    public Respond showPassword(int index){
        if (1 <= index && index < this.passwordDataList.size()){
            PasswordData passwordData = findByNumber(index);
            if (passwordData == null){
                return Respond.fail("找不到对应条目");
            }
            return Respond.ok(passwordData.getPassword());
        }
        return Respond.fail("索引有误");
    }

    private PasswordData findByNumber(int index){
        if (index < 1 || index >= this.passwordDataList.size()){
            System.out.println("索引有误");
            return null;
        }
        PasswordData pd = this.firstNode;
        String nextName = this.firstNode.getNextName();
        String nextUUID = this.firstNode.getNextUUID();
        for (int i = 1;i <= index;++i){
            pd = jiemi(nextName,nextUUID);
            nextName = pd.getNextName();
            nextUUID = pd.getNextUUID();
        }
        if (pd.getState() == -1 || pd.getZh() != null && !this.passwordDataList.get(index).getZh().equals(pd.getZh())){
            System.out.println("(检测到获取的节点可能有误,如果有误请与作者联系)");
        }
        return pd;
    }

    public Respond deletePassword(int index){
        //先校验索引有无问题
        if (index < 1 || index >= this.passwordDataList.size()){
            return Respond.fail("输入的索引有误");
        }
        //删除分两种情况，分别是删除尾节点和非尾节点
        if (index == this.passwordDataList.size() - 1){
            //删除尾节点的情况
            //找到上一个节点的名称和解密加密上一个节点的uid
            String preNodeName = this.firstNodeName;
            String preNodeUID = this.firstNodeUID;

            for (int i = 1;i < index;++i){
                PasswordData data = jiemi(preNodeName, preNodeUID);
                preNodeName = data.getNextName();
                preNodeUID = data.getNextUUID();
            }
            //对要删除的上一个节点进行信息更改
            PasswordData preNode = jiemi(preNodeName, preNodeUID);
            String deleteFileName = preNode.getNextName();//拿到要删除的文件的名字
            preNode.setNextName(null);
            preNode.setNextUUID(null);

            return rejiami(preNode,preNodeName,preNodeUID,deleteFileName);
        } else {
            //删除非尾节点的情况，区别与删除尾节点的情况就是将前一个节点的nextName等不再设为null
            //找到上一个节点的名称和解密加密上一个节点的uid
            String preNodeName = this.firstNodeName;
            String preNodeUID = this.firstNodeUID;

            for (int i = 1;i < index;++i){
                PasswordData data = jiemi(preNodeName, preNodeUID);
                preNodeName = data.getNextName();
                preNodeUID = data.getNextUUID();
            }
            //对要删除的上一个节点进行信息更改
            PasswordData preNode = jiemi(preNodeName, preNodeUID);//拿到要删除的节点的上一个节点
            String deleteFileName = preNode.getNextName();//拿到要删除的文件的名字
            PasswordData deleteNode = jiemi(preNode.getNextName(), preNode.getNextUUID());//要删除的节点

            //将要删除的节点的信息赋给要删除的节点的上一个节点
            preNode.setNextName(deleteNode.getNextName());
            preNode.setNextUUID(deleteNode.getNextUUID());

            return rejiami(preNode,preNodeName,preNodeUID,deleteFileName);
        }
    }

    //该方法用于删除节点时的操作
    private Respond rejiami(PasswordData preNode, String preNodeName, String preNodeUID, String deleteFileName){
        //备份上一个节点的源文件和要删除的节点，然后删除两个节点的源文件，开始重新建立更新后的节点的文件，如果成功就删除备份文件，如果失败就重备份文件中找回
        //备份
        boolean b1 = creatBackFile(preNodeName);
        boolean b2 = creatBackFile(deleteFileName);
        if (!b1 || !b2){
            return Respond.fail("创建备份文件失败");
        }
        //删除两个源文件
        //删除要删除的源文件
        String deleteFilePath = this.workPath + "\\" + deleteFileName + ".propd";
        File deleteFile = new File(deleteFilePath);
        if (deleteFile.exists()){
            boolean b3 = deleteFile.delete();
            if (!b3){
                System.out.println("无法删除源文件，有源文件残留");
            }
        }
        String preNodeFilePath = this.workPath + "\\" + preNodeName + ".propd";
        File preNodeFile = new File(preNodeFilePath);
        if (preNodeFile.exists()){
            boolean b3 = preNodeFile.delete();
            if (!b3){
                return Respond.fail("无法删除要更改的文件");
            }
        }
        //开始重新建立
        boolean b = jiami(preNode, preNodeName, preNodeUID);
        if (!b){
            //恢复删除节点的上一个节点的文件
            System.out.println("写入失败，正在恢复...");
            //重加密失败，通过备份文件恢复
            boolean b4 = useBackFile(preNodeName, true);
            if (!b4){
                return Respond.fail("恢复失败");
            } else {
                System.out.println("恢复成功");
            }
            //恢复要删除的节点
            System.out.println("正在恢复要删除的节点");
            boolean b5 = useBackFile(deleteFileName, true);
            if (!b5){
                return Respond.fail("恢复备份失败，请与作者联系");
            } else {
                System.out.println("恢复成功");
            }
            return Respond.fail("失败，但是已经恢复");
        } else {
            String preFileBkPaht = this.workPath + "\\" + preNodeName + ".propdbk";
            File preFileBkFile = new File(preFileBkPaht);
            if (preFileBkFile.exists()){
                boolean b4 = preFileBkFile.delete();
                if (!b4){
                    System.out.println("删除备份文件失败,产生残留");
                }
            }

            String deleteFileBkPaht = this.workPath + "\\" + deleteFileName + ".propdbk";
            File deleteFileBkFile = new File(deleteFileBkPaht);
            if (deleteFileBkFile.exists()){
                boolean b4 = deleteFileBkFile.delete();
                if (!b4){
                    System.out.println("删除备份文件失败,产生残留");
                }
            }
        }
        return Respond.ok();
    }

    private boolean creatBackFile(String fileName){
        String preFilePath = this.workPath + "\\" + fileName + ".propd";
        String prebkFilePath = this.workPath + "\\" + fileName + ".propdbk";
        File preFile = new File(preFilePath);
        File prebkFile = new File(prebkFilePath);
        if (!preFile.exists()){
            System.out.println("创建失败，找不到要操作的文件");
            return false;
        }

        if (prebkFile.exists()){
            //备份文件已经存在，直接删除
            boolean delete = prebkFile.delete();
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(preFile);
            os = new FileOutputStream(prebkFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e){
            System.out.println("备份失败");
            return false;
        }finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                System.out.println("关闭文件失败");
            }
        }
        if (preFile.exists()){
            //删除被备份的文件
            boolean delete = preFile.delete();
            if (!delete){
                System.out.println("删除源文件失败");
            }
        }
        return true;
    }

    private boolean deletBackFile(String fileName){
        String prebkFilePath = this.workPath + "\\" + fileName + ".propdbk";
        File prebkFile = new File(prebkFilePath);
        if (!prebkFile.exists()){
            System.out.println("找不到.propdbk文件");
            return false;
        }
        prebkFile.delete();
        return true;
    }

    //用于将备份文件恢复为源文件，参数delBk如果选择true则是在完成恢复后删除备份文件，否则不删除
    private boolean useBackFile(String fileName,boolean delBk){
        String preFilePath = this.workPath + "\\" + fileName + ".propd";
        String prebkFilePath = this.workPath + "\\" + fileName + ".propdbk";
        File preFile = new File(preFilePath);
        File prebkFile = new File(prebkFilePath);

        if (!prebkFile.exists()){
            System.out.println("找不到.propdbk文件");
            return false;
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(prebkFile);
            os = new FileOutputStream(preFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e){
            System.out.println(e);
            System.out.println("重大错误！非常抱歉，未知bug导致恢复失败，请与作者联系！");
            return false;
        }finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                System.out.println("关闭失败");
            }
        }

        //删除备份文件
        if (delBk){
            if (prebkFile.exists()){
                prebkFile.delete();
            } else {
                System.out.println("要删除的备份文件不存在");
            }
        }
        return true;
    }

    public Respond addPassword(PasswordData passwordData){
        if (passwordData.getPassword() == null){
            return Respond.fail("密码不能为空");
        }

        String[] dataInfo = jiami(passwordData);//获取到这个节点的name和uid
        if (dataInfo == null){
            return Respond.fail("新的信息找不到");
        }
        //将这个新节点的name和uid写入到上一个节点中
        boolean b = rejiami(dataInfo,this.lastNodeName,this.lastNodeUID);
        if (!b){
            return Respond.fail("添加信息失败");
        }
        //更新信息
        this.lastNodeName = dataInfo[0];
        this.lastNodeUID = dataInfo[1];

        return Respond.ok();
    }

    /*
    对指定节点进行重新加密，用于插入节点时，对新节点的上一个节点进行重加密
    新节点的上一级节点其实就是最后一个节点，该节点的name和uid已经记录到类变量中了
    参数为新节点的name和uid信息
     */
    private boolean rejiami(String[] newInfo,String fileName, String fileUID){
        String preFilePath = this.workPath + "\\" + fileName + ".propd";
        File preFile = new File(preFilePath);
        String prebkFilePath = this.workPath + "\\" + fileName + ".propdbk";
        File prebkFile = new File(prebkFilePath);

        if (!preFile.exists()){
            System.out.println("添加失败，找不到前继数据");
            return false;
        }

        //添加新信息
        PasswordData preData = jiemi(fileName, fileUID);
        if (preData == null){
            return false;
        }
        //将新节点的上一个节点文件，也就是要改动的节点文件先备份，防止出错
        boolean b1 = creatBackFile(fileName);
        if (!b1){
            System.out.println("备份失败");
            return false;
        }

        preData.setNextName(newInfo[0]);
        preData.setNextUUID(newInfo[1]);

        boolean b = jiami(preData, fileName, fileUID);
        if (!b){
            //加密失败，删除坏的新的上个节点文件(如果有),并恢复
            useBackFile(fileName,false);
        } else {
            //重加密成功,直接删除备份文件
            if (prebkFile.exists()){
                prebkFile.delete();
            }
        }
        return true;
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

    private boolean jiami(PasswordData passwordData,String fileName,String fileUID){

        //创建.propd文件
        String filePath = this.workPath + "\\" + fileName + ".propd";
        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.out.println("重加密时创建密文失败");
            return false;
        }
        //对象转json
        String dataJson = JSON.toJSONString(passwordData);
        //对字符串进行加密
        byte[] aesEncryptNoBase;
        try {
            aesEncryptNoBase = AESEncryptNoBase(dataJson, getMD5(this.key + fileUID));
        } catch (Exception e) {
            System.out.println("重加密时文件加密出错");
            return false;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(aesEncryptNoBase);
            outputStream.close();
        } catch (Exception e) {
            System.out.println("重加密时写入密文失败");
            return false;
        }
        return true;
    }


    //加密模块--------扫描 XXX.procash 加密后存为 XXX.propd 并删除缓存文件(XXX.procash)
    @Deprecated
    private boolean jiami(String path,String cashFileName,String UID){
        File cashFile = new File(path + "\\" + cashFileName + ".procash");
        if (!cashFile.exists()){
            System.out.println("加密区：找不到指定缓存文件");
            return false;
        }
        String cashString;
        try {
            InputStreamReader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(cashFile)));
            char[] chars = new char[1024];
            StringBuilder builder = new StringBuilder();
            int len;
            while ((len = reader.read(chars)) != -1){
                builder.append(String.valueOf(chars,0,len));
            }
            reader.close();
            cashString = builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //对字符串进行加密
        byte[] aesEncryptNoBase;
        try {
            aesEncryptNoBase = AESEncryptNoBase(cashString, getMD5(this.key + UID));

        } catch (Exception e) {
            System.out.println("加密区:文件加密出错");
            throw new RuntimeException(e);
        }
        //创建存放密文的文件并将密文存进去
        File passDastaFile = new File(path + "\\" + cashFileName + ".propd");
        try {
            passDastaFile.createNewFile();
        } catch (IOException e) {
            System.out.println("创建密文失败");
            throw new RuntimeException(e);
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(passDastaFile);
            outputStream.write(aesEncryptNoBase);
            outputStream.close();
        } catch (Exception e) {
            System.out.println("写入密文失败");
            throw new RuntimeException(e);
        }

        boolean delete = cashFile.delete();
        if (delete){
            System.out.println("删除缓存成功");
        }
        return true;
    }

    //解密模块--------获取 XXX.propd 解密文件 并提交到工作列表中
    private PasswordData jiemi(String fileName,String UID){
        String filePath = this.workPath + "\\" + fileName + ".propd";
        File pdFile = new File(filePath);
        if (!pdFile.exists()){
            System.out.println("找不到.propd文件");
            return null;
        }
        byte[] pdData = new byte[8 * 1024];
        int len;
        String pdString;
        try {
            FileInputStream fis = new FileInputStream(pdFile);
            len = fis.read(pdData);
            fis.close();
            pdData = Arrays.copyOf(pdData,len);
            String md5 = getMD5(this.key + UID);
            pdString = AESDecryptNoBase(pdData, md5);
        } catch (Exception e) {
            System.out.println("解密时出错");
            return null;
        }
        //字符串转对象
        PasswordData passwordData = JSON.parseObject(pdString, PasswordData.class);
        return passwordData;
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
    private byte[] getMD5Byte(String password){
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            digest = md.digest(password.getBytes());
        }catch (Exception e){
            System.out.println(e);
        }
        return digest;
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

    /*
     * 解析pk文件的字符串
     * 返回的字符串数组：
     *------------------------32位不用返回的字符串
     * 0：AES(key,密码)的密文---若干
     * 1：nextFileName---------32
     * 2：nextUID--------------32
     * */

    public String[] analysisPKData(String data){
        //提取nextUID
        int length = data.length();
        if (length < 32){
            System.out.println("发现pk文件有误");
            return null;
        }

        String[] res = new String[3];
        res[2] = data.substring(length - 32,length);
        res[1] = data.substring(length - 64,length - 32);
        res[0] = data.substring(32,length - 64);
        return res;
    }

}
