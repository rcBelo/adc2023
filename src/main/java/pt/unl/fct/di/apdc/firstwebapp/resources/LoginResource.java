package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.servlet.http.HttpServletRequest;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.loginDatav2DTO;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	
	private final Gson g = new Gson();
	
	//private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final Datastore datastore = DatastoreOptions.newBuilder().setHost("http://localhost:8081").setProjectId("iconic-valve-379315").build().getService();
		
	
	public LoginResource() {} //nothing to me done here

	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {
		//System.out.println(data.username);
		LOG.fine("Attempt to login user: " + data.username);
		if(data.username.equals("jleitao") && data.password.equals("password")) {
			AuthToken at = new AuthToken(data.username);
			return Response.ok(g.toJson(at)).build();
			}
			return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
	}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLoginV1(LoginData data) {
		LOG.fine("Attempt to login user: " + data.username);
		

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		
		Entity user = datastore.get(userKey);
		
		if (user == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		if (!user.getString("password").equals(data.password)) {
			return Response.status(Status.BAD_GATEWAY).build();
		}

		AuthToken at = new AuthToken(data.username);
		return Response.ok(g.toJson(at)).build();
	}
	
	@POST
	@Path("/v2")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLoginV2(LoginData data) {
		LOG.fine("Attempt to login user: " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		
		Entity user = datastore.get(userKey);
		
		Key contadorKey = datastore.newKeyFactory().setKind("Info").newKey("Contador");
		
		Entity contador = datastore.get(contadorKey);
		
		if (contador == null) {
			contador = Entity.newBuilder((contadorKey)).set("positivo", 0).set("negativo", 0).build();
			datastore.put(contador);
		}
		
		if (user == null) {
			long neg = contador.getLong("negativo");
			neg++;
			contador = Entity.newBuilder(contador).set("negativo", neg).build();
			datastore.update(contador);
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		if (!user.getString("password").equals(data.password)) {
			long neg = contador.getLong("negativo");
			neg++;
			contador = Entity.newBuilder(contador).set("negativo", neg).build();
			datastore.update(contador);
			return Response.status(Status.BAD_GATEWAY).build();
		}
		
		long neg = contador.getLong("positivo");
		neg++;
		contador = Entity.newBuilder(contador).set("positivo", neg).build();
		datastore.update(contador);

		AuthToken at = new AuthToken(data.username);
		return Response.ok(g.toJson(contador)).build();
	}
	
	
	
	@GET
	@Path("/{username}")
	public Response checkUsernameAvailable(@PathParam("username") String username) {
	if(username.equals("jleitao")) {
	return Response.ok().entity(g.toJson(false)).build();
	} else {
	return Response.ok().entity(g.toJson(true)).build();
	}
	}
}
