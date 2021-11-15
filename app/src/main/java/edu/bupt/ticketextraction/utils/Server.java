package edu.bupt.ticketextraction.utils;

import edu.bupt.ticketextraction.bill.tickets.CabTicket;
import edu.bupt.ticketextraction.setting.contact.Contact;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *     author : 武连增
 *     e-mail : wulianzeng@bupt.edu.cn
 *     time   : 2021/10/07
 *     desc   : Server工具类，用于调用服务端API，不可实例化
 *     version: 0.0.1
 * </pre>
 */
@SuppressWarnings("unused")
public final class Server {
    private final static String securityCode = "";
    private final static String EMAIL_URL = "/mail";
    private final static String LOGIN_URL = "/login";
    private final static String REGISTER_URL = "/register";
    private final static String GET_EMAIL_URL = "/getMails";

    /**
     * Server工具类，请不要实例化此类！
     */
    private Server() {
        throw new AssertionError();
    }

    /**
     * 调用登录服务
     *
     * @param phoneNumber 账号
     * @param password    密码
     * @return 1-登录成功, 0-密码错误, -1-用户名不存在
     */
    @Contract(pure = true)
    public static int callLogin(String phoneNumber, String password) {
        // 加密密码
        String cipherText = Server.passwordEncrypt(password);
        HashMap<String, String> map = new HashMap<>();
        map.put("phone", phoneNumber);
        map.put("key", cipherText);
        String res = Server.post(LOGIN_URL, map);
        return Integer.parseInt(res);
    }

    /**
     * @param phoneNumber 手机号
     * @return 从数据库中查到的联系人
     */
    @Contract(pure = true)
    public static Contact @NotNull [] callGetContacts(@NotNull String phoneNumber) {
        Contact[] contacts = new Contact[4];
        HashMap<String, String> map = new HashMap<>();
        map.put("phone", phoneNumber);
//        map = Server.post(url, map);
        return contacts;
    }

    /**
     * 调用注册服务
     *
     * @param phoneNumber      账号
     * @param password         密码
     * @param verificationCode 验证码
     * @return 注册成功与否
     */
    @Contract(pure = true)
    public static boolean callRegister(String phoneNumber, String password, String verificationCode) {
        // 加密密码
        String cipherText = Server.passwordEncrypt(password);
        HashMap<String, String> map = new HashMap<>();
        return true;
    }

    /**
     * 验证手机号是否为本人手机号
     *
     * @param phoneNumber      账户
     * @param verificationCode 验证码
     * @return 验证码是否匹配
     */
    @Contract(pure = true)
    public static boolean callAccountVerification(String phoneNumber, String verificationCode) {
        return true;
    }

    /**
     * 发送验证码短信到指定手机号上
     *
     * @param phoneNumber 手机号
     * @return 返回的验证码
     */
    @Contract(pure = true)
    public static @NotNull String callVerificationSending(String phoneNumber) {
        return "6626";
    }

    /**
     * 把出租车发票的信息转换成表格
     *
     * @param tickets 待发送的所有票据信息
     * @param email   目标邮箱
     * @return 是否成功
     */
    @Contract(pure = true)
    public static boolean sendEmail(@NotNull ArrayList<CabTicket> tickets, String email) {
        // 每个发票一行，再加第一行的说明
        final int rowCnt = tickets.size() + 1;
        // 第一列的序号，再加上单价、距离、总价、日期四列
        final int columnCnt = 5;
        String[] firstRow = {"发票", "单价", "距离", "总价", "日期"};
        String[][] infos = new String[rowCnt][columnCnt];
        // 第一行说明信息
        System.arraycopy(firstRow, 0, infos[0], 0, columnCnt);
        for (int i = 1; i < rowCnt; ++i) {
            // 序号、单价、距离、总价、日期
            CabTicket ticket = tickets.get(i);
            infos[i][0] = String.valueOf(i);
            infos[i][1] = String.valueOf(ticket.getUnitPrice());
            infos[i][2] = String.valueOf(ticket.getDistance());
            infos[i][3] = String.valueOf(ticket.getTotalPrice());
            infos[i][4] = ticket.getDate();
        }
        HashMap<String, String> map = new HashMap<>();
        Server.post(EMAIL_URL, map);
        return false;
    }

    @Contract(pure = true)
    public static void callCheckTicketValid() {
        //TODO 验真
    }

    @Contract(pure = true)
    public static void callOcr() {
        //TODO 识别
    }

    /**
     * 获得加密后的密码
     *
     * @param plainText 明文
     * @return 密文
     */
    private static @NotNull String passwordEncrypt(@NotNull String plainText) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("sha");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // messageDigest必不为空，因为输入sha算法一定是正确的
        assert messageDigest != null;
        messageDigest.update(plainText.getBytes());
        return new BigInteger(messageDigest.digest()).toString(32);
    }

    private static String post(@NotNull String urlStr, @NotNull Map<String, String> params) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            //以下两行必须加否则报错.
            conn.setDoInput(true);
            conn.setDoOutput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert conn != null;
        StringBuilder s = new StringBuilder();
        // 使用try-with-resources替代try-catch-finally
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

            StringBuilder psBuilder = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                psBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            String ps = psBuilder.substring(0, psBuilder.lastIndexOf("&"));

            writer.write(ps + "\r\n");

            writer.flush();
            String line;
            while ((line = reader.readLine()) != null) {
                s.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s.toString();
    }
}
