package vn.iotstar.ktqt03.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import vn.iotstar.ktqt03.dto.SessionUser;
import vn.iotstar.ktqt03.service.*;
import vn.iotstar.ktqt03.util.*;
import vn.iotstar.ktqt03.exception.ValidationException;

@WebServlet(urlPatterns={"/shop","/library","/cart","/checkout","/orders","/admin/shop","/admin/orders","/admin/media"})
public class ShopServlet extends HttpServlet {
    private final ShopService shop=new ShopService();
    private SessionUser user(HttpServletRequest req) {return (SessionUser)req.getSession().getAttribute("authUser");}
    private String token(HttpServletRequest req,String key) {
        var session=req.getSession(); synchronized(session) {
            String value=(String)session.getAttribute(key);
            if(value==null){value=UUID.randomUUID().toString();session.setAttribute(key,value);} return value;
        }
    }
    private boolean authorized(HttpServletRequest req,HttpServletResponse res) throws IOException {
        if(!List.of("/shop","/library").contains(req.getServletPath()) && user(req)==null){res.sendRedirect(req.getContextPath()+"/login");return false;}
        if(req.getServletPath().startsWith("/admin/") && !user(req).isAdmin()){res.sendError(403);return false;} return true;
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        if(!authorized(req,res))return;
        res.setHeader("Cache-Control","no-store");
        req.setAttribute("csrf",token(req,"shop.csrf")); WebUtil.consumeFlash(req);
        try {
            String path=req.getServletPath(); boolean admin=path.startsWith("/admin/");
            req.setAttribute("adminShop",admin); req.setAttribute("statuses",OrderStatus.values());
            if(path.equals("/library") || path.equals("/admin/media")) {
                int page=WebUtil.parsePage(req.getParameter("page")); req.setAttribute("libraryPage",page);
                req.setAttribute("lessons",shop.library(req.getParameter("q"),admin,page));
                WebUtil.render(req,res,"shop/library");
            } else if(path.endsWith("orders")) {
                int page=WebUtil.parsePage(req.getParameter("page"));
                req.setAttribute("orderPage",page);req.setAttribute("filter",Validation.text(req.getParameter("status")));
                req.setAttribute("orders",shop.orders(user(req).getUsername(),req.getParameter("status"),admin,page));
                WebUtil.render(req,res,"shop/orders");
            } else if(path.endsWith("shop")) {
                req.setAttribute("products",shop.products(req.getParameter("q"),admin));
                WebUtil.render(req,res,"shop/catalog");
            } else {
                var cart=shop.cart(user(req).getUsername()); req.setAttribute("cart",cart);req.setAttribute("total",ShopService.total(cart));
                req.setAttribute("canCheckout",!cart.isEmpty()&&cart.stream().allMatch(r->Boolean.TRUE.equals(r.get("Valid"))));
                if(path.equals("/checkout")) {
                    synchronized(req.getSession()) {
                        if(token(req,"shop.checkout").equals(req.getSession().getAttribute("shop.lastCheckout"))) {
                            req.getSession().removeAttribute("shop.checkout");
                            req.getSession().removeAttribute("shop.checkoutFields");
                        }
                        req.setAttribute("checkoutToken",token(req,"shop.checkout"));
                        req.getSession().setAttribute("shop.expectedCart",ShopService.fingerprint(cart));
                    }
                    Object fields=req.getSession().getAttribute("shop.checkoutFields");
                    req.setAttribute("checkoutFields",fields);
                }
                WebUtil.render(req,res,path.equals("/checkout")?"shop/checkout":"shop/cart");
            }
        }catch(ValidationException e){res.setStatus(400);req.setAttribute("error",e.getMessage());WebUtil.render(req,res,"shop/error");}
        catch(RuntimeException e){log("Shop read failed",e);res.setStatus(500);req.setAttribute("error","Không thể tải cửa hàng. Kiểm tra kết nối SQL Server.");WebUtil.render(req,res,"shop/error");}
    }
    private int id(String value) {try{int id=Integer.parseInt(value);if(id<=0)throw new NumberFormatException();return id;}
        catch(Exception e){throw new ValidationException("Mã sản phẩm không hợp lệ.");}}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        if(!authorized(req,res))return;
        if(user(req)==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        if(!token(req,"shop.csrf").equals(req.getParameter("csrf"))){res.sendError(403,"Phiên thao tác không hợp lệ. Tải lại trang.");return;}
        String path=req.getServletPath(), redirect=path.equals("/shop")?"/cart":path;
        try {
            String action=req.getParameter("action"), username=user(req).getUsername();
            switch(path) {
                case "/shop" -> {
                    if(!"add".equals(action))throw new ValidationException("Thao tác không hợp lệ.");
                    shop.changeCart(username,id(req.getParameter("productId")),ShopService.quantity(req.getParameter("quantity")),true);
                }
                case "/cart" -> {
                    if("update".equals(action))shop.changeCart(username,id(req.getParameter("productId")),ShopService.quantity(req.getParameter("quantity")),false);
                    else if("remove".equals(action))shop.remove(username,id(req.getParameter("productId")));
                    else if("clear".equals(action))shop.remove(username,null);
                    else throw new ValidationException("Thao tác không hợp lệ.");
                }
                case "/checkout" -> {
                    String checkout=req.getParameter("checkoutToken");
                    if(!token(req,"shop.checkout").equals(checkout))throw new ValidationException("Phiên thanh toán đã thay đổi. Vui lòng tải lại trang.");
                    long order=shop.checkout(username,checkout,(String)req.getSession().getAttribute("shop.expectedCart"),req.getParameter("recipient"),req.getParameter("phone"),req.getParameter("address"),req.getParameter("note"));
                    // Keep the token for retries; rotate when a new checkout GET follows a successful order.
                    req.getSession().setAttribute("shop.lastCheckout",checkout);
                    WebUtil.flash(req,"message","Đặt đơn #"+order+" thành công. Thanh toán COD khi nhận hàng.");
                    res.sendRedirect(req.getContextPath()+"/orders");return;
                }
                case "/orders", "/admin/orders" -> {
                    long order;
                    try{order=Long.parseLong(req.getParameter("orderId"));if(order<=0)throw new NumberFormatException();}
                    catch(Exception e){throw new ValidationException("Mã đơn không hợp lệ.");}
                    shop.changeStatus(username,order,path.equals("/orders")?"CANCELLED":req.getParameter("status"),user(req).isAdmin()&&path.startsWith("/admin/"));
                }
                case "/admin/shop" -> shop.saveProduct("0".equals(req.getParameter("productId"))?0:id(req.getParameter("productId")),req.getParameter("title"),req.getParameter("price"),req.getParameter("stock"),req.getParameter("maxQuantity"),req.getParameter("videoId"),"on".equals(req.getParameter("active")),req.getParameter("mediaUrl"));
                case "/admin/media" -> shop.saveMedia(req.getParameter("videoId"),req.getParameter("mediaUrl"));
                default -> throw new ValidationException("Thao tác không hợp lệ.");
            }
            WebUtil.flash(req,"message","Đã cập nhật thành công.");
        }catch(ValidationException e){
            WebUtil.flash(req,"error",e.getMessage());
            if(path.equals("/checkout")) {
                Map<String,String> fields=new HashMap<>();for(String key:List.of("recipient","phone","address","note"))fields.put(key,req.getParameter(key));
                req.getSession().setAttribute("shop.checkoutFields",fields);
            }
        }catch(RuntimeException e){log("Shop write failed",e);WebUtil.flash(req,"error","Chưa thể lưu thay đổi. Vui lòng thử lại.");}
        res.sendRedirect(req.getContextPath()+redirect);
    }
}
