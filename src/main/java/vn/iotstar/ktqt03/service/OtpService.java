package vn.iotstar.ktqt03.service;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Locale;
import vn.iotstar.ktqt03.exception.ValidationException;

public class OtpService {
    private final Clock clock;
    private final SecureRandom random=new SecureRandom();
    public OtpService(){this(Clock.systemUTC());}
    public OtpService(Clock clock){this.clock=clock;}
    public OtpState createOtp(String username,String email) {
        OtpState s=new OtpState();
        s.setUsername(username); s.setEmail(email);
        s.setCode(String.format(Locale.ROOT,"%06d",random.nextInt(1_000_000)));
        s.setExpiresAt(clock.instant().plusSeconds(300));
        // sentAt stays null until the SMTP server accepts the message.
        return s;
    }
    public void markSent(OtpState s) {
        s.setSentAt(clock.instant());
        s.setExpiresAt(clock.instant().plusSeconds(300));
    }
    public OtpState resendOtp(OtpState old) {
        if(old==null) throw new ValidationException("Không còn phiên kích hoạt.");
        if(old.getSentAt()!=null && clock.instant().isBefore(old.getSentAt().plusSeconds(60)))
            throw new ValidationException("Vui lòng đợi 60 giây trước khi gửi lại.");
        OtpState next=createOtp(old.getUsername(),old.getEmail());
        while(next.getCode().equals(old.getCode())) next=createOtp(old.getUsername(),old.getEmail());
        return next;
    }
    public void verify(OtpState s,String code) {
        if(s.getSentAt()==null) throw new ValidationException("OTP chưa gửi thành công. Vui lòng gửi lại.");
        if(s.getAttempts()>=5) throw new ValidationException("Đã nhập sai 5 lần. Vui lòng gửi lại OTP.");
        if(!clock.instant().isBefore(s.getExpiresAt())) throw new ValidationException("OTP đã hết hạn. Vui lòng gửi lại.");
        if(code==null || !code.matches("[0-9]{6}") || !s.getCode().equals(code)) {
            s.incrementAttempts();
            throw new ValidationException("OTP không đúng. Còn "+(5-s.getAttempts())+" lần thử.");
        }
    }
}
