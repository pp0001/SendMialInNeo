package com.sap.cloud.sample.mail;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.annotation.Resource;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class MailServlet
 */
@WebServlet("/MailServlet")
public class MailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "mail/Session") 
    private Session mailSession; 

    private static final Logger LOGGER = LoggerFactory.getLogger(MailServlet.class);
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MailServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Show input form to user
        response.setHeader("Content-Type", "text/html");
        PrintWriter writer = response.getWriter();
        writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" "
                + "\"http://www.w3.org/TR/html4/loose.dtd\">");
        writer.write("<html><head><title>Mail Test</title></head><body>");
        writer.write("<form action='' method='post'>");
        writer.write("<table style='width: 100%'>");
        writer.write("<tr>");
        writer.write("<td width='100px'><label>From:</label></td>");
        writer.write("<td><input type='text' size='50' value='' name='fromaddress'></td>");
        writer.write("</tr>");
        writer.write("<tr>");
        writer.write("<td><label>To:</label></td>");
        writer.write("<td><input type='text' size='50' value='' name='toaddress'></td>");
        writer.write("</tr>");
        writer.write("<tr>");
        writer.write("<td><label>Subject:</label></td>");
        writer.write("<td><textarea rows='1' cols='100' name='subjecttext'>Subject</textarea></td>");
        writer.write("</tr>");
        writer.write("<tr>");
        writer.write("<td><label>Mail:</label></td>");
        writer.write("<td><textarea rows='7' cols='100' name='mailtext'>Mail Text</textarea></td>");
        writer.write("</tr>");
        writer.write("<tr>");
        writer.write("<tr>");
        writer.write("<td><input type='submit' value='Send Mail'></td>");
        writer.write("</tr>");
        writer.write("</table>");
        writer.write("</form>");
        writer.write("</body></html>");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Transport transport = null;
        try {
            // Parse form parameters
            String from = request.getParameter("fromaddress");
            String to = request.getParameter("toaddress");
            String subjectText = request.getParameter("subjecttext");
            String mailText = request.getParameter("mailtext");
            if (from.isEmpty() || to.isEmpty()) {
                throw new RuntimeException("Form parameters From and To may not be empty!");
            }
            
            String host = "smtp.gmail.com";
            Properties props = System.getProperties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.user", "pamela.piao9210@gmail.com");
            props.put("mail.smtp.password", "Pamela1001[]");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.debug", "true");

            Session session = Session.getInstance(props, new GMailAuthenticator("pamela.piao9210@gmail.com", "Pamela1001[]"));
//            		Session.getDefaultInstance(props, null);

            // Construct message from parameters
            MimeMessage mimeMessage = new MimeMessage(session);
            InternetAddress[] fromAddress = InternetAddress.parse(from);
            InternetAddress[] toAddresses = InternetAddress.parse(to);
            mimeMessage.setFrom(fromAddress[0]);
            mimeMessage.setRecipients(RecipientType.TO, toAddresses);
            mimeMessage.setSubject(subjectText, "UTF-8");
            MimeMultipart multiPart = new MimeMultipart("alternative");
            MimeBodyPart part = new MimeBodyPart();
            part.setText(mailText, "utf-8", "plain");
            multiPart.addBodyPart(part);
            mimeMessage.setContent(multiPart);
            
            transport = session.getTransport("smtp");
            transport.connect(host, "pamela.piao9210@gmail.com", "Pamela1001[]");
            mimeMessage.saveChanges();
            Transport.send(mimeMessage);
            transport.close();

            // Send mail
//            transport = session.getTransport();
//            transport.connect();
//            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

            // Confirm mail sending
            response.getWriter().println(
                    "E-mail was sent (in local scenario stored in '<local-server>/work/mailservice'"
                            + " - in cloud scenario using configured mail session).");
        } catch (Exception e) {
            LOGGER.error("Mail operation failed", e);
            throw new ServletException(e);
        } finally {
            // Close transport layer
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    throw new ServletException(e);
                }
            }
        }
	}

}
