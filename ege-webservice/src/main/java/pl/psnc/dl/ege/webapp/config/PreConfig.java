package pl.psnc.dl.ege.webapp.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class PreConfig implements ServletContextListener
{
	public static final String MIME_EXTENSION_PROVIDER = "mimeExtProv";
	
	public static final String LABEL_PROVIDER = "labelProvider";
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0)
	{
		
	}
	
	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		ServletContext context = event.getServletContext();
		// loads file extension to mime provider
		MimeExtensionProvider mimExtProv = MimeExtensionProvider.getInstance(context.getRealPath("/WEB-INF/config/fileExt.xml"));
		context.setAttribute(MIME_EXTENSION_PROVIDER, mimExtProv);
		
		//loads local labels provider
		LabelProvider lp = LabelProvider.getInstance(context.getRealPath("/WEB-INF/locale/"));
		context.setAttribute(LABEL_PROVIDER, lp);
	}
}
