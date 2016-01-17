package net.evalcode.services.directory.internal;


import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.LDAPConnectionFactory;
import org.forgerock.opendj.ldap.LDAPOptions;
import org.forgerock.opendj.ldap.SSLContextBuilder;
import org.forgerock.opendj.ldap.TrustManagers;
import org.forgerock.opendj.ldap.requests.PasswordModifyExtendedRequest;
import org.forgerock.opendj.ldap.requests.PlainSASLBindRequest;
import org.forgerock.opendj.ldap.requests.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.evalcode.services.directory.service.xml.LdapConfiguration;


/**
 * Directory
 *
 * @author carsten.schipke@gmail.com
 */
public class Directory
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(Directory.class);


  // MEMBERS
  final LdapConfiguration configuration;
  volatile Connection connection;


  // CONSTRUCTION
  @Inject
  public Directory(final LdapConfiguration configuration)
  {
    this.configuration=configuration;
  }


  // ACCESSORS/MUTATORS
  public synchronized void bind(final String username, final String password)
    throws DirectoryException
  {
    Connection connection=this.connection;

    if(null==connection || connection.isClosed())
    {
      try(final LDAPConnectionFactory connectionFactory=new LDAPConnectionFactory(
          configuration.getHost(), configuration.getPort(), getLdapOptions()))
      {
        connection=connectionFactory.getConnection();

        final String bindDN="dn:cn="+username+","+configuration.getUserBase();
        final PlainSASLBindRequest bindRequest=Requests.newPlainSASLBindRequest(
          bindDN, password.toCharArray()
        );

        // final CRAMMD5SASLBindRequest bindRequestInit=Requests.newCRAMMD5SASLBindRequest(
        // bindDN, "".toCharArray()
        // );
        //
        // final BindResult bindResultInit=connection.bind(bindRequestInit);
        //
        // LOG.info("INIT BIND RESULT: {}", bindResultInit.isSuccess());
        // LOG.info("INIT BIND RESULT: {}", bindResultInit.getResultCode());
        // LOG.info("INIT BIND RESULT: {}", bindResultInit.getServerSASLCredentials());
        //
        // bindResultInit.getServerSASLCredentials()
        // final CRAMMD5SASLBindRequest bindRequest=Requests.newCRAMMD5SASLBindRequest(
        // bindDN, password.toCharArray()
        // );
        //
        // final BindResult bindResult=connection.bind(bindRequest);
        //
        // LOG.info("BIND RESULT: {}", bindResult.isSuccess());
        // LOG.info("BIND RESULT: {}", bindResult.getResultCode());
        // LOG.info("BIND RESULT: {}", bindResult.getServerSASLCredentials());

        connection.bind(bindRequest);

        if(null==this.connection || this.connection.isClosed())
          this.connection=connection;
      }
      catch(final ErrorResultException e)
      {
        LOG.error(e.getMessage(), e);


        throw new DirectoryException(e.getMessage(), e);
      }
    }
  }

  public synchronized void unbind()
  {
    if(null!=connection && !connection.isClosed())
      connection.close();
  }

  public synchronized boolean isBound()
  {
    if(null==connection)
      return false;

    return !connection.isClosed() && connection.isValid();
  }

  public synchronized void updatePassword(final String username,
      final String passwordOld, final String passwordNew)
    throws DirectoryException
  {
    if(null==connection || connection.isClosed())
      bind(username, passwordOld);

    final String bindDN="dn:cn="+username+","+configuration.getUserBase();
    final PasswordModifyExtendedRequest request=Requests.newPasswordModifyExtendedRequest();

    request.setUserIdentity(bindDN);
    request.setOldPassword(passwordOld.toCharArray());
    request.setNewPassword(passwordNew.toCharArray());

    try
    {
      connection.extendedRequest(request);
    }
    catch(final ErrorResultException e)
    {
      LOG.error(e.getMessage(), e);

      throw new DirectoryException(e.getMessage(), e);
    }
  }


  // IMPLEMENTATION
  protected LDAPOptions getLdapOptions()
  {
    final LDAPOptions options=new LDAPOptions();

    try
    {
      final SSLContext context=new SSLContextBuilder()
        .setTrustManager(TrustManagers.trustAll())
        .getSSLContext();

      options.setSSLContext(context);
    }
    catch(final GeneralSecurityException e)
    {
      LOG.error(e.getMessage(), e);
    }

    return options;
  }

  @Override
  protected void finalize() throws Throwable
  {
    // FIXME Implement component-and-session-lifecycle-oriented connection pool.
    unbind();

    super.finalize();
  }


  /**
   * Command
   *
   * @author carsten.schipke@gmail.com
   */
  interface Command
  {
    // ACCESSORS
    void invoke(final Connection connection) throws DirectoryException;
  }


  /**
   * DirectoryException
   *
   * @author carsten.schipke@gmail.com
   */
  public static class DirectoryException extends ExecutionException
  {
    // PREDEFINED PROPERTIES
    private static final long serialVersionUID=1L;


    // CONSTRUCTION
    public DirectoryException(final Throwable e)
    {
      super(e);
    }

    public DirectoryException(final String message, final Throwable e)
    {
      super(message, e);
    }
  }
}
