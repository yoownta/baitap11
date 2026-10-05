package vn.iotstar.ktqt03.service;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.sql.*;
import java.math.BigDecimal;
import java.util.*;
import vn.iotstar.ktqt03.util.*;
import vn.iotstar.ktqt03.exception.ValidationException;

/** Parameterized JDBC transactions over the additive commerce schema. */
public class ShopService {
    @FunctionalInterface private interface Work<T> { T run(Connection c) throws SQLException; }
    private <T> T db(boolean transaction, Work<T> work) {
        SQLServerDataSource ds=new SQLServerDataSource();
        ds.setURL(Jpa.requiredEnv("KTQT_DB_URL"));
        ds.setUser(Jpa.requiredEnv("KTQT_DB_USER")); ds.setPassword(Jpa.requiredEnv("KTQT_DB_PASSWORD"));
        try(Connection c=ds.getConnection()) {
            if(transaction) c.setAutoCommit(false);
            try { T result=work.run(c); if(transaction)c.commit(); return result; }
            catch(SQLException|RuntimeException e) { if(transaction)c.rollback(); throw e; }
        } catch(SQLException e) { throw new IllegalStateException("Lỗi truy cập cửa hàng.",e); }
    }
    private static PreparedStatement statement(Connection c,String sql,Object...args) throws SQLException {
        PreparedStatement p=c.prepareStatement(sql);
        for(int i=0;i<args.length;i++)p.setObject(i+1,args[i]); return p;
    }
    private static int exec(Connection c,String sql,Object...args) throws SQLException {
        try(var p=statement(c,sql,args)){return p.executeUpdate();}
    }
    private static List<Map<String,Object>> rows(Connection c,String sql,Object...args) throws SQLException {
        try(var p=statement(c,sql,args);var r=p.executeQuery()) {
            List<Map<String,Object>> list=new ArrayList<>(); var meta=r.getMetaData();
            while(r.next()) { Map<String,Object> row=new LinkedHashMap<>();
                for(int i=1;i<=meta.getColumnCount();i++)row.put(meta.getColumnLabel(i),r.getObject(i));
                list.add(row);
            } return list;
        }
    }
    private static Map<String,Object> one(Connection c,String sql,Object...args) throws SQLException {
        var list=rows(c,sql,args); if(list.isEmpty())throw new ValidationException("Không tìm thấy dữ liệu."); return list.get(0);
    }
    public static int quantity(String raw) {
        try { int q=Integer.parseInt(raw); if(q<1||q>99)throw new NumberFormatException(); return q; }
        catch(Exception e){throw new ValidationException("Số lượng phải là số nguyên từ 1 đến 99.");}
    }
    public static String media(String raw) {
        String url=Validation.text(raw);
        if(url.isEmpty())return "";
        if(url.length()>1000)throw new ValidationException("Đường dẫn video quá dài.");
        if(url.matches("/videos/[A-Za-z0-9_.-]+\\.(?i:mp4|webm)"))return url;
        try { var uri=java.net.URI.create(url);
            if("https".equals(uri.getScheme()) && uri.getHost()!=null && uri.getRawUserInfo()==null
               && uri.getPath().matches(".*\\.(?i:mp4|webm)") && uri.getFragment()==null)return uri.toASCIIString();
        }catch(Exception ignored){}
        throw new ValidationException("Dùng /videos/tên-file.mp4 hoặc URL HTTPS của file MP4/WebM.");
    }
    public List<Map<String,Object>> products(String search,boolean admin) {
        String q=Validation.text(search);
        if(q.length()>100)throw new ValidationException("Tìm kiếm tối đa 100 ký tự.");
        return db(false,c->rows(c,"SELECT p.*,v.Poster,m.MediaUrl FROM dbo.ShopProducts p LEFT JOIN dbo.Videos v ON v.VideoId=p.VideoId LEFT JOIN dbo.VideoMedia m ON m.VideoId=p.VideoId WHERE (?=1 OR p.Active=1) AND p.Title LIKE ? ORDER BY p.ProductId",admin?1:0,"%"+q+"%"));
    }
    public List<Map<String,Object>> cart(String user) { return db(false,c->cart(c,user,false)); }
    private List<Map<String,Object>> cart(Connection c,String user,boolean lock) throws SQLException {
        var result=rows(c,"SELECT p.*,ci.Quantity FROM dbo.CartItems ci "+(lock?"WITH(UPDLOCK,HOLDLOCK) ":"")+"JOIN dbo.ShopProducts p "+(lock?"WITH(UPDLOCK,HOLDLOCK) ":"")+"ON p.ProductId=ci.ProductId WHERE ci.Username=? ORDER BY p.ProductId",user);
        for(var row:result) { int limit=Math.min((int)row.get("Stock"),(int)row.get("MaxQuantity"));
            row.put("Limit",limit); row.put("Subtotal",((BigDecimal)row.get("Price")).multiply(BigDecimal.valueOf((int)row.get("Quantity"))));
            row.put("Valid",Boolean.TRUE.equals(row.get("Active")) && (int)row.get("Quantity")<=limit);
        } return result;
    }
    public static BigDecimal total(List<Map<String,Object>> cart) {
        return cart.stream().map(r->(BigDecimal)r.get("Subtotal")).reduce(BigDecimal.ZERO,BigDecimal::add);
    }
    private void lockUser(Connection c,String user) throws SQLException {
        one(c,"SELECT Username FROM dbo.Users WITH(UPDLOCK,HOLDLOCK) WHERE Username=? AND Active=1",user);
    }
    public void changeCart(String user,int product,int quantity,boolean add) {
        db(true,c->{ lockUser(c,user);
            var p=one(c,"SELECT * FROM dbo.ShopProducts WITH(UPDLOCK,HOLDLOCK) WHERE ProductId=? AND Active=1",product);
            var old=rows(c,"SELECT Quantity FROM dbo.CartItems WITH(UPDLOCK,HOLDLOCK) WHERE Username=? AND ProductId=?",user,product);
            int next=quantity+(add&&!old.isEmpty()?(int)old.get(0).get("Quantity"):0);
            int max=Math.min((int)p.get("Stock"),(int)p.get("MaxQuantity"));
            if(next<1||next>max)throw new ValidationException("Số lượng vượt giới hạn/tồn kho hiện tại (tối đa "+max+").");
            if(old.isEmpty())exec(c,"INSERT dbo.CartItems VALUES(?,?,?)",user,product,next);
            else exec(c,"UPDATE dbo.CartItems SET Quantity=? WHERE Username=? AND ProductId=?",next,user,product);
            return null;
        });
    }
    public void remove(String user,Integer product) { db(true,c->{lockUser(c,user);
        if(product==null)exec(c,"DELETE dbo.CartItems WHERE Username=?",user);
        else exec(c,"DELETE dbo.CartItems WHERE Username=? AND ProductId=?",user,product);return null;}); }
    public static String fingerprint(List<Map<String,Object>> cart) {
        return cart.stream().map(r->r.get("ProductId")+":"+r.get("Quantity")+":"+r.get("Price")).reduce("",(a,b)->a+"|"+b);
    }
    public long checkout(String user,String token,String expected,String name,String phone,String address,String note) {
        String recipient=Validation.required(name,100,"Người nhận"), tel=Validation.text(phone),
            addr=Validation.required(address,500,"Địa chỉ"), memo=Validation.text(note);
        if(!tel.matches("0[0-9]{9,10}"))throw new ValidationException("Điện thoại gồm 10–11 chữ số, bắt đầu bằng 0.");
        if(memo.length()>500)throw new ValidationException("Ghi chú tối đa 500 ký tự.");
        try { UUID.fromString(token); }catch(Exception e){throw new ValidationException("Phiên thanh toán không hợp lệ.");}
        return db(true,c->{lockUser(c,user);
            var existing=rows(c,"SELECT OrderId FROM dbo.ShopOrders WHERE Username=? AND CheckoutToken=?",user,token);
            if(!existing.isEmpty())return ((Number)existing.get(0).get("OrderId")).longValue();
            var items=cart(c,user,true);
            if(items.isEmpty())throw new ValidationException("Giỏ hàng đang trống.");
            if(!fingerprint(items).equals(expected))throw new ValidationException("Giỏ hàng hoặc giá đã thay đổi. Kiểm tra lại tổng tiền rồi đặt hàng.");
            for(var item:items)if(!Boolean.TRUE.equals(item.get("Valid")))throw new ValidationException("Có sản phẩm hết hàng hoặc vượt giới hạn. Vui lòng sửa giỏ hàng.");
            long id=((Number)one(c,"INSERT dbo.ShopOrders(Username,Recipient,Phone,Address,Note,Total,CheckoutToken) OUTPUT inserted.OrderId VALUES(?,?,?,?,?,?,?)",user,recipient,tel,addr,memo,total(items),token).get("OrderId")).longValue();
            for(var item:items) {
                exec(c,"INSERT dbo.ShopOrderItems VALUES(?,?,?,?,?)",id,item.get("ProductId"),item.get("Title"),item.get("Price"),item.get("Quantity"));
                int updated=exec(c,"UPDATE dbo.ShopProducts SET Stock=Stock-? WHERE ProductId=? AND Stock>=?",item.get("Quantity"),item.get("ProductId"),item.get("Quantity"));
                if(updated!=1)throw new ValidationException("Tồn kho đã thay đổi. Vui lòng thử lại.");
            }
            exec(c,"INSERT dbo.ShopOrderEvents(OrderId,Status,Actor) VALUES(?,'NEW',?)",id,user);
            exec(c,"DELETE dbo.CartItems WHERE Username=?",user); return id;
        });
    }
    public List<Map<String,Object>> orders(String user,String status,boolean admin,int page) {
        String code=Validation.text(status); if(!code.isEmpty())OrderStatus.parse(code);
        return db(false,c->{var list=rows(c,"SELECT * FROM dbo.ShopOrders WHERE (?=1 OR Username=?) AND (?='' OR Status=?) ORDER BY CreatedAt DESC,OrderId DESC OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY",admin?1:0,user,code,code,(Math.max(1,Math.min(page,100000))-1)*20);
            for(var order:list){order.put("StatusLabel",OrderStatus.parse((String)order.get("Status")).getLabel());
                order.put("Items",rows(c,"SELECT *,Price*Quantity AS Subtotal FROM dbo.ShopOrderItems WHERE OrderId=? ORDER BY ProductId",order.get("OrderId")));
                order.put("Events",rows(c,"SELECT * FROM dbo.ShopOrderEvents WHERE OrderId=? ORDER BY EventId",order.get("OrderId")));
            }return list;});
    }
    public void changeStatus(String user,long id,String raw,boolean admin) {
        OrderStatus next=OrderStatus.parse(raw);
        db(true,c->{lockUser(c,user);
            var order=one(c,"SELECT * FROM dbo.ShopOrders WITH(UPDLOCK,HOLDLOCK) WHERE OrderId=? AND (?=1 OR Username=?)",id,admin?1:0,user);
            OrderStatus current=OrderStatus.parse((String)order.get("Status"));
            if(!admin && (current!=OrderStatus.NEW || next!=OrderStatus.CANCELLED))throw new ValidationException("Chỉ được hủy đơn hàng mới của bạn.");
            if(!current.canChangeTo(next))throw new ValidationException("Không thể chuyển trạng thái từ "+current.getLabel()+" sang "+next.getLabel()+".");
            exec(c,"UPDATE dbo.ShopOrders SET Status=?,UpdatedAt=SYSDATETIME() WHERE OrderId=?",next.name(),id);
            return null;});
    }
    public String mediaFor(String id) { return db(false,c->{var list=rows(c,"SELECT MediaUrl FROM dbo.VideoMedia WHERE VideoId=?",id);
        return list.isEmpty()?"":media((String)list.get(0).get("MediaUrl"));}); }
    public List<Map<String,Object>> library(String query,boolean admin,int page) {
        String q=Validation.text(query);
        if(q.length()>100)throw new ValidationException("Tìm kiếm tối đa 100 ký tự.");
        return db(false,c->rows(c,"SELECT v.*,m.MediaUrl,cat.Categoryname FROM dbo.Videos v LEFT JOIN dbo.VideoMedia m ON m.VideoId=v.VideoId LEFT JOIN dbo.Category cat ON cat.CategoryId=v.CategoryId WHERE (?=1 OR v.Active=1) AND (v.Title LIKE ? OR cat.Categoryname LIKE ?) ORDER BY v.VideoId OFFSET ? ROWS FETCH NEXT 12 ROWS ONLY",admin?1:0,"%"+q+"%","%"+q+"%",(Math.max(1,Math.min(page,100000))-1)*12));
    }
    public void saveMedia(String video,String url) {
        String id=Validation.id(video), source=media(url);
        db(true,c->{one(c,"SELECT VideoId FROM dbo.Videos WITH(UPDLOCK,HOLDLOCK) WHERE VideoId=?",id);
            exec(c,"DELETE dbo.VideoMedia WHERE VideoId=?",id);
            if(!source.isEmpty())exec(c,"INSERT dbo.VideoMedia VALUES(?,?)",id,source);
            return null;});
    }
    public void saveProduct(int id,String title,String price,String stock,String max,String video,boolean active,String mediaUrl) {
        String name=Validation.required(title,200,"Tên sản phẩm"), link=media(mediaUrl), videoId=Validation.text(video);
        BigDecimal amount;
        try{amount=new BigDecimal(price); if(amount.scale()>0||amount.signum()<=0||amount.compareTo(new BigDecimal("1000000000"))>0)throw new NumberFormatException();}
        catch(Exception e){throw new ValidationException("Giá phải là số nguyên từ 1 đến 1.000.000.000 đồng.");}
        int inventory=Validation.nonNegativeInt(stock,"Tồn kho"), limit=quantity(max);
        db(true,c->{if(!videoId.isEmpty())one(c,"SELECT VideoId FROM dbo.Videos WHERE VideoId=?",videoId);
            if(id==0)exec(c,"INSERT dbo.ShopProducts(Title,VideoId,Price,Stock,MaxQuantity,Active) VALUES(?,?,?,?,?,?)",name,videoId.isEmpty()?null:videoId,amount,inventory,limit,active);
            else if(exec(c,"UPDATE dbo.ShopProducts SET Title=?,VideoId=?,Price=?,Stock=?,MaxQuantity=?,Active=? WHERE ProductId=?",name,videoId.isEmpty()?null:videoId,amount,inventory,limit,active,id)!=1)throw new ValidationException("Sản phẩm không tồn tại.");
            if(!videoId.isEmpty()) {exec(c,"DELETE dbo.VideoMedia WHERE VideoId=?",videoId);if(!link.isEmpty())exec(c,"INSERT dbo.VideoMedia VALUES(?,?)",videoId,link);}
            return null;});
    }
}
