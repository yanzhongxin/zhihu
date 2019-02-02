package com.yanzhongxin.zhihu.util;

import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.stereotype.Component;

@Component
public class SendEmailUtil {
	private static String  fromEmailAddress="747328867@qq.com"; //�����˵�������
	private static String fromEmailPassword="rnvlkadikxvtbfhf"; //�����˵���Ȩ�루ע�ⲻ�����룩
	private static String hostAddress="smtp.qq.com";            //�����������Ӧ�ķ�����������qq������ smtp.qq.com,163������smtp.163.com
	public static void sendMail(String destMailAddress,String title,String message){
		// �ռ��˵�������
        String to = destMailAddress;

        // �����˵�������
        String from = fromEmailAddress;

        // ָ�������ʼ�������Ϊ smtp.qq.com
        String host = "smtp.qq.com";  //QQ �ʼ�������

        // ��ȡϵͳ����
        Properties properties = System.getProperties();

        // �����ʼ�������
        properties.setProperty("mail.smtp.host", host);

        properties.put("mail.smtp.auth", "true");
        MailSSLSocketFactory sf=null;
		try {
			sf = new MailSSLSocketFactory();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);
        // ��ȡĬ��session����
        Session session = Session.getDefaultInstance(properties,new Authenticator(){
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(fromEmailAddress, fromEmailPassword); //�������ʼ��û���������
            }
        });

        try{
            // ����Ĭ�ϵ� MimeMessage ����
            MimeMessage message1 = new MimeMessage(session);

            // Set From: ͷ��ͷ�ֶ�
            message1.setFrom(new InternetAddress(from));

            // Set To: ͷ��ͷ�ֶ�
            message1.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: ͷ��ͷ�ֶ�
            message1.setSubject(title);

            // ������Ϣ��
            message1.setText(message);

            // ������Ϣ
            Transport.send(message1);
            System.out.println("Sent message successfully...");
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
	}
	
	public static String getFromEmailAddress() {
		return fromEmailAddress;
	}
	public static void setFromEmailAddress(String fromEmailAddress) {
		SendEmailUtil.fromEmailAddress = fromEmailAddress;
	}
	public static String getFromEmailPassword() {
		return fromEmailPassword;
	}
	public static void setFromEmailPassword(String fromEmailPassword) {
		SendEmailUtil.fromEmailPassword = fromEmailPassword;
	}
	public static String getHostAddress() {
		return hostAddress;
	}
	public static void setHostAddress(String hostAddress) {
		SendEmailUtil.hostAddress = hostAddress;
	}
}
