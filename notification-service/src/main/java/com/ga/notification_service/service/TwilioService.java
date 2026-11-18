package com.ga.notification_service.service;

import com.ga.notification_service.client.CompanyClient;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class TwilioService {
    private CompanyClient companyClient;
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void initialize() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String mobileNumber, String message) {
        Message.creator(
                new PhoneNumber(mobileNumber),
                new PhoneNumber(twilioPhoneNumber),
                message
        ).create();
    }


    public void makeManagerCall(String mobileNumber) {
        String twiml = "<Response>"
                + "<Say language=\"en-US\">"
                + "This is an automated call from Arqemio Construction Management. "
                + "A risk report requires your review and approval. "
                + "Please open the link sent to your phone to review the risk."
                + "</Say>"
                + "<Say language=\"ar-SA\">"
                + "هذه مكالمة آلية من نظام أركيميو لإدارة المشاريع الإنشائية. "
                + "يوجد بلاغ خطر يتطلب مراجعتك والموافقة عليه أو رفضه. "
                + "يرجى فتح الرابط المرسل إلى هاتفك لمراجعة البلاغ."
                + "</Say>"
                + "</Response>";

        Call.creator(
                new PhoneNumber(mobileNumber),
                new PhoneNumber(twilioPhoneNumber),
                new Twiml(twiml)
        ).create();
    }

    public void makeWorkerCall(String mobileNumber, String riskTitle, String riskLocation, String callbackUrl, String callToken) {
        String safeRiskTitle = HtmlUtils.htmlEscape(riskTitle);
        String safeRiskLocation = HtmlUtils.htmlEscape(riskLocation);
        String safeCallbackUrl = HtmlUtils.htmlEscape(callbackUrl);

        String twiml = "<Response>"
                + "<Gather input=\"dtmf\" numDigits=\"1\" action=\""
                + safeCallbackUrl + "\" method=\"POST\" timeout=\"10\">"
                + "<Say language=\"en-US\">"
                + "This is an emergency safety alert from Arqemio. "
                + "A critical risk has been reported. "
                + "Risk: " + safeRiskTitle + ". "
                + "Location: " + safeRiskLocation + ". "
                + "Please avoid the affected area and follow site safety instructions. "
                + "Press 1 if you are safe. "
                + "Press 2 if you need immediate assistance."
                + "</Say>"
                + "<Say language=\"ar-SA\">"
                + "هذا تنبيه طوارئ من نظام أركيميو. "
                + "تم الإبلاغ عن خطر حرج. "
                + "نوع الخطر: " + safeRiskTitle + ". "
                + "الموقع: " + safeRiskLocation + ". "
                + "يرجى الابتعاد عن المنطقة المتأثرة واتباع تعليمات السلامة. "
                + "اضغط 1 إذا كنت بأمان. "
                + "اضغط 2 إذا كنت بحاجة إلى مساعدة فورية."
                + "</Say>"
                + "</Gather>"
                + "<Say language=\"en-US\">"
                + "No response received. Please update your status using the SMS link."
                + "</Say>"
                + "</Response>";
        Call call = Call.creator(
                new PhoneNumber(mobileNumber),
                new PhoneNumber(twilioPhoneNumber),
                new Twiml(twiml)
        ).create();

        companyClient.registerRiskCall(callToken, call.getSid());
    }

}


