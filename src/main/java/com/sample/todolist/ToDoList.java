package com.sample.todolist;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sample.todolist.model.ListItem;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;

/**
 * A sample resource that provides access to todolist application methods.
 */
@Path(value = "/todolist")
public class ToDoList {
	public ToDoList() {

	}

	private static List<ListItem> list;

	@GET
	@Produces(value = "text/plain")
	public String getList() {
		StringBuffer buffer = new StringBuffer();
		if (list != null && list.size() > 0) {
			buffer.append("{");
			for (int i = 0; i < list.size(); ++i) {
				if (i != 0)
					buffer.append(", ");
				buffer.append(list.get(i)); 
			}
			buffer.append(" }");
		} else {
			buffer.append("The list is empty.");
		}
		return buffer.toString();
	}

	@GET
	@Produces(value = "text/plain")
	@Path(value = "{id}")
	public String getPropety(@PathParam("id") int id) {
		if ((list != null) && (id > -1) && (id < list.size() - 1)) {
			return list.get(id).toString();
		} else {
			return "Name Not Found";
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveListItem(InputStream incomingData) {
		StringBuilder listBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				listBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing: - ");
		}

		try {
			JSONObject json = new JSONObject(listBuilder.toString());
			Iterator<String> iterator = json.keys();
			if (list == null) {
				list = new ArrayList<ListItem>();
			}
			ListItem item = new ListItem((String) json.get(iterator.next()),
					(String) json.get(iterator.next()),
					(Boolean)json.get(iterator.next()));
			list.add(item);
		} catch (JSONException e) {	
			e.printStackTrace();
			return Response.status(500).entity(listBuilder.toString()).build();
		}
		// return HTTP response 200 in case of success
		return Response.status(200).entity(listBuilder.toString()).build();
	}

	@DELETE
	@Produces(value = "text/plain")
	@Path("/{title}/{body}/{done}")
	public String delete(@PathParam("title") String title,
			@PathParam("body") String body, @PathParam("done") String done) {
		System.out.println("Incoming values   : " + title + " " + body + " "
				+ done);
		Boolean doneFlag = Boolean.parseBoolean(done);
		ListItem deleteItem = new ListItem(title, body, doneFlag);
		list.remove(deleteItem);
		return deleteItem.toString();
	}

	@PUT
	@Path("{title}/{body}/{isDone}")
	@Produces(value = "text/plain")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response toggleStatusListItem(@PathParam("title") String title,
			@PathParam("body") String body, @PathParam("isDone") boolean isDone) {
		// Find your Account Sid and Token at twilio.com/user/account
		ListItem item = new ListItem(title, body, isDone);
		boolean done = isDone ? false : true;
		int index = list.indexOf(item);
		item.setDone(done);
		list.remove(index);
		list.add(index, item);

		if (isDone == Boolean.FALSE) {

			String ACCOUNT_SID = "";
			String AUTH_TOKEN = "";

			TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID,
					AUTH_TOKEN);
			try {
				// Build a filter for the MessageList
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Body", "Task : " + body
						+ "is complete."));
				params.add(new BasicNameValuePair("To", "+14086270378"));
				params.add(new BasicNameValuePair("From", "+18316847487"));

				MessageFactory messageFactory = client.getAccount()
						.getMessageFactory();
				Message message = messageFactory.create(params);
				System.out.println(message.getSid());
				return Response.status(200).entity(body.toString()).build();
			} catch (TwilioRestException e) {
				System.out.println(e.getErrorMessage());
			}
		}
		return Response.status(500).entity(body.toString()).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addUpdateListItem(InputStream incomingData) {
		StringBuilder listBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				listBuilder.append(line);
			}
		} catch (Exception e) {
			System.out.println("Error Parsing :- ");
		}

		try {
			JSONObject json = new JSONObject(listBuilder.toString());
			Iterator<String> iterator = json.keys();
			ListItem item = new ListItem((String) json.get(iterator.next()),
					(String) json.get(iterator.next()),
					Boolean.parseBoolean((String) json.get(iterator.next())));
			int index = list.indexOf(item);
			item = list.get(index);
			list.remove(index);
			item.setTitle((String) json.get(iterator.next()));
			item.setBody((String) json.get(iterator.next()));
			list.add(index, item);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// return HTTP response 200 in case of success
		return Response.status(200).entity(listBuilder.toString()).build();
	}
}
