package net.evalcode.services.directory.service.xml;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import net.evalcode.services.manager.component.annotation.Configuration;


/**
 * LdapConfiguration
 *
 * @author carsten.schipke@gmail.com
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Configuration("ldap.json")
public class LdapConfiguration
{
  /**
   * Authentication
   *
   * @author carsten.schipke@gmail.com
   */
  public static enum Authentication
  {
    // PREDEFINED PROPERTIES
    SIMPLE;
  }


  /**
   * Scheme
   *
   * @author carsten.schipke@gmail.com
   */
  public static enum Scheme
  {
    // PREDEFINED PROPERTIES
    LDAP(389),
    LDAPS(636);


    // MEMBERS
    public final int port;


    // CONSTRUCTION
    Scheme(final int port)
    {
      this.port=port;
    }
  }


  // PREDEFINED PROPERTIES
  static final Authentication DEFAULT_AUTHENTICATION=Authentication.SIMPLE;

  static final String DEFAULT_USER_OBJECT_CLASS="inetOrgPerson";
  static final String DEFAULT_USER_ATTRIBUTE_NAME="cn";
  static final String DEFAULT_USER_ATTRIBUTE_PASSWORD="userPassword";

  static final String DEFAULT_GROUP_OBJECT_CLASS="groupOfUniqueNames";
  static final String DEFAULT_GROUP_ATTRIBUTE_NAME="cn";
  static final String DEFAULT_GROUP_ATTRIBUTE_MEMBER="uniqueMember";


  // MEMBERS
  @XmlElement(name="scheme", required=true)
  Scheme scheme;
  @XmlElement(name="host", required=true)
  String host;
  @XmlElement(name="port")
  Integer port;
  @XmlElement(name="authentication")
  Authentication authentication=DEFAULT_AUTHENTICATION;

  @XmlElement(name="user_base", required=true)
  String userBase;
  @XmlElement(name="user_object_class")
  String userObjectClass=DEFAULT_USER_OBJECT_CLASS;
  @XmlElement(name="user_attribute_name")
  String userAttributeName=DEFAULT_USER_ATTRIBUTE_NAME;
  @XmlElement(name="user_attribute_password")
  String userAttributePassword=DEFAULT_USER_ATTRIBUTE_PASSWORD;

  @XmlElement(name="group_base", required=true)
  String groupBase;
  @XmlElement(name="group_object_class")
  String groupObjectClass=DEFAULT_GROUP_OBJECT_CLASS;
  @XmlElement(name="group_attribute_name")
  String groupAttributeName=DEFAULT_GROUP_ATTRIBUTE_NAME;
  @XmlElement(name="group_attribute_member")
  String groupAttributeMember=DEFAULT_GROUP_ATTRIBUTE_MEMBER;


  // ACCESSORS/MUTATORS
  public Scheme getScheme()
  {
    return scheme;
  }

  public String getHost()
  {
    return host;
  }

  public int getPort()
  {
    if(null==port)
      return scheme.port;

    return port.intValue();
  }

  public Authentication getAuthentication()
  {
    return authentication;
  }

  public String getUserBase()
  {
    return userBase;
  }

  public String getUserObjectClass()
  {
    return userObjectClass;
  }

  public String getUserAttributeName()
  {
    return userAttributeName;
  }

  public String getUserAttributePassword()
  {
    return userAttributePassword;
  }

  public String getGroupBase()
  {
    return groupBase;
  }

  public String getGroupObjectClass()
  {
    return groupObjectClass;
  }

  public String getGroupAttributeName()
  {
    return groupAttributeName;
  }

  public String getGroupAttributeMember()
  {
    return groupAttributeMember;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String toString()
  {
    final StringBuilder stringBuilder=new StringBuilder(512);

    stringBuilder.append(LdapConfiguration.class.getSimpleName());
    stringBuilder.append("{");

    stringBuilder.append(String.format("scheme: %1$s, ", getScheme()));
    stringBuilder.append(String.format("host: %1$s, ", getHost()));
    stringBuilder.append(String.format("port: %1$d, ", getPort()));
    stringBuilder.append(String.format("authentication: %1$s, ", getAuthentication()));

    stringBuilder.append(String.format("user_base: %1$s, ", getUserBase()));
    stringBuilder.append(String.format("user_object_class: %1$s, ", getUserBase()));
    stringBuilder.append(String.format("user_attribute_name: %1$s, ", getUserAttributeName()));
    stringBuilder.append(String.format("user_attribute_password: %1$s, ",
      getUserAttributePassword()
    ));

    stringBuilder.append(String.format("group_base: %1$s, ", getGroupBase()));
    stringBuilder.append(String.format("group_object_class: %1$s, ", getGroupBase()));
    stringBuilder.append(String.format("group_attribute_name: %1$s, ", getGroupAttributeName()));
    stringBuilder.append(String.format("group_attribute_member: %1$s, ",
      getGroupAttributeMember()
    ));

    stringBuilder.append("}");

    return stringBuilder.toString();
  }
}
