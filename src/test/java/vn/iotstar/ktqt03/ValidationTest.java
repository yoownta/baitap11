package vn.iotstar.ktqt03;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import vn.iotstar.ktqt03.dto.VideoCard;
import vn.iotstar.ktqt03.exception.ValidationException;
import vn.iotstar.ktqt03.util.*;

class ValidationTest {
    @Test void rejectsInvalidVideoAndNormalizesValidInput() {
        VideoCard form=new VideoCard();form.setCategoryId(1);form.setTitle(" ");
        assertThrows(ValidationException.class,()->Validation.video(form));
        form.setTitle("Tiêu đề ");form.setViews(-1);
        assertThrows(ValidationException.class,()->Validation.video(form));
        form.setViews(0);form.setPoster("/assets/images/../secret.png");
        assertThrows(ValidationException.class,()->Validation.video(form));
        form.setPoster("");Validation.video(form);
        assertEquals("Tiêu đề",form.getTitle());assertEquals(Validation.DEFAULT_POSTER,form.getPoster());
        form.setDescription("x".repeat(501));
        assertThrows(ValidationException.class,()->Validation.video(form));
    }
    @Test void rejectsBadNumbersAndEmails() {
        for(String value:new String[]{"-1","abc","2147483648"})
            assertThrows(ValidationException.class,()->Validation.nonNegativeInt(value,"Views"));
        assertEquals(0,Validation.nonNegativeInt("","Views"));
        assertEquals("a@example.com",Validation.email(" A@example.com "));
        assertThrows(ValidationException.class,()->Validation.email("invalid"));
        assertThrows(ValidationException.class,()->Validation.username("<script>"));
    }
    @Test void passwordFitsExactExamColumn() {
        String encoded=PasswordUtil.hash("ExamplePassword03!");
        assertEquals(49,encoded.length());
        assertTrue(PasswordUtil.matches("ExamplePassword03!",encoded));
        assertFalse(PasswordUtil.matches("wrong",encoded));
        assertFalse(PasswordUtil.matches("anything",null));
    }
    @Test void invalidPageNeverCausesServerError() {
        for(String value:new String[]{null,"", "abc", "-1", "999999999999999999"})
            assertEquals(1,WebUtil.parsePage(value));
        assertEquals(2,WebUtil.parsePage("2"));
    }
}
