package com.galaksiya.newsObserver.parser.testutil;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

public class CreateRssWebsite extends AbstractHandler
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
        		+"<lastBuildDate>Mon, 16 May 2016 13:40:38 +0000</lastBuildDate>"
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
        	
        		+"</item>"
        		+"</channel>"
        		+"</rss>";
        out.print(context);
        baseRequest.setHandled(true);
    }
}