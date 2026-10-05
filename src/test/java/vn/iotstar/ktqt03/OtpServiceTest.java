package vn.iotstar.ktqt03;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import vn.iotstar.ktqt03.service.*;
import vn.iotstar.ktqt03.exception.ValidationException;

class OtpServiceTest {
    private final Instant now=Instant.parse("2026-09-23T00:00:00Z");
    private OtpService at(long seconds) { return new OtpService(Clock.fixed(now.plusSeconds(seconds),ZoneOffset.UTC)); }
    @Test void unsentOtpCannotActivateAndLeadingZeroIsPreserved() {
        OtpService service=at(0);OtpState s=service.createOtp("user","u@example.com");
        s.setCode("012345");
        assertThrows(ValidationException.class,()->service.verify(s,"012345"));
        service.markSent(s);assertDoesNotThrow(()->service.verify(s,"012345"));
        assertThrows(ValidationException.class,()->service.verify(s,"12345"));
    }
    @Test void expiresExactlyAtFiveMinutes() {
        OtpState s=at(0).createOtp("user","u@example.com");at(0).markSent(s);
        assertDoesNotThrow(()->at(299).verify(s,s.getCode()));
        assertThrows(ValidationException.class,()->at(300).verify(s,s.getCode()));
    }
    @Test void fifthWrongAttemptLocksEvenCorrectCode() {
        OtpState s=at(0).createOtp("user","u@example.com");at(0).markSent(s);
        for(int i=0;i<5;i++) assertThrows(ValidationException.class,()->at(1).verify(s,"bad"));
        assertThrows(ValidationException.class,()->at(1).verify(s,s.getCode()));
        OtpState fresh=at(60).resendOtp(s);at(60).markSent(fresh);
        assertNotEquals(s.getCode(),fresh.getCode());assertEquals(0,fresh.getAttempts());
        assertDoesNotThrow(()->at(60).verify(fresh,fresh.getCode()));
    }
    @Test void resendCooldownAndUnsentState() {
        OtpState s=at(0).createOtp("user","u@example.com");
        assertDoesNotThrow(()->at(0).resendOtp(s));
        at(0).markSent(s);
        assertThrows(ValidationException.class,()->at(59).resendOtp(s));
        assertDoesNotThrow(()->at(60).resendOtp(s));
    }
}
