/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
package ddf.security.filter.handlers;


import org.apache.cxf.sts.QNameConstants;
import org.apache.cxf.ws.security.sts.provider.model.secext.AttributedString;
import org.apache.cxf.ws.security.sts.provider.model.secext.PasswordString;
import org.apache.cxf.ws.security.sts.provider.model.secext.UsernameTokenType;
import org.apache.ws.security.WSConstants;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;
import java.io.StringWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnonymousHandler implements AuthenticationHandler {
    public static final Logger logger = LoggerFactory.getLogger(AnonymousHandler.class.getName());

    @Override
    public FilterResult getNormalizedToken(ServletRequest request, ServletResponse response, FilterChain chain, boolean resolve) {
        FilterResult result = new FilterResult();
        result.setStatus(FilterResult.FilterStatus.NO_ACTION);

        // For anonymous - always generate authentication credentials as 'guest'
        UsernameTokenType usernameToken = new UsernameTokenType();
        AttributedString username = new AttributedString();
        username.setValue("guest");
        usernameToken.setUsername(username);
        JAXBElement<UsernameTokenType> usernameTokenElement =
          new JAXBElement<UsernameTokenType>(
            QNameConstants.USERNAME_TOKEN, UsernameTokenType.class, usernameToken
          );
        PasswordString password = new PasswordString();
        password.setValue("guest");
        password.setType(WSConstants.PASSWORD_TEXT);
        usernameToken.getAny().add(password);
        JAXBElement<PasswordString> passwordType =
          new JAXBElement<PasswordString>(
            QNameConstants.PASSWORD, PasswordString.class, password
          );
        usernameToken.getAny().add(passwordType);

        Writer writer = new StringWriter();
        JAXB.marshal(usernameTokenElement, writer);

        String usernameSecurityToken = writer.toString();
        logger.debug("Security token returned: {}", usernameSecurityToken);

        result.setAuthCredentials(usernameSecurityToken);
        result.setStatus(FilterResult.FilterStatus.COMPLETED);
        return result;
    }
}