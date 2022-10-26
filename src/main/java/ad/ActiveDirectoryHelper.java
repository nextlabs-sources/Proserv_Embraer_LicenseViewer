package main.java.ad;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import main.java.common.PropertyLoader;
import main.java.common.Utils;
import main.java.object.User;

public class ActiveDirectoryHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(ActiveDirectoryHelper.class);

	/**
	 * Return LdapContext
	 * 
	 * @param userName
	 *            ldap user name
	 * @param password
	 *            ldap password
	 * @return LdapContext
	 */
	public static LdapContext getLDAPContextFromPool(String userName, String password) {
		long lCurrentTime = System.nanoTime();
		LdapContext ctx = null;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(LdapContext.CONTROL_FACTORIES, "com.sun.jndi.ldap.ControlFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			/*
			 * if (PropertyLoader.properties.getProperty("ssl-authentication").
			 * equals("true")) { LOG.info("LDAP Authenticated with SSL");
			 * env.put(Context.SECURITY_PROTOCOL, "ssl"); }
			 */
			env.put(Context.SECURITY_PRINCIPAL, userName);
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put(Context.PROVIDER_URL, "ldap://" + PropertyLoader.properties.getProperty("ad_server") + ":"
					+ PropertyLoader.properties.getProperty("ad_port") + "/");
			env.put(Context.STATE_FACTORIES, "PersonStateFactory");
			env.put(Context.OBJECT_FACTORIES, "PersonObjectFactory");
			env.put("com.sun.jndi.ldap.connect.pool", "true");
			ctx = new InitialLdapContext(env, null);

		} catch (Exception ex) {
			LOG.error("ActiveDirectoryHelper getLDAPContextFromPool(): " + ex.getMessage(), ex);
		}
		LOG.debug("ActiveDirectoryHelper getLDAPContextFromPool() completed. Time spent: "
				+ ((System.nanoTime() - lCurrentTime) / 1000000.00) + "ms");
		return ctx;
	}

	public static LdapContext getLDAPContext(String userName, String password) {
		long lCurrentTime = System.nanoTime();
		LdapContext ctx = null;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(LdapContext.CONTROL_FACTORIES, "com.sun.jndi.ldap.ControlFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			/*
			 * if (PropertyLoader.properties.getProperty("ssl-authentication").
			 * equals("true")) { LOG.info("LDAP Authenticated with SSL");
			 * env.put(Context.SECURITY_PROTOCOL, "ssl"); }
			 */
			env.put(Context.SECURITY_PRINCIPAL, userName);
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put(Context.PROVIDER_URL, "ldap://" + PropertyLoader.properties.getProperty("ad_server") + ":"
					+ PropertyLoader.properties.getProperty("ad_port") + "/");
			env.put(Context.STATE_FACTORIES, "PersonStateFactory");
			env.put(Context.OBJECT_FACTORIES, "PersonObjectFactory");
			env.put("com.sun.jndi.ldap.connect.pool", "false");
			ctx = new InitialLdapContext(env, null);

		} catch (Exception ex) {
			LOG.error("ActiveDirectoryHelper getLDAPContext(): " + ex.getMessage(), ex);
		}
		LOG.debug("ActiveDirectoryHelper getLdapContext() completed. Time spent: "
				+ ((System.nanoTime() - lCurrentTime) / 1000000.00) + "ms");
		return ctx;
	}

	public static boolean authenticate(String userName, String password) {
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(LdapContext.CONTROL_FACTORIES, "com.sun.jndi.ldap.ControlFactory");
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			/*
			 * if (PropertyLoader.properties.getProperty("ssl-authentication").
			 * equals("true")) { LOG.info("LDAP Authenticated with SSL");
			 * env.put(Context.SECURITY_PROTOCOL, "ssl"); }
			 */
			env.put(Context.SECURITY_PRINCIPAL, userName);
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put(Context.PROVIDER_URL, "ldap://" + PropertyLoader.properties.getProperty("ad_server") + ":"
					+ PropertyLoader.properties.getProperty("ad_port") + "/");
			env.put(Context.STATE_FACTORIES, "PersonStateFactory");
			env.put(Context.OBJECT_FACTORIES, "PersonObjectFactory");
			env.put("com.sun.jndi.ldap.connect.pool", "false");
			LdapContext ctx = new InitialLdapContext(env, null);
			ctx.close();
			return true;

		} catch (Exception ex) {
			LOG.error("ActiveDirectoryHelper authenticate(): " + ex.getMessage(), ex);
			return false;
		}
	}

	public static boolean testLdap(String userName, String password) {
		LdapContext context = ActiveDirectoryHelper.getLDAPContextFromPool(
				PropertyLoader.properties.getProperty("edit_account_name"),
				PropertyLoader.properties.getProperty("edit_account_password"));

		if (context == null) {
			return false;
		} else {
			try {
				context.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
			return true;
		}
	}

	/**
	 * Return one user with selected attributes based on filter
	 * 
	 * @param filter
	 *            Search filter
	 * @param attributes
	 *            Array of selected attributes
	 * @param userName
	 *            Authorized user
	 * @param password
	 *            Password
	 * @return Searched user
	 */
	public static User getUser(String filter, String[] attributes, String user, String password) {
		User returnUser = null;
		try {

			LOG.info("ActiveDirectoryHelper getUser(): filter = " + filter);
			SearchControls constraint = new SearchControls();
			if (attributes != null) {
				constraint.setReturningAttributes(attributes);
			}
			constraint.setSearchScope(SearchControls.SUBTREE_SCOPE);

			NamingEnumeration<SearchResult> answer = searchAD(PropertyLoader.properties.getProperty("ldap_domain_name"),
					filter, constraint, user, password);

			if (answer == null) {
				LOG.error("ActiveDirectoryHelper getUser(): Error in search: return null");
				return returnUser;
			}

			if (!answer.hasMoreElements()) {
				LOG.info("ActiveDirectoryHelper getUser(): No user found!");
				return returnUser;
			}

			if (answer.hasMoreElements()) {
				LOG.info("ActiveDirectoryHelper getUser(): User found!");
				SearchResult sr = (SearchResult) answer.next();
				returnUser = new User(sr.getAttributes());
			}

			answer.close();
		} catch (Exception e) {
			LOG.error("ActiveDirectoryHelper getUser(): " + e.getMessage(), e);
		}
		return returnUser;
	}

	/**
	 * Perform general AD search and return search result. Get ldap context by
	 * itself
	 * 
	 * @param domain
	 *            Ldap domain
	 * @param filter
	 *            Search filter
	 * @param constraint
	 *            Search constraint
	 * @param userName
	 *            Authorized user
	 * @param password
	 *            Password
	 * @return Search result
	 */
	public static NamingEnumeration<SearchResult> searchAD(String domain, String filter, SearchControls constraint, String user, String password) {
		NamingEnumeration<SearchResult> answer = null;
		LdapContext ctx = getLDAPContextFromPool(user, password);
		constraint.setReturningObjFlag(false);
		try {
			answer = ctx.search(domain, filter, constraint);
		} catch (NamingException ne) {
			LOG.error("ActiveDirectoryHelper searchAD(): " + ne.getMessage(), ne);
		} finally {
			try {
				ctx.close();
			} catch (Exception e) {
				LOG.error("ActiveDirectoryHelper getUsers(): " + e.getMessage(), e);
			}
		}
		return answer;
	}

	/**
	 * Get the value of an attribute of an object
	 * 
	 * @param attributes
	 *            Attributes of the object
	 * @param attributeName
	 *            Name of the attribute to be query
	 * @return Value of the attribute
	 */
	public static String getAttribute(Attributes attributes, String attributeName) {
		Attribute attrGet = attributes.get(attributeName);
		if (attrGet == null || attrGet.toString().trim().equals("")) {
			return "";
		} else {
			String temp = attrGet.toString();
			String[] tList = temp.split(":");
			return tList[1].trim();
		}
	}

	public static List<String> getAllUserGroup(String filter, String user, String password) {
		List<String> result = new ArrayList<String>();
		try {
			String[] attributeID = new String[] { "groupMembership" };
			Attributes attributes = getUser(filter, attributeID, user, password).getAttributes();
			Attribute attr = attributes.get("groupMembership");
			NamingEnumeration<?> nenum = attr.getAll();
			LOG.info("ActiveDirectoryHelper getAllUserGroup(): Found " + attr.size() + " groups");
			while (nenum.hasMore()) {
				String value = (String) nenum.next();
				String[] tValue = value.split(",");
				tValue = tValue[0].split("=");
				value = tValue[1].trim();
				LOG.info("ActiveDirectoryHelper getAllUserGroup(): Member of: " + value);
				result.add(value);
			}
		} catch (Exception e) {
			LOG.error("ActiveDirectoryHelper getAllUserGroup(): " + e.getMessage(), e);
		}
		return result;
	}
}
