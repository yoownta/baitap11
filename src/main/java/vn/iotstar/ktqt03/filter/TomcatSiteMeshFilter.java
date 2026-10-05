package vn.iotstar.ktqt03.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.sitemesh.webapp.SiteMeshFilter;
import org.sitemesh.webapp.WebAppContext;
import org.sitemesh.webapp.contentfilter.ResponseMetaData;

/** SiteMesh 3.2 uses forward for decorators, which commits an empty response on Tomcat 11.
 * Keep XML decoration/HTML parsing, but dispatch the decorator with include instead. */
public class TomcatSiteMeshFilter extends ConfigurableSiteMeshFilter {
    private ServletContext context;
    @Override public void init(FilterConfig config) throws ServletException {
        context=config.getServletContext();
        super.init(config);
    }
    @Override protected Filter setup() throws ServletException {
        SiteMeshFilter configured=(SiteMeshFilter)super.setup();
        SiteMeshFilterBuilder selector=new SiteMeshFilterBuilder();
        selector.addExcludedPath("/assets/*");
        return new SiteMeshFilter(selector.getSelector(),configured.getContentProcessor(),
                configured.getDecoratorSelector(),true) {
            @Override protected WebAppContext createContext(String type,HttpServletRequest req,
                    HttpServletResponse res,ResponseMetaData meta) {
                return new WebAppContext(type,req,res,context,getContentProcessor(),meta,true) {
                    @Override protected void dispatch(HttpServletRequest request,HttpServletResponse response,String path)
                            throws ServletException,IOException {
                        RequestDispatcher dispatcher=context.getRequestDispatcher(path);
                        if(dispatcher==null) throw new ServletException("Decorator not found: "+path);
                        dispatcher.include(request,response);
                    }
                };
            }
        };
    }
}
