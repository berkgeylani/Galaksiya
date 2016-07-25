package com.galaksiya.newsobserver.parser.testutil;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

public class CreateRssMalformedItemTitle extends AbstractHandler
{


    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
                                                      ServletException
    {
        response.setContentType("text/xml; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath( "/rss" );
        PrintWriter out = response.getWriter();
        String context = "<rss xmlns:dc=\"http://purl.org/dc/elements/1.1/\" version=\"2.0\">"
        		+"<channel>"
        		+"<title>Sözcü</title>"
        		+"<link>http://www.sozcu.com.tr</link>"
        		+"<description>Tek gerçek gazete ve haber sitesi</description>"
        		+"<language>tr</language>"
        		+"<category>News</category>"
        		+"<lastBuildDate>Tue, 21 Jun 2016 10:21:48 GMT</lastBuildDate>"
        		+"<ttl>1</ttl>"
        		+"<image>"
        		+"<title>Sözcü</title>"
        		+"<url>http://i.sozcu.com.tr/static/sozcu_rss_logo.png</url>"
        		+"<width>120</width>"
        		+"<height>31</height>"
        		+"<link>http://www.sozcu.com.tr</link>"
        		+"<description>Tek gerçek gazete ve haber sitesi</description>"
        		+"</image>"
        		+"<item>"
        		+"<title>"
        		+"</title>"
        		+"<description>"
        		+"New York Times gazetesi, Osmanlı topraklarının paylaşılmasını öngören ve tüm taraflarla imzalanan Sykes-Picot Anlaşmasının 100. yıldönümünde arşivinden yeni bir harita çıkardı. Haritalar ise İngiltere ve Fransanın hazırladığı Sykes-Picotun alternatifleri. ORTADOĞU HARİTASI BU ŞEKİLDE ÇİZİLSEYDİ Haberde dönemin ABD Başkanı Woodrow Wilson tarafından hazırlatılan haritayla birlikte,1920lerde sınırlar bu şekilde çizilseydi Ortadoğu kurtarılabilir miydi? sorusu da yer alıyor. []"
        		+"</description>"
        		+"<pubDate>Tue, 21 Jun 2016 10:16:16 GMT</pubDate>"
        		+"<enclosure url=\"http://i.sozcu.com.tr/wp-content/uploads/2016/05/5-new-york-times-sykes-picot-ortadogu-880.jpg\" length=\"50000\" type=\"image/jpeg\"/>"
        		+"<link>"
        		+"http://www.sozcu.com.tr/2016/dunya/farkli-sinirlar-ortadoguyu-kurtarabilir-mi-1232284/"
        		+"</link>"
        		+"<guid isPermaLink=\"false\">"
        		+"http://www.sozcu.com.tr/2016/dunya/farkli-sinirlar-ortadoguyu-kurtarabilir-mi-1232284/"
        		+"</guid>"
        		+"</item>"
        		+"</channel>"
        		+"</rss>";
        out.print(context);
        baseRequest.setHandled(true);
    }
 }