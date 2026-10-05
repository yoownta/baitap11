package vn.iotstar.ktqt03.service;

import vn.iotstar.ktqt03.exception.ValidationException;

public enum OrderStatus {
    NEW("Đơn hàng mới"), CONFIRMED("Đã xác nhận"), PREPARING("Chuẩn bị hàng"),
    SHIPPING("Vận chuyển"), DELIVERING("Giao hàng"), DELIVERED("Đã giao"),
    CANCELLED("Đơn hàng hủy"), RETURNED("Đơn hàng hoàn");
    private final String label;
    OrderStatus(String label) { this.label=label; }
    public String getCode() { return name(); }
    public String getLabel() { return label; }
    public static OrderStatus parse(String value) {
        try { return valueOf(value); }
        catch(Exception e) { throw new ValidationException("Trạng thái không hợp lệ."); }
    }
    public boolean canChangeTo(OrderStatus next) {
        return switch(this) {
            case NEW -> next==CONFIRMED || next==CANCELLED;
            case CONFIRMED -> next==PREPARING || next==CANCELLED;
            case PREPARING -> next==SHIPPING || next==CANCELLED;
            case SHIPPING -> next==DELIVERING || next==RETURNED;
            case DELIVERING -> next==DELIVERED || next==RETURNED;
            case DELIVERED -> next==RETURNED;
            default -> false;
        };
    }
}
