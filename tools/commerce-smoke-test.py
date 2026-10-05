"""HTTP/JSP + real SQL Server + loopback SMTP integration test; no external email.
Run via commerce-smoke-test.ps1. Uses a fresh test database; the application's DB is untouched.
"""
import base64, email, email.policy, http.cookiejar, json, os, re
import socket, socketserver, subprocess, threading, time, urllib.error, urllib.parse, urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = 'http://127.0.0.1:18084/ktqt03'
RESULTS = []
MESSAGES = []
DB = 'BAITAP11_test_' + time.strftime('%Y%m%d_%H%M%S')
ENV = os.environ.copy()
ENV['KTQT_DB_URL'] = re.sub(r'(?i)databaseName=[^;]+', 'databaseName=' + DB, ENV['KTQT_DB_URL'])
ENV.update(KTQT_DB_INIT='true', KTQT_SMTP_HOST='127.0.0.1', KTQT_SMTP_PORT='12526',
           KTQT_SMTP_AUTH='false', KTQT_SMTP_STARTTLS='false', KTQT_SMTP_USER='test@example.com')

class SMTP(socketserver.StreamRequestHandler):
    def handle(self):
        self.wfile.write(b'220 localhost test SMTP\r\n')
        while line := self.rfile.readline():
            cmd = line.decode('ascii', 'replace').strip().upper()
            if cmd.startswith(('EHLO','HELO')): self.wfile.write(b'250 localhost\r\n')
            elif cmd == 'DATA':
                self.wfile.write(b'354 End with dot\r\n')
                data = bytearray()
                while (part := self.rfile.readline()) not in (b'.\r\n', b''):
                    data.extend(part)
                message = email.message_from_bytes(bytes(data), policy=email.policy.default)
                MESSAGES.append(message.get_content())
                self.wfile.write(b'250 accepted\r\n')
            elif cmd == 'QUIT': self.wfile.write(b'221 bye\r\n'); break
            else: self.wfile.write(b'250 OK\r\n')

class Server(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True

SQL_WORKER = None

def sql(statement):
    global SQL_WORKER
    if SQL_WORKER is None:
        SQL_WORKER=subprocess.Popen(['powershell','-NoProfile','-ExecutionPolicy','Bypass','-File',str(ROOT/'tools/sql-test-worker.ps1')],
                                    env=ENV,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE,
                                    text=True,encoding='utf-8',creationflags=subprocess.CREATE_NO_WINDOW)
    SQL_WORKER.stdin.write(base64.b64encode(statement.encode('utf-8')).decode('ascii')+'\n')
    SQL_WORKER.stdin.flush()
    line=SQL_WORKER.stdout.readline()
    if not line: raise AssertionError('SQL worker stopped: '+str(SQL_WORKER.poll()))
    result=json.loads(line)
    if not result['ok']: raise AssertionError('Test SQL failed: '+result['error'])
    return result['value'].strip()

jar = http.cookiejar.CookieJar()
client = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
def request(path, data=None):
    body = urllib.parse.urlencode(data).encode() if data is not None else None
    try:
        with client.open(BASE + path, data=body, timeout=25) as r:
            return r.status, r.read().decode('utf-8'), r.url
    except urllib.error.HTTPError as ex:
        return ex.code, ex.read().decode('utf-8', 'replace'), ex.url

def check(name, condition):
    if not condition: raise AssertionError(name)
    RESULTS.append(name)
    print('PASS ' + name, flush=True)

with socket.socket() as probe:
    if probe.connect_ex(('127.0.0.1',18084))==0:
        raise RuntimeError('Port 18084 already in use. Stop the old test Tomcat before testing.')
server = Server(('127.0.0.1',12526), SMTP)
threading.Thread(target=server.serve_forever,daemon=True).start()
log = (ROOT/'target'/'commerce-server.log').open('w', encoding='utf-8')
process = subprocess.Popen(['powershell','-NoProfile','-ExecutionPolicy','Bypass','-File',str(ROOT/'run.ps1'),
                            '-SkipBuild','-SkipLocalConfig','-TestRuntime','-Port','18084'], cwd=ROOT,
                           env=ENV, stdout=log, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
success = False
try:
    for _ in range(90):
        try:
            status, body, _ = request('/home')
            if status == 200 and 'video-card' in body: break
        except (OSError, urllib.error.URLError): pass
        if process.poll() is not None: raise AssertionError('Tomcat stopped; see target/commerce-server.log')
        time.sleep(1)
    else: raise AssertionError('Tomcat startup failed; see target/commerce-server.log')
    import html
    def hidden(body, name):
        found=re.search(r'name="'+name+r'" value="([^"]*)"',body)
        assert found, 'missing form '+name
        return html.unescape(found.group(1))
    def count_orders(): return int(sql('SELECT COUNT(*) FROM dbo.ShopOrders'))
    def stock(): return int(sql('SELECT Stock FROM dbo.ShopProducts WHERE ProductId=1'))
    def login(username='review_admin'):
        return request('/login',dict(username=username,password='ReviewPass03!'))
    def cart_change(action='add',quantity='1',product='1'):
        _, page, _=request('/shop' if action=='add' else '/cart')
        return request('/shop' if action=='add' else '/cart',dict(csrf=hidden(page,'csrf'),action=action,productId=product,quantity=quantity))
    def checkout_form():
        _, page, _=request('/checkout')
        return dict(csrf=hidden(page,'csrf'),checkoutToken=hidden(page,'checkoutToken'),recipient='Người nhận kiểm thử',phone='0901234567',address='123 Đường kiểm thử, TP Hồ Chí Minh',note='Giao giờ hành chính')
    check('home layout and UTF-8',status==200 and 'Phan Tuấn Thanh' in body and body.count('<footer>')==1)
    check('catalog JSP compiles',request('/shop')[0]==200)
    check('searchable library compiles',request('/library?q=Java')[0]==200)
    check('library pagination compiles',request('/library?page=2')[0]==200)
    check('anonymous cart requires login',request('/cart')[2].endswith('/login'))
    check('anonymous admin blocked',request('/admin/shop')[2].endswith('/login'))
    check('bundled MP4 player available', 'lesson-player' in request('/video?id=1')[1])
    media_request=urllib.request.Request(BASE+'/videos/java-01.mp4',headers={'Range':'bytes=0-1023'})
    with urllib.request.urlopen(media_request) as r:
        check('video supports byte range seeking',r.status==206 and len(r.read())==1024 and 'video/mp4' in r.headers.get('Content-Type',''))
    sql((ROOT/'src/main/resources/db/commerce.sql').read_text(encoding='utf-8'))
    check('commerce migration is idempotent',sql('SELECT COUNT(*) FROM dbo.ShopProducts')=='3')
    register=dict(username='review_admin',password='ReviewPass03!',confirmPassword='ReviewPass03!',fullName='Review Admin',email='review@example.com',phone='0900000000')
    _,_,url=request('/register',register)
    check('real registration sends OTP to loopback SMTP',url.endswith('/verify-otp') and len(MESSAGES)==1)
    code=re.search(r'\b[0-9]{6}\b',MESSAGES[-1]).group()
    request('/verify-otp',dict(otp=code))
    check('normal user can log in',login()[2].endswith('/home'))
    check('normal user cannot manage shop',request('/admin/shop')[0]==403)
    check('CSRF blocks forged cart POST',request('/shop',dict(action='add',productId='1',quantity='1'))[0]==403)
    check('add cart item',cart_change(quantity='2')[0]==200 and sql("SELECT Quantity FROM dbo.CartItems WHERE Username='review_admin' AND ProductId=1")=='2')
    for invalid in ['0','-1','1.5','99999','6']:
        cart_change('update',invalid)
        check('reject quantity '+invalid,sql("SELECT Quantity FROM dbo.CartItems WHERE Username='review_admin' AND ProductId=1")=='2')
    check('cart edits quantity',cart_change('update','3')[0]==200 and sql("SELECT Quantity FROM dbo.CartItems WHERE Username='review_admin' AND ProductId=1")=='3')
    cart_change('remove')
    check('cart removes item',sql('SELECT COUNT(*) FROM dbo.CartItems')=='0')
    cart_change(quantity='2')
    data=checkout_form();data['phone']='bad'
    request('/checkout',data)
    check('invalid COD address details do not create order',count_orders()==0 and stock()==25)
    data=checkout_form();data['phone']='0901234567'
    sql('UPDATE dbo.ShopProducts SET Price=100000 WHERE ProductId=1')
    request('/checkout',data)
    check('price change requires review before purchase',count_orders()==0 and stock()==25)
    data=checkout_form()
    _,body,url=request('/checkout',data)
    check('COD checkout creates order and compiles history',url.endswith('/orders') and count_orders()==1 and 'Đơn hàng mới' in body)
    check('checkout decrements stock and clears cart',stock()==23 and sql('SELECT COUNT(*) FROM dbo.CartItems')=='0')
    check('snapshot saves total and COD',sql("SELECT CAST(Total AS VARCHAR(30))+':'+PaymentMethod FROM dbo.ShopOrders")=='200000:COD')
    request('/checkout',data)
    check('repeated checkout does not create duplicate',count_orders()==1 and stock()==23)
    oid=sql('SELECT MAX(OrderId) FROM dbo.ShopOrders')
    _,page,_=request('/orders')
    request('/orders',dict(csrf=hidden(page,'csrf'),orderId=oid))
    check('user can cancel own new order',sql('SELECT Status FROM dbo.ShopOrders WHERE OrderId='+oid)=='CANCELLED' and stock()==25)
    sql("UPDATE dbo.ShopOrders SET Status='CANCELLED' WHERE OrderId="+oid)
    check('repeated cancellation never restores twice',stock()==25)
    cart_change(quantity='1');request('/checkout',checkout_form())
    oid=sql('SELECT MAX(OrderId) FROM dbo.ShopOrders')
    sql("UPDATE dbo.ShopOrders SET Status='CONFIRMED' WHERE OrderId="+oid)
    _,page,_=request('/orders')
    request('/orders',dict(csrf=hidden(request('/shop')[1],'csrf'),orderId=oid))
    check('user cannot cancel confirmed order',sql('SELECT Status FROM dbo.ShopOrders WHERE OrderId='+oid)=='CONFIRMED')
    for code,label in [('PREPARING','Chuẩn bị hàng'),('SHIPPING','Vận chuyển'),('DELIVERING','Giao hàng'),('DELIVERED','Đã giao'),('RETURNED','Đơn hàng hoàn')]:
        sql("UPDATE dbo.ShopOrders SET Status='"+code+"' WHERE OrderId="+oid)
        _,page,_=request('/orders?status='+code)
        check('database status '+code+' appears in matching filter','id="order-'+oid+'"' in page and label in page)
    check('return restores inventory once',stock()==25)
    check('SQL changes record all events',sql('SELECT COUNT(*) FROM dbo.ShopOrderEvents WHERE OrderId='+oid)=='7')
    check('empty status filter excludes unrelated orders','id="order-'+oid+'"' not in request('/orders?status=NEW')[1])
    check('invalid history status returns 400',request('/orders?status=BOGUS')[0]==400)
    # A second real account checks ownership boundaries.
    register.update(username='review_other',email='other@example.com')
    request('/register',register)
    request('/verify-otp',dict(otp=re.search(r'\b[0-9]{6}\b',MESSAGES[-1]).group()))
    login('review_other')
    check('other account cannot see first account orders','id="order-'+oid+'"' not in request('/orders')[1])
    cart_change(quantity='1')
    _,page,_=request('/orders')
    request('/orders',dict(csrf=hidden(request('/cart')[1],'csrf'),orderId=oid))
    check('other account cannot mutate first order',sql('SELECT Status FROM dbo.ShopOrders WHERE OrderId='+oid)=='RETURNED')
    check('cart is isolated by account',sql("SELECT COUNT(*) FROM dbo.CartItems WHERE Username='review_other'")=='1' and sql("SELECT COUNT(*) FROM dbo.CartItems WHERE Username='review_admin'")=='0')
    sql("UPDATE dbo.Users SET Admin=1 WHERE Username='review_admin'")
    login()
    check('admin product form compiles',request('/admin/shop')[0]==200)
    check('admin media form compiles',request('/admin/media')[0]==200)
    check('admin order form compiles',request('/admin/orders')[0]==200)
    _,page,_=request('/admin/shop')
    product=dict(csrf=hidden(page,'csrf'),productId='0',title='Bộ học thử',price='150000',stock='8',maxQuantity='2',videoId='1',active='on',mediaUrl='/videos/java-01.mp4')
    request('/admin/shop',product)
    check('admin creates product',sql('SELECT COUNT(*) FROM dbo.ShopProducts')=='4')
    product.update(productId='4',title='Bộ học đã sửa',price='155000')
    request('/admin/shop',product)
    check('admin edits product',sql('SELECT Price FROM dbo.ShopProducts WHERE ProductId=4')=='155000')
    check('order preserves historical price after edits',sql('SELECT Total FROM dbo.ShopOrders WHERE OrderId='+oid)=='100000')
    cart_change(quantity='1');request('/checkout',checkout_form());oid=sql('SELECT MAX(OrderId) FROM dbo.ShopOrders')
    for code in ['CONFIRMED','PREPARING','SHIPPING','DELIVERING','DELIVERED']:
        _,page,_=request('/admin/orders')
        request('/admin/orders',dict(csrf=hidden(page,'csrf'),orderId=oid,status=code))
        check('admin transitions '+code,sql('SELECT Status FROM dbo.ShopOrders WHERE OrderId='+oid)==code)
    # Stock becomes unavailable after adding but before checkout.
    cart_change(quantity='1');data=checkout_form();before=count_orders();sql('UPDATE dbo.ShopProducts SET Stock=0 WHERE ProductId=1')
    request('/checkout',data)
    check('checkout rejects stale inventory atomically',count_orders()==before and stock()==0)
    sql('UPDATE dbo.ShopProducts SET Stock=24 WHERE ProductId=1')
    cart_change('clear')
    # Verify set-based database trigger behavior and terminal-state integrity.
    before_stock=stock()
    new_ids=[]
    for _ in range(2):
        cart_change(quantity='1');request('/checkout',checkout_form())
        new_ids.append(sql('SELECT MAX(OrderId) FROM dbo.ShopOrders'))
    check('two new orders reserve inventory',stock()==before_stock-2)
    sql("UPDATE dbo.ShopOrders SET Status='CANCELLED' WHERE OrderId IN ("+','.join(new_ids)+")")
    check('multi-row SQL cancellation restores each item once',stock()==before_stock)
    try:
        sql("UPDATE dbo.ShopOrders SET Status='NEW' WHERE OrderId="+new_ids[0])
        reopening_rejected=False
    except AssertionError:
        reopening_rejected=True
    check('SQL cannot reopen terminal order',reopening_rejected and stock()==before_stock)
    # Two users compete for the last item: only one order may reserve it.
    sql('UPDATE dbo.ShopProducts SET Stock=1 WHERE ProductId=1')
    cart_change(quantity='1');admin_data=checkout_form();admin_client=client
    client=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
    login('review_other');other_data=checkout_form();other_client=client
    before=count_orders()
    def submit(opener,data):
        with opener.open(BASE+'/checkout',data=urllib.parse.urlencode(data).encode(),timeout=30) as r: r.read()
    from concurrent.futures import ThreadPoolExecutor
    with ThreadPoolExecutor(max_workers=2) as pool:
        futures=[pool.submit(submit,opener,form) for opener,form in [(admin_client,admin_data),(other_client,other_data)]]
        for future in futures: future.result()
    check('concurrent checkout cannot oversell final item',count_orders()==before+1 and stock()==0)
    client=admin_client
    request('/logout',{})
    check('logout protects orders',request('/orders')[2].endswith('/login'))
    success=True

finally:
    # Stop only the process tree this test started; leave other Tomcat instances alone.
    stopped = subprocess.run(['taskkill','/PID',str(process.pid),'/T','/F'],capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW)
    if stopped.returncode and process.poll() is None:
        print('WARNING: could not stop test process '+str(process.pid)+'. Stop test Tomcat before rerunning.',flush=True)
    server.shutdown();server.server_close();log.close()
    if SQL_WORKER is not None:
        SQL_WORKER.stdin.close()
        SQL_WORKER.wait(timeout=10)
    report=dict(passed=len(RESULTS),success=success,checks=RESULTS,testDatabase=DB,
                note='Test DB retained for inspection; no external email sent.')
    (ROOT/'target'/'commerce-results.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    print('RESULT '+str(len(RESULTS))+' passed; database '+DB,flush=True)
