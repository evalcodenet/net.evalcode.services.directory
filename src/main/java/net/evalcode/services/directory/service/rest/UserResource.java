package net.evalcode.services.directory.service.rest;


import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import net.evalcode.services.directory.internal.Directory;
import net.evalcode.services.directory.internal.Directory.DirectoryException;
import net.evalcode.services.http.exception.InternalServerErrorException;


/**
 * UserResource
 *
 * @author carsten.schipke@gmail.com
 */
@Path(/*directory/rest*/"user")
public class UserResource
{
  // MEMBERS
  @Inject
  Directory directory;


  // ACCESSORS
  @POST
  @Path(/*directory/rest/user*/"password")
  @RolesAllowed({"Employees"})
  public void password(@Context final SecurityContext securityContext,
      @HeaderParam("Services-Directory-Credential") final String credential,
      @HeaderParam("Services-Directory-Credential-New") final String credentialNew)
  {
    try
    {
      directory.updatePassword(
        securityContext.getUserPrincipal().getName(),
        credential,
        credentialNew
      );
    }
    catch(final DirectoryException e)
    {
      throw new InternalServerErrorException(e.getMessage(), e);
    }
  }
}
