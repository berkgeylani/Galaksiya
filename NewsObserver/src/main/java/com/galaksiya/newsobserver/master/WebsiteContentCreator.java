package com.galaksiya.newsobserver.master;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;

import com.galaksiya.newsobserver.database.Database;
import com.galaksiya.newsobserver.database.DatabaseFactory;
import com.galaksiya.newsobserver.database.MongoDb;

/**
 * this class fill the context with html from a selected query.
 * @author francium
 *
 */
@Path("/newsobserver")
public class WebsiteContentCreator {

	private static final String BAD_REQUEST_ANSWER = "BAD REQUEST </br>"
			+ "topLimitforday/{First Parameter}/{Second Parameter}/{Third Parameter}/{Fourth Parameter} </br>"
			+ "First Parameter : Add a acceptable day 1-(30 | 31) on here</br>"
			+ "Second Parameter : Add a acceptable month 1-12 on here.ıt should be in text  and abbrevitaion from like: May,Jun</br>"
			+ "Third Parameter : Add a acceptable year on here</br>"
			+ "Fourth Parameter : The limit  which you want see data.Limit Should be greater that 0.";
	private DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
	private DateUtils dateUtils = new DateUtils();
	private Database dbHelper ;
	public WebsiteContentCreator(){
		dbHelper = databaseFactory.getDatabase();
	}
	public WebsiteContentCreator(Database dbHelperArg){
		dbHelper=dbHelperArg;
	}
	public String createContext(List<Document> dataAl,String collectionToShowInTitle){
		Database dbForNews = databaseFactory.getDatabase(collectionToShowInTitle);
		if(dataAl==null || dataAl.isEmpty()) return null;
		String content="<html>"
				+"<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><title>Data-"+ dbForNews.getNews().size() +"</title></head>"
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
			return Response.status(Status.BAD_REQUEST).entity(BAD_REQUEST_ANSWER).build();		
		List<Document> dataAl = dbHelper.fetch(day+" "+month+" "+year);
//		if(dataAl==null)
//			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR </br>"
//					+ "We have a problem in our databases.Please come back later again.").build();
//		else 
		if(dataAl.isEmpty())
			return Response.status(Response.Status.NO_CONTENT).entity("We haven't got any data to show you."
					+ "Please try any other parameter.").build();
		return Response.ok(createContext(dataAl ,"news")).build(); 
	}
	/**
	 * This is fill a website content with all the data sorted by frequency.
	 * @return
	 */
	@GET
	@Path("sortedAll")
	@Produces(MediaType.TEXT_HTML)
	public Response sortedAll() {
		List<Document> dataAl = dbHelper.fetch();
//		if(dataAl==null)
//			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("INTERNAL_SERVER_ERROR </br>We have a problem in our databases."
//					+ "Please come back later again.").build();
//		else 
		if(dataAl.isEmpty())
			return Response.status(Response.Status.NO_CONTENT).entity("NO CONTENT </br>We haven't got any data to show you."
					+ "Please try any other parameter.").build();
		return Response.ok(createContext(dataAl ,"news")).build(); 
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
			return Response.status(Status.BAD_REQUEST).entity(BAD_REQUEST_ANSWER).build();
		List<Document> dataAl = dbHelper.fetch(day+" "+month+" "+year, limit);
//		if(dataAl==null)
//			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("We have a problem in our databases.Please come back later again.").build();
//		else 
		if(dataAl.isEmpty())
			return Response.status(Response.Status.NO_CONTENT).entity("We haven't got any data to show you.Please try any other parameter.").build();
		return Response.ok(createContext(dataAl ,"news")).build();
	}
}