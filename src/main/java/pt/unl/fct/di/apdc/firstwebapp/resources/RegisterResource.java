package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.loginDatav2DTO;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	//private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081").setProjectId("iconic-valve-379315").build().getService();
	
	public RegisterResource() {
	}

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doSignin(LoginData data) {
		LOG.fine("Attempt to register user: " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = Entity.newBuilder((userKey)).set("password", data.password).build();

		datastore.put(user);
		AuthToken at = new AuthToken(data.username);
		return Response.ok(g.toJson(at)).build();
	}

	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doSignin(loginDatav2DTO data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (data.username == null || data.username.contains(" ") || data.password == null
				|| data.confirm == null || !data.confirm.equals(data.password) || data.email == null
				|| data.name == null) {

			return Response.status(Status.BAD_REQUEST).build();
		}

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		
		
		if (datastore.get(userKey) != null) {
			return Response.status(Status.BAD_REQUEST).build();
		}

		Entity user = Entity.newBuilder((userKey)).set("password", data.password).set("email", data.email).set("name", data.name).build();

		datastore.put(user);
		AuthToken at = new AuthToken(data.username);
		return Response.ok(g.toJson(at)).build();
	}

}
