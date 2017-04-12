package com.lhjz.portal.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class RunAsAuth {

	private Authentication authenticationOld;

	private RunAsAuth() {
		this.backup();
	}

	public static RunAsAuth instance() {
		return new RunAsAuth();
	}

	private void backup() {
		try {
			this.authenticationOld = SecurityContextHolder.getContext().getAuthentication();
		} catch (Exception e) {
		}

	}

	public RunAsAuth runAs(String username) {

		try {
			AbstractAuthenticationToken authenticationToken = new AbstractAuthenticationToken(null) {

				private static final long serialVersionUID = 1033003540219681089L;

				@Override
				public Object getPrincipal() {
					return username;
				}

				@Override
				public Object getCredentials() {
					return null;
				}
			};
			authenticationToken.setAuthenticated(true);
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);

		} catch (Exception e) {
		}
		
		return this;
	}

	public RunAsAuth rest() {
		try {
			SecurityContextHolder.getContext().setAuthentication(this.authenticationOld);
		} catch (Exception e) {
		}
		
		return this;
	}

}