package net.evalcode.services.directory.service.security;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import org.eclipse.jetty.plus.jaas.spi.UserInfo;
import org.eclipse.jetty.util.StringUtil;
import org.forgerock.opendj.ldap.Attribute;
import org.forgerock.opendj.ldap.ByteString;
import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.DN;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.ErrorResultIOException;
import org.forgerock.opendj.ldap.LDAPConnectionFactory;
import org.forgerock.opendj.ldap.SearchResultReferenceIOException;
import org.forgerock.opendj.ldap.SearchScope;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;
import org.forgerock.opendj.ldif.ConnectionEntryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.evalcode.services.http.service.security.JaasLoginModule;
import net.evalcode.services.http.service.security.ServicePermission;
import net.evalcode.services.manager.util.security.Hash;


/**
 * JaasLdapLoginModule
 *
 * @author carsten.schipke@gmail.com
 */
public class JaasLdapLoginModule extends JaasLoginModule
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(JaasLdapLoginModule.class);

  static final String HOST="host";
  static final String PORT="port";
  static final String USER_BASE="user_base";
  static final String USER_OBJECT_CLASS="user_object_class";
  static final String USER_ATTRIBUTE_NAME="user_attribute_name";
  static final String USER_ATTRIBUTE_MEMBER="user_attribute_member";
  static final String GROUP_BASE="group_base";
  static final String GROUP_OBJECT_CLASS="group_object_class";
  static final String GROUP_ATTRIBUTE_ACL="group_attribute_acl";
  static final String GROUP_ATTRIBUTE_NAME="group_attribute_name";


  // MEMBERS
  final Map<String, String> config=new HashMap<>();


  // OVERRIDES/IMPLEMENTS
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void initialize(final Subject subject, final CallbackHandler callbackHandler,
      final Map sharedState, final Map options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);

    config.putAll(options);
  }

  @Override
  public boolean login() throws LoginException
  {
    setAuthenticated(false);

    final NameCallback callbackPrincipal=new NameCallback("Username");
    final PasswordCallback callbackCredential=new PasswordCallback("Password", false);

    try
    {
      getCallbackHandler().handle(new Callback[] {callbackPrincipal, callbackCredential});
    }
    catch(final IOException | UnsupportedCallbackException e)
    {
      throw new LoginException(e.getMessage());
    }

    final String principal=callbackPrincipal.getName();
    final char[] credential=callbackCredential.getPassword();

    try(final LDAPConnectionFactory connectionFactory=new LDAPConnectionFactory(config.get(HOST), Integer.parseInt(config.get(PORT)));
        final Connection connection=connectionFactory.getConnection())
    {
      final SearchResultEntry searchResultUser=connection.searchSingleEntry(
        config.get(USER_BASE),
        SearchScope.WHOLE_SUBTREE,
        String.format("(&(objectClass=%s)(%s=%s))",
          config.get(USER_OBJECT_CLASS),
          config.get(USER_ATTRIBUTE_NAME),
          principal
        ),
        config.get(USER_ATTRIBUTE_NAME)
      );

      final DN bindDN=searchResultUser.getName();
      connection.bind(bindDN.toString(), credential);

      if(null==searchResultUser.getAttribute(config.get(USER_ATTRIBUTE_NAME)))
        return false;

      final ConnectionEntryReader searchResultRoles=connection.search(
        config.get(GROUP_BASE),
        SearchScope.WHOLE_SUBTREE,
        String.format("(&(objectClass=%s)(%s=%s))",
          config.get(GROUP_OBJECT_CLASS),
          config.get(USER_ATTRIBUTE_MEMBER),
          bindDN.toString()
        ),
        new String[] {config.get(USER_ATTRIBUTE_NAME), config.get(GROUP_ATTRIBUTE_ACL)}
      );

      final Map<String, Set<ServicePermission>> roles=new HashMap<>();

      while(searchResultRoles.hasNext())
      {
        final SearchResultEntry searchResultRole=searchResultRoles.readEntry();
        final Attribute attributeRoleName=searchResultRole.getAttribute(config.get(USER_ATTRIBUTE_NAME));

        if(null!=attributeRoleName)
        {
          final String roleName=attributeRoleName.firstValueAsString();
          final Attribute attributeRoleAcl=searchResultRole.getAttribute(config.get(GROUP_ATTRIBUTE_ACL));

          final Set<ServicePermission> rolePermissions=new HashSet<>();

          for(final ByteString roleAclEntry : attributeRoleAcl)
          {
            final String aclEntry=roleAclEntry.toString();
            final String aclEntryNoQuotes=StringUtil.replace(aclEntry, "\"", "");
            final String[] aclEntryChunks=aclEntryNoQuotes.split(",");

            if(2!=aclEntryChunks.length)
            {
              LOG.debug("Skipping illegal ACL entry [entry: {}].", aclEntry);

              continue;
            }

            final String privilege=aclEntryChunks[0].trim().toLowerCase();
            final String action=aclEntryChunks[1].trim().toLowerCase();

            rolePermissions.add(new ServicePermission(privilege, action));
          }

          roles.put(roleName, rolePermissions);
        }
      }

      setCurrentUser(new ServiceUserInfo(principal, Hash.random(), roles));

      setAuthenticated(true);

      LOG.info("Authenticated [principal: {}, roles: {}].", principal, roles);

      return true;
    }
    catch(final ErrorResultException | SearchResultReferenceIOException | ErrorResultIOException e)
    {
      throw new LoginException(e.getMessage());
    }
  }

  @Override
  public UserInfo getUserInfo(final String user)
  {
    return null;
  }
}
