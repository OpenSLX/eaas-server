package de.bwl.bwfla.emil;

import java.io.*;
import java.nio.channels.FileChannel;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.bwl.bwfla.common.utils.*;

import org.jboss.resteasy.specimpl.ResponseBuilderImpl;


@Path("Emil")
@ApplicationScoped
public class Emil extends EmilRest
{
	/* ### ADMIN Interfaces ### */

	@GET
	@Path("/buildInfo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response buildInfo()
	{
		JsonBuilder json = new JsonBuilder(DEFAULT_RESPONSE_CAPACITY);
		try {
			json.beginObject();
			json.add("status", "0");
			json.add("version", EaasBuildInfo.getVersion());
			json.endObject();
			json.finish();
			return Emil.createResponse(Status.OK, json.toString());
		} catch (IOException e) {
			return Emil.internalErrorResponse(e);
		}
	}

	@GET
	@Path("/serverLog")
	@Produces(MediaType.TEXT_PLAIN)
	public Response serverLog()
	{
		File logfile = new File("/home/bwfla/log/eaas.log");
		Response.ResponseBuilder builder = new ResponseBuilderImpl();
		builder.status(Status.OK);
		builder.entity(logfile);
		builder.header("Content-Disposition",
				"attachment; filename=\"eaas.log\"");
		return builder.build();
	}

	@GET
	@Path("/resetUsageLog")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetUsageLog()
	{
		File logfile = new File("/home/bwfla/server-data/sessions.csv");
		FileChannel outChan = null;
		try {
			outChan = new FileOutputStream(logfile, true).getChannel();
			outChan.truncate(0);
			outChan.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Response.ResponseBuilder builder = new ResponseBuilderImpl();
		builder.status(Status.OK);
		return builder.build();
	}

	@GET
	@Path("/usageLog")
	@Produces(MediaType.TEXT_PLAIN)
	public Response usageLog()
	{
		File logfile = new File("/home/bwfla/server-data/sessions.csv");
		Response.ResponseBuilder builder = new ResponseBuilderImpl();
		builder.status(Status.OK);
		builder.entity(logfile);
		builder.header("Content-Disposition",
				"attachment; filename=\"sessions.csv\"");
		return builder.build();
	}
}