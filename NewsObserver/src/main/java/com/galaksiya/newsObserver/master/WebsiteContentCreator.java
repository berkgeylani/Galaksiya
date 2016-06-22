package com.galaksiya.newsObserver.master;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;

import com.galaksiya.newsObserver.database.MongoDb;

/**
 * this class fill the context with html from a selected query.
 * @author francium
 *
 */
@Path("/newsobserver")
public class WebsiteContentCreator {

	private MongoDb mongoDbHelper ;
	private DateUtils dateUtils = new DateUtils();
	public WebsiteContentCreator(){
		mongoDbHelper = new MongoDb();
	}
	public WebsiteContentCreator(MongoDb mongoHelper){
		mongoDbHelper=mongoHelper;
	}
	public String createContext(ArrayList<Document> dataAl){
		DateUtils dateUtils = new DateUtils();
		if(dataAl==null || dataAl.size()==0) return null;
		String content="<html>"
				+"<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Data</title></head>"
				+"<body>"
				+"<h4>Data</h4>";
		for (Document document : dataAl) {
			content+="<ul>"
					+"<li>"+dateUtils.dateCustomize(document.get("date").toString())+"</li>"
					+"<li>"+document.get("word").toString()+"</li>"
					+"<li>"+document.get("frequency").toString()+"</li>"
					+"</ul>";
		}
		content+="</body>"
				+"</html>";
		return content;
	}
	/**
	 * This fill a website content with a selected date-month-year with limited data.
	 * @param day
	 * @param month
	 * @param year
	 * @param limit It limits data whicwill be come.
	 * @return A html content
	 */
	@GET
	@Path("topLimitforday/{year}/{month}/{day}/{limit}")
	@Produces(MediaType.TEXT_HTML)
	public Response topLimitForADay(@PathParam("year") String year,@PathParam("month") String month,@PathParam("day") String day,@PathParam("limit") int limit) {
		if(day==null || month==null ||year==null || !dateUtils.canConvert(day+" "+month+" "+year) || limit < 1 )
			return Response.status(Status.BAD_REQUEST).entity("BAD REQUEST </br>"
					+ "topLimitforday/{First Parameter}/{Second Parameter}/{Third Parameter}/{Fourth Parameter} </br>"
					+ "First Parameter : Add a acceptable day 1-(30 | 31) on here</br>"
					+ "Second Parameter : Add a acceptable month 1-12 on here.ıt should be in text  and abbrevitaion from like: May,Jun</br>"
					+ "Third Parameter : Add a acceptable year on here</br>"
					+ "Fourth Parameter : The limit  which you want see data.Limit Should be greater that 0.").build();
		ArrayList<Document> dataAl = mongoDbHelper.fetch(day+" "+month+" "+year, limit);
		if(dataAl==null)
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("We have a problem in our databases.Please come back later again.").build();
		else if(dataAl.size()==0)
			return Response.status(Response.Status.NO_CONTENT).entity("We haven't got any data to show you.Please try any other parameter.").build();
		return Response.ok(createContext(dataAl )).build();
	}
	/**
	 * This fill a website content with a selected date-month-year.
	 * @param day
	 * @param month
	 * @param year
	 * @return A html content
	 */
	@GET
	@Path("perday/{year}/{month}/{day}")
	@Produces(MediaType.TEXT_HTML)
	public Response forADay(@PathParam("year") String year,@PathParam("month") String month,@PathParam("day") String day) {
		if(day==null || month==null ||year==null || !dateUtils.canConvert(day+" "+month+" "+year))
			return Response.status(Status.BAD_REQUEST).entity("BAD REQUEST </br>"
					+ "topLimitforday/{First Parameter}/{Second Parameter}/{Third Parameter}/{Fourth Parameter} </br>"
					+ "First Parameter : Add a acceptable day 1-(30 | 31) on here</br>"
					+ "Second Parameter : Add a acceptable month 1-12 on here.ıt should be in text  and abbrevitaion from like: May,Jun</br>"
					+ "Third Parameter : Add a acceptable year on here</br>"
					+ "Fourth Parameter : The limit  which you want see data.Limit Should be greater that 0.").build();		
		ArrayList<Document> dataAl = mongoDbHelper.fetch(day+" "+month+" "+year);
		if(dataAl==null)
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR </br>"
					+ "We have a problem in our databases.Please come back later again.").build();
		else if(dataAl.size()==0)
			return Response.status(Response.Status.NO_CONTENT).entity("We haven't got any data to show you."
					+ "Please try any other parameter.").build();
		return Response.ok(createContext(dataAl)).build(); 
	} 
	/**
	 * This is fill a website content with all the data sorted by frequency.
	 * @return
	 */
	@GET
	@Path("sortedAll")
	@Produces(MediaType.TEXT_HTML)
	public Response sortedAll() {
		ArrayList<Document> dataAl = mongoDbHelper.fetch();
		if(dataAl==null)
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR </br>We have a problem in our databases."
					+ "Please come back later again.").build();
		else if(dataAl.size()==0)
			return Response.status(Response.Status.NO_CONTENT).entity("NO CONTENT </br>We haven't got any data to show you."
					+ "Please try any other parameter.").build();
		return Response.ok(createContext(dataAl)).build(); 
	}
}