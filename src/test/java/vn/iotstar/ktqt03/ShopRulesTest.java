package vn.iotstar.ktqt03;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import vn.iotstar.ktqt03.service.*;
import vn.iotstar.ktqt03.exception.ValidationException;

class ShopRulesTest {
    @Test void quantitiesRejectZeroNegativeFractionOverflow() {
        for(String value:new String[]{"0","-1","100","1.5","999999999999","abc",null})
            assertThrows(ValidationException.class,()->ShopService.quantity(value));
        assertEquals(1,ShopService.quantity("1")); assertEquals(99,ShopService.quantity("99"));
    }
    @Test void terminalOrdersCannotBeReopened() {
        for(OrderStatus status:OrderStatus.values()) {
            assertFalse(OrderStatus.CANCELLED.canChangeTo(status));
            assertFalse(OrderStatus.RETURNED.canChangeTo(status));
        }
        assertTrue(OrderStatus.NEW.canChangeTo(OrderStatus.CONFIRMED));
        assertFalse(OrderStatus.NEW.canChangeTo(OrderStatus.DELIVERED));
        assertTrue(OrderStatus.DELIVERED.canChangeTo(OrderStatus.RETURNED));
    }
    @Test void mediaRejectsTraversalScriptsAndInsecureUrls() {
        for(String value:new String[]{"/videos/../private.mp4","javascript:alert(1)","http://example.com/a.mp4","//example.com/a.mp4","https://example.com/index.html","https://user:pass@example.com/a.mp4"})
            assertThrows(ValidationException.class,()->ShopService.media(value));
        assertEquals("/videos/java-01.mp4",ShopService.media("/videos/java-01.mp4"));
        assertEquals("https://example.com/a.mp4",ShopService.media("https://example.com/a.mp4"));
    }
}
